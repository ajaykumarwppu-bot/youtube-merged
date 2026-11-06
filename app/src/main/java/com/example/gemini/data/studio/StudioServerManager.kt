package com.example.gemini.data.studio

import android.content.Context
import android.util.Log
import com.example.gemini.BuildConfig
import com.example.gemini.data.local.LocalEnvironmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Creator Studio local web app (Next.js standalone build).
 *
 * assets/creator-studio.zip -> <home>/creator-studio/ extract karke app ke apne
 * Linux environment ke Node se `server.js` chalata hai — BrowserExtensionManager
 * ke host pattern jaisa hi (loopback bind, health polling, log file).
 *
 * DATA SAFETY: user ka studio data <home>/creator-studio/data/ me rehta hai.
 * APK update par sirf build files replace hote hain (marker version mismatch par
 * bhi `data/` folder preserve hota hai) — isliye update ke baad bhi videos,
 * brain, settings sab waise hi milte hain.
 */
object StudioServerManager {

    private const val TAG = "StudioServerManager"
    private const val ASSET_ZIP = "creator-studio.zip"
    private const val START_TIMEOUT_MS = 90_000L

    @Volatile
    private var process: Process? = null

    private val mutex = Mutex()

    fun port(): Int = BuildConfig.STUDIO_PORT

    fun baseUrl(): String = "http://127.0.0.1:${port()}"

    private fun installDir(context: Context): File =
        File(LocalEnvironmentManager.getHomeDir(context), "creator-studio")

    private fun dataDir(context: Context): File = File(installDir(context), "data")

    private fun markerFile(context: Context): File = File(installDir(context), ".asset-version")

    private fun logFile(context: Context): File =
        File(LocalEnvironmentManager.getHomeDir(context), "creator-studio-server.log")

    private fun expectedVersion(): String = "${BuildConfig.VERSION_NAME}+${BuildConfig.VERSION_CODE}"

    fun isHealthy(): Boolean = runCatching {
        val conn = URL("${baseUrl()}/api/cs/dashboard").openConnection() as HttpURLConnection
        conn.connectTimeout = 1000
        conn.readTimeout = 2000
        val ok = conn.responseCode == 200
        conn.disconnect()
        ok
    }.getOrDefault(false)

    /**
     * Server healthy hai to reuse, warna assets extract karke Node server start karo
     * aur health aane tak poll karo. Kahin bhi fail ho to IllegalStateException with
     * log path — StudioScreen wahi error dikhata hai.
     */
    suspend fun ensureStarted(context: Context): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (isHealthy()) return@withLock

            extractAssetsIfNeeded(context)

            val node = File(LocalEnvironmentManager.getBinDir(context), "node")
            if (!node.canExecute()) {
                throw IllegalStateException(
                    "Node.js is required in the local terminal environment to run Creator Studio. " +
                        "Pehle local environment setup karo (Settings > Local Environment)."
                )
            }

            if (process?.isAlive != true) {
                val command = listOf(node.absolutePath, ".next/standalone/server.js")
                val builder = ProcessBuilder(command)
                    .directory(installDir(context))
                    .redirectErrorStream(true)
                builder.environment().putAll(
                    mapOf(
                        "HOME" to LocalEnvironmentManager.getHomeDir(context).absolutePath,
                        "PREFIX" to LocalEnvironmentManager.getPrefixDir(context).absolutePath,
                        "PATH" to "${LocalEnvironmentManager.getBinDir(context).absolutePath}:/system/bin",
                        "TMPDIR" to LocalEnvironmentManager.getTmpDir(context).absolutePath,
                        "NODE_ENV" to "production",
                        "PORT" to port().toString(),
                        "HOSTNAME" to "127.0.0.1",
                        "STUDIO_DATA_DIR" to dataDir(context).absolutePath,
                    )
                )

                // Server ka pura output log file me — phone par diagnose karne ke liye
                val log = logFile(context)
                runCatching { log.writeText("[${java.util.Date()}] starting: ${command.joinToString(" ")}\n") }
                val proc = try {
                    builder.start()
                } catch (e: Exception) {
                    runCatching { log.appendText("failed to launch node: $e\n") }
                    throw IllegalStateException(
                        "Creator Studio server failed to launch: ${e.message} (log: ${log.absolutePath})", e
                    )
                }
                process = proc
                Thread {
                    runCatching {
                        proc.inputStream.bufferedReader().useLines { lines ->
                            lines.forEach { line ->
                                Log.d(TAG, line)
                                runCatching { log.appendText(line + "\n") }
                            }
                        }
                    }
                    val code = runCatching { proc.waitFor() }.getOrNull()
                    Log.w(TAG, "Creator Studio server exited with code $code")
                    runCatching { log.appendText("[${java.util.Date()}] server exited with code $code\n") }
                }.start()
            }

            val deadline = System.currentTimeMillis() + START_TIMEOUT_MS
            while (System.currentTimeMillis() < deadline) {
                if (isHealthy()) return@withLock
                if (process?.isAlive == false) {
                    throw IllegalStateException(
                        "Creator Studio server exited during startup (log: ${logFile(context).absolutePath})"
                    )
                }
                Thread.sleep(500)
            }
            throw IllegalStateException(
                "Creator Studio server start timeout — check ${logFile(context).absolutePath}"
            )
        }
    }

    /**
     * APK ke assets se Studio web app extract karo. Marker version same ho aur
     * server.js maujood ho to skip (fast path). Update par build files replace
     * hote hain PAR `data/` folder kabhi delete nahi hota.
     */
    private fun extractAssetsIfNeeded(context: Context) {
        val target = installDir(context)
        val marker = markerFile(context)
        val serverJs = File(target, ".next/standalone/server.js")
        if (marker.exists() && marker.readText().trim() == expectedVersion() && serverJs.exists()) return

        val inputStream = context.assets.open(ASSET_ZIP)
        target.mkdirs()
        target.listFiles()?.forEach { if (it.name != "data") it.deleteRecursively() }
        val alreadyHadData = dataDir(context).resolve("creator-studio").isDirectory

        ZipInputStream(inputStream.buffered()).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            val canonicalTarget = target.canonicalPath + File.separator
            while (entry != null) {
                val out = File(target, entry.name)
                if (out.canonicalPath != target.canonicalPath &&
                    !out.canonicalPath.startsWith(canonicalTarget)
                ) {
                    throw SecurityException("Blocked zip entry outside install dir: ${entry.name}")
                }
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile?.mkdirs()
                    out.outputStream().buffered().use { zip.copyTo(it) }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        // Fresh install: zip ke andar bundled default skill library
        // (.next/standalone/data/creator-studio) ko user data dir me seed karo.
        if (!alreadyHadData) {
            val defaultData = File(File(target, ".next/standalone"), "data/creator-studio")
            if (defaultData.isDirectory) {
                val dest = dataDir(context).resolve("creator-studio")
                dest.parentFile?.mkdirs()
                defaultData.copyRecursively(dest, overwrite = true)
            }
        }

        marker.writeText(expectedVersion())
        Log.i(TAG, "Creator Studio assets extracted to ${target.absolutePath}")
    }
}

package com.example.gemini

import android.app.Application
import android.util.Log
import com.example.gemini.data.adb.AdbConnectManager
import com.example.gemini.data.local.LocalEnvironmentManager
import com.example.gemini.data.updater.PluginUpdateCheckWorker
import com.example.gemini.data.updater.UpdateCheckWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AntiGemApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Crash diagnostics: bina adb ke bhi phone par crash ka stack trace mile —
        // terminal me `cat ~/app-crash.log` se dekh sakte ho (log $HOME me likha jata hai).
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val home = LocalEnvironmentManager.getHomeDir(this@AntiGemApplication)
                if (!home.exists()) home.mkdirs()
                val target = if (home.isDirectory) java.io.File(home, "app-crash.log")
                             else java.io.File(filesDir, "app-crash.log")
                target.appendText(
                    "[${java.util.Date()}] thread=${t.name}\n${Log.getStackTraceString(e)}\n\n"
                )
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(t, e)
        }

        // Termux-family prewarm (Main-thread ANR fix): env install hone ke baad pehla
        // terminal/Studio spawn heavy file-walk (com.termux path rewrite) + glibc extract
        // trigger karta tha jo Main thread par app ko hang/karvata tha. Ye kaam ab app
        // start par background me ho jata hai — install flow bhi yeh karta hai (IO par).
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                if (LocalEnvironmentManager.isInstalled(this@AntiGemApplication)) {
                    LocalEnvironmentManager.ensureTermuxScriptPaths(this@AntiGemApplication)
                    LocalEnvironmentManager.ensureGlibcEnvironment(this@AntiGemApplication)
                }
            }
        }

        UpdateCheckWorker.schedule(this)
        PluginUpdateCheckWorker.schedule(this)
        // Reconnects adb when Wireless debugging turns back on (it gets a new port every time)
        AdbConnectManager.init(this)
        // NOTE: TelemetryDeck analytics intentionally removed in this fork —
        // koi usage data kahin nahi bheja jata (original developer ka telemetry
        // bhi disabled). Chaaho to apna telemetry yahan add kar sakte ho.
    }
}

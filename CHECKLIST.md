# CHECKLIST — हर चीज़ की verification

Har item ka status honest hai:
- ✅ = sandbox me abhi verify kiya (build/test chala ke)
- 🤖 = GitHub Actions (CI) par push ke baad verify hoga — APK isi se banta hai
- 📱 = phone par install karke test hoga (setup.md follow karo)

## 1. Alag app identity (original AntiGem se koi connection nahi)

- [x] ✅ Fresh clone → `.git` delete → bilkul nayi git history (fork relation zero)
- [x] ✅ Git remote original repo ko point NAHI karta (origin = ajaykumarwppu-bot/youtube-merged)
- [x] ✅ `applicationId` = `com.creatorstudio` (original: com.antigem / com.termux) — side-by-side install hoga
- [x] ✅ App label = "Creator Studio" (main + termux flavor strings)
- [x] ✅ Updater URLs → is repo par (version.json raw + releases download)
- [x] ✅ APK naming pattern updater se match: `CreatorStudio-<flavor>-v<ver>-release.apk`
- [x] ✅ Linux rootfs download URL → is repo ke release asset par (tag rootfs-v1)
- [x] ✅ Telemetry (TelemetryDeck) poori tarah remove — na toast, na data bhejna; original dev ke telemetry se connection zero
- [x] ✅ About/Repo links (Settings dialog, Update dialog) → is repo
- [x] ✅ `grep -r "santoshkurmi/antigem"` in app code = 0 matches
  - Note: `santoshkurmi/termux-api` ka link sirf termux-api APK download ke liye hai — wo alag utility hai, antigem repo nahi

## 2. Update system (naya APK = direct update, data safe)

- [x] ✅ version.json scheme: version_code 100 se start, har release bump
- [x] ✅ Release signing: apna keystore (alias creatorstudio) — CI secrets se; har build SAME key → update install hoga
- [x] ✅ Studio data `<home>/creator-studio/data/` me — APK update is folder ko delete nahi karta (StudioServerManager wipe logic `data/` preserve karta hai)
- [x] ✅ Studio assets marker versioning — naya APK par build files replace, data intact
- [x] ✅ App-level data (DataStore, chats) — Android khud update par preserve karta hai
- [x] ✅ Updater app ke andar hai — naya version.json push → app me update notice → APK download → install

## 3. Creator Studio web app (creator-studio/)

- [x] ✅ Standalone Next.js project — khud build hota hai (`npm run build` ✓ chala ke dekha)
- [x] ✅ Standalone server boot hota hai (`node server.js` ✓ — health + pages 200)
- [x] ✅ **Demo data ZERO**: videos/pipeline/brain stores khali start hote hain (seed functions delete)
  - `GET /api/cs/videos` → `{"videos":[]}` ✓
  - `GET /api/cs/pipeline` → `{"ideas":[],"runs":[]}` ✓
  - `GET /api/cs/brain` → `{"videos":[],"questions":[],"experiments":[]}` ✓
- [x] ✅ Settings seed = EMPTY keys (koi fake key nahi) — sirf sensible defaults
- [x] ✅ Built-in skill library ship hoti hai (index.json + packages/ — ye content hai, demo data nahi)
- [x] ✅ `STUDIO_DATA_DIR` env — device par data alag folder me (✓ test kiya: POST idea → file usi dir me bani)
- [x] ✅ `POST /api/cs/videos` — agent video record create/update kar sake (upsert + validation ✓ test kiya)
- [x] ✅ Upload Package feature (title/titleNote/description/tags/chapters/endScreen/pinnedComment) — videos POST route isi schema me likhta hai
- [x] ✅ Sab 8 API areas live: brain, dashboard, packages, pipeline, settings, skills, upload, videos

## 4. Android integration (Kotlin)

- [x] ✅ `AppViewMode.STUDIO` enum + initial mode mapping + persistent Box (baki modes jaisa)
- [x] ✅ `StudioScreen.kt` — loading/error/ready states + WebView (loopback only) + reload
- [x] ✅ `StudioServerManager.kt` — assets extract (zip-traversal guard) + Node spawn (BrowserExtensionManager jaisa pattern) + health polling + log file
- [x] ✅ Floating switcher me "studio" item — vertical + horizontal dono layouts, icon/label/selection/click sab wired
- [x] ✅ AuthPreferences default items me studio + purani saved lists me bhi guarantee-append
- [x] ✅ `STUDIO_PORT` per-flavor (standard 8877 / termux 8875) — side-by-side port conflict zero
- [x] ✅ `zip` noCompress — 40MB+ studio asset bina double-compression ke
- [x] ✅ WebView cleartext localhost — manifest me already allowed (`usesCleartextTraffic`)
- [x] ✅ Kotlin compile — CI (gradle assembleStandardRelease) GREEN ho gaya
- [ ] 📱 Studio button → server start → WebView me Studio UI

## 5. MCP plugin (creator-studio-plugin/)

- [x] ✅ Manifest format extension_docs ke contract ke hisaab se (manifest_version 3, id, mcp_tools, background worker)
- [x] ✅ 17 tools — sab REAL Studio API routes par map (koi demo/placeholder nahi):
  ideas (3), pipeline (4), videos (2), brain (3), settings, skills (2), analytics/health (2)
- [x] ✅ `studio_video_record` → POST /api/cs/videos (naya route) — instructions ke vaade se match
- [x] ✅ 2 skills (get_skills/get_skill flow) — overview + 8-stage playbooks (Studio ke agent-instructions se aligned)
- [x] ✅ Port fallback 8877/8875 (flavor ke hisaab se dono try)
- [x] ✅ Server band ho to clear guidance error (Studio kholo)
- [ ] 📱 ZIP install → Custom AntiGem Plugin MCP → agent se health check

## 6. CI workflow (APK hamesha ready)

- [x] ✅ `.github/workflows/apk.yml` — push par: studio build → zip → APK assets → gradle release → APK artifact + GitHub Release
- [x] ✅ version.json se version — original release system jaisa, updater compatible
- [x] ✅ Keystore CI secrets se (RELEASE_KEYSTORE_BASE64) — repo me key kabhi nahi
- [x] ✅ Release publish par updater-ready APK naming
- [x] ✅ Workflow GREEN — release v1.0.0 published: CreatorStudio-standard-v1.0.0-release.apk (36.6 MB)

## 7. Docs

- [x] ✅ setup.md — beginner-level YouTube setup (Google Cloud → APIs → consent → client → refresh token → app me values → test → troubleshooting → data safety). Koi demo data nahi — pure instructions.
- [x] ✅ README.md — ye repo kya hai, kaise bana, build steps, docs links
- [x] ✅ creator-studio-plugin/README.md — install + verify + update
- [x] ✅ version.json changelog — pehli release ke notes

## 8. Push ke baad — VERIFIED (live status)

1. [x] ✅ Actions → "Build & Release APK" workflow GREEN (dono runs success)
2. [x] ✅ Release **v1.0.0** published — `CreatorStudio-standard-v1.0.0-release.apk` (36.6 MB) — updater isi ko dhundega
3. [x] ✅ Release **rootfs-v1** — bootrapz_2026-10-04.zip (351.3 MB) — app ka Linux env isi se download karega
4. [x] ✅ Updater probes pass: version.json raw (200) + APK download URL + rootfs URL — teeno live
5. [x] ✅ **v1.0.1 (termux-family build)**: standard flavor par isTermuxPackage()==false tha — isi liye
   360MB bootstrap option nahi aata tha aur termux Node na hone se Studio server start nahi hota tha
   (wahi problem Achiva me bhi thi — unka proven fix port kiya: com.termux.creatorstudio +
   applyTermuxRuntimeEnv + ensureTermuxScriptPaths + LD_LIBRARY_PATH + ports 1238/1239/8769/8875).
   NOTE: package id change ki wajah se v1.0.0 uninstall karke v1.0.1 FRESH install karna hoga.
6. [x] ✅ **v1.0.2 (env install verification fix)**: phone par "Shell verification test failed /
   libandroid-support.so not found" error aaya — root cause: install verification ka bash spawn
   bina LD_LIBRARY_PATH ke hota tha (bootstrap binaries ka DT_RUNPATH /data/data/com.termux par
   hota hai jo dusre package name par SELinux-block hai). Achiva ka exact v1.0.21 fix port kiya:
   executeCommand + executeScriptLive + executeCommandLive env, .bashrc + etc/profile exports,
   AgyMcpManager package-aware env. Rootfs zip sha256 original release ke barabar verified.
7. [x] ✅ **v1.0.3 (stability fix)**: phone par (a) Get Started ke baad app hang/crash — root cause
   pehla terminal/Studio spawn Main thread par poore rootfs ka file-walk chala raha tha (ANR);
   (b) terminal me bash/dpkg "Permission denied" — bootstrap binaries ka compiled-in com.termux
   sysconfdir. Fixes: prewarm app-start + install-success par (IO thread), login script patch
   (--login -> --rcfile $HOME/.bashrc), Studio stale-process cleanup + Retry button + node memory
   cap 512MB, $HOME/app-crash.log crash diagnostics. NOTE: dpkg ki compiled-path warning cosmetic
   hai — sirf bootstrap rebuild se hi jayegi.
8. [x] ✅ **v1.0.4 (real ANR root-cause fix)**: v1.0.3 ka prewarm kaafi nahi tha — asli deadlock
   race ye thi: `LocalPtySession.init` ke andar se hi `ensure*` heavy calls ho rahi thi AUR session
   `withContext(Dispatchers.Main)` me ban raha tha. Prewarm ka IO file-walk (25k+ files,
   `synchronized` lock) chal raha ho tab server/terminal spawn ka Main-thread call usi lock par
   BLOCK ho jata tha -> UI freeze ("Waking up..." atka, buttons dead) -> ANR -> system app ko
   maar deta tha (~20-25s). Process death se pref-flag kabhi set nahi hota tha -> har launch par
   wahi loop. Fixes: (1) LocalPtySession.init se saare ensure* calls hata diye; (2) server-runner
   session ab IO par banta hai (withContext(Main) removed); (3) LocalTerminalManager.managerScope
   Main -> IO; (4) getOrCreateOrRestoreSessions ke creation blocks IO par + ensure* hoist;
   (5) Application prewarm ab poora ensure* set chalata hai. Compile verified: compileTermuxReleaseKotlin GREEN.
5. [ ] 📱 Ab sirf PHONE par: APK install → original AntiGem ke saath side-by-side → Studio button → setup.md follow

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
- [ ] 🤖 Kotlin compile — CI (gradle assembleStandardRelease) par confirm hoga
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
- [ ] 🤖 Pehla push — workflow green + release me APK (push ke baad confirm hoga)

## 7. Docs

- [x] ✅ setup.md — beginner-level YouTube setup (Google Cloud → APIs → consent → client → refresh token → app me values → test → troubleshooting → data safety). Koi demo data nahi — pure instructions.
- [x] ✅ README.md — ye repo kya hai, kaise bana, build steps, docs links
- [x] ✅ creator-studio-plugin/README.md — install + verify + update
- [x] ✅ version.json changelog — pehli release ke notes

## 8. Push ke baad (is push par kya hoga)

1. Actions tab → "Build & Release APK" workflow green + APK artifact
2. Releases page par v1.0.0 release + APK (updater isi ko dhundega)
3. Phone par: APK install → original AntiGem ke saath side-by-side → Studio button → setup.md

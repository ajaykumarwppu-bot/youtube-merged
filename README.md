<div align="center">

# Creator Studio (YouTube Merged)

**AntiGem-based Android app — AI workspace + built-in YouTube Automation Creator Studio.**

AntiGem ka pura power (chat agents, Linux terminal, IDE, browser, notes) + floating switcher me ek **Studio** button jo pura Creator Studio web app local server par chalata hai: Idea Bank → 8-stage production pipeline → video records with full upload package → brain (skills, experiments, findings) → settings.

</div>

---

## Ye repo kya hai

- Base: [AntiGem](https://github.com/Santoshkurmi/antigem) (GPLv3) — apna independent fork, original repo se koi connection nahi (updater, rootfs, telemetry sab is repo se/nahi).
- **Alag app identity**: `com.termux.creatorstudio` (termux-family build — jaise AntiGem Achiva) — original AntiGem ke saath side-by-side, ports bhi alag (1238/1239/8769/8875).
- **Update-safe**: same signature + badhta versionCode → naya APK seedha update hoga, studio data (videos, brain, settings) preserve.

## App ke andar kya naya hai

| Part | Kya karta hai |
| --- | --- |
| `app/.../data/studio/StudioServerManager.kt` | APK assets me bundled `creator-studio.zip` ko `~/creator-studio/` me extract karke app ke Node se local server (port 8877) chalata hai. `data/` folder update par kabhi delete nahi hota. |
| `app/.../ui/studio/StudioScreen.kt` | Studio ka WebView UI (loading / error / ready states ke saath). |
| `AppViewMode.STUDIO` + floating switcher | Baki modes ki tarah ek persistent mode — switcher me SmartDisplay icon. |
| `creator-studio/` | Pura Creator Studio web app (Next.js standalone build CI me zip hota hai). Fresh install par **koi demo data nahi** — sirf built-in skill library. |
| `creator-studio-plugin/` | AntiGem MCP plugin — agent ko 17 `studio_*` tools + 2 skills deta hai (chat se poora pipeline chala sakta hai). |
| `setup.md` | YouTube (Data API v3 + Analytics API + OAuth) ka beginner-level setup guide. |
| `.github/workflows/apk.yml` | Push par APK build + GitHub Release — APK hamesha ready. |

## Build

```bash
# 1) Creator Studio web app build + APK assets me bundle
cd creator-studio && npm install && npm run build
cd .. && mkdir -p app/src/main/assets
cd creator-studio && zip -qr ../app/src/main/assets/creator-studio.zip .next/standalone package.json
cd ..

# 2) APK (standard flavor)
./gradlew :app:assembleStandardRelease   # keystore: app/release.jks (CI secret se)
# ya debug: ./gradlew :app:assembleStandardDebug
```

APK output: `app/build/outputs/apk/standard/release/CreatorStudio-standard-release.apk`
Releases: is repo ke **Releases** section me har push par built APK milega.

## Documentation

- **YouTube setup (beginner, step-by-step)** → [setup.md](setup.md)
- **Verification checklist** → [CHECKLIST.md](CHECKLIST.md)
- Plugin install/usage → [creator-studio-plugin/README.md](creator-studio-plugin/README.md)
- Extension/plugin system docs (AntiGem se) → [extension_docs/README.md](extension_docs/README.md)

## License

GPLv3 (base AntiGem ke saath) — LICENSE file dekho.

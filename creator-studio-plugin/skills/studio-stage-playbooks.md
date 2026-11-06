---
name: Stage playbooks
description: Creator Studio ke 8 production stages ka detail — har stage me kya karna hai, kaunsi skills, kaunse tools.
---

# Creator Studio stage playbooks (8 stages, fixed order)

## 1. research
- Topic ki deep research: trends, competitor videos (unke titles/tags dekho), facts + sources.
- Owner ke idea ki `details` (studio_ideas_list) ko core subject maano.
- Output: locked angle + facts. Brain ke purane videos findings se galtiyan avoid karo.
- Skills: `trend_research`, `audience_profile`.

## 2. packaging (script se PEHLE — packaging-first rule)
- 5 title candidates likho → sabse strong 1 final + `titleNote` (kis angle se likha).
- Thumbnail ka CONCEPT (composition, subject, 3-4 word text, emotion) — 2 options.
- Owner ko title + concepts dikhake confirm lo (channel ka public face hai).
- `studio_video_record {title, titleNote}` — draft record banao.
- Brain findings: thumbnail text 3-4 words max, title me concrete number/result/curiosity (generic titles forbidden).

## 3. script
- Hook word-by-word (long ≤15s, short ≤2s) — packaging wale promise par.
- Pura markdown script: `## HOOK (0:00-0:15)` format me — sections + timestamps.
- `studio_video_record {script}` + pipeline skills me `hinglish_scriptwriter` log karo.
- Owner ko summary dikhao, 'go' ke baad aage (jab tak owner ne faster mode na kaha ho).

## 4. voiceover
- Script se TTS voice generate (Settings ka TTS credential — `studio_settings_get` se check karo).
- Tone/pace format ke hisaab se (documentary = slow, shorts = energetic). Retakes nahi chahiye ho to stage done.

## 5. visuals
- Har script section ka matching visual: AI generation (google-flow tools) ya stock directory (Settings > Stock B-roll).
- Visual direction script stage se locked hai — usi se deviate mat karo.

## 6. edit
- Final cut + captions + music (Settings > Music directory, license notes) + pattern interrupts (20-30s me ek).
- Render lock. Render fail → `studio_run_retry` (media files preserve hain).

## 7. final_packaging
- Edit ke BAAD final thumbnail — video ke ACTUAL content se match (clickbait mismatch forbidden).
- 2 options (A/B) banao, owner choice confirm karo. Text mobile par readable ho.

## 8. seo_prep (last stage — run production-ready)
- Description: pehli 2 lines promise + keywords; phir sections summary; music credits.
- Tags/hashtags: research ke competitor keywords + contentLanguage ke search terms.
- Chapters (sirf long): script timestamps se match hone chahiye.
- endScreen + pinnedComment draft.
- Sab kuch ek saath: `studio_video_record {packaging: {title, titleNote, description, tags, hashtags, chapters, endScreen, pinnedComment}}`.
- YouTube Upload connected + autoUpload → owner ko 1 confirm (schedule/now); connected nahi → owner ko exact missing setup batao (Settings > YouTube Upload — Data API v3 OAuth; setup.md follow karo).
- `studio_stage_next` — run "ready" ho jata hai.

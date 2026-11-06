---
name: Run the Creator Studio video pipeline
description: YouTube Automation Creator Studio chalane ka pura process — idea se upload-ready video tak, studio_* tools aur stage rules ke saath.
---

# Creator Studio video pipeline (agent ka play-book)

Ye skill batati hai ki `studio_*` tools se ek video idea se upload-ready kaise banate hain.

## Pehli baar (har session me ek baar)

1. `studio_health` call karo — server chal raha hai? Error aaye to owner ko bolo Studio mode kholo (floating switcher me **Studio** button) aur ruk jao.
2. `studio_settings_get` — integrations dekho: YouTube connected? TTS key? `contentLanguage` aur `defaultKind` kya hai?
3. `studio_analytics_get` — channel ki current health dekho.

## Video banane ka pura flow

1. **Idea**: `studio_ideas_list` — owner ke ideas PEHLE. 1 idea ho to usi par shuru karo; 2-3 ho to owner se pucho (options de ke) kis par banana hai. Ideas khali hon to owner se puchh ke `studio_idea_add` karo.
2. **Start**: `studio_pipeline_start {ideaId}` — run banta hai, research active.
3. **Stages (order FIXED hai — jump MAT karo)**:
   research → packaging → script → voiceover → visuals → edit → final_packaging → seo_prep

   Har stage ka detailed play-book: `get_skill` se **studio_stage_playbooks** skill padho.
4. **Har stage ke baad**: `studio_stage_next {runId}` — active stage complete hoga, next active hoga. Response me `nextStageHint` milega.
5. **Fail hone par**: `studio_run_retry {runId}` — wahin se resume (media files preserve rehte hain).
6. **Data likhna**: jo bhi stage deliver kare wo `studio_video_record` me save karo (script stage par script, packaging par title+titleNote, seo_prep par packaging object).
7. **Seekhna**: kisi stage me kuch galat hua / naya pattern mila → `studio_brain_question_add` + relevant brain update. Purane findings har video me `studio_brain_get` se padho.

## Rules

- Stage order aur Studio ke gates ke against kabhi nahi — Studio validate karta hai.
- Owner ka approval: title (packaging), script summary, thumbnail final choice, upload (schedule/now) — ye 4 points par confirm lo. Baaki stages me rukna nahi.
- Skills: `studio_skill_list` / `studio_skill_get` se stage ki relevant skill padho (jaise hinglish_scriptwriter, seo_optimizer).
- Content language Settings ki `contentLanguage` follow kare (default Hinglish).
- Sab kuch `studio_video_record` me hi save hota hai — Studio Videos section me wahi dikhta hai (analytics, script, Upload Package).

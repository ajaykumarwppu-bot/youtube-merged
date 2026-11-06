# Creator Studio Bridge (AntiGem plugin)

Agent (chat) ↔ Creator Studio ka MCP bridge. Install ke baad agent ke paas
`studio_*` tools aa jaate hain — ideas, pipeline stages, video records, brain,
skills, analytics — sab Studio ke local server se real data par.

## Install (app me)

1. Is folder ko ZIP banao (manifest.json ZIP ke root me hona chahiye):
   ```
   cd creator-studio-plugin && zip -r ../creator-studio-plugin.zip .
   ```
2. AntiGem app → Browser/Plugins section → **Install ZIP** → `creator-studio-plugin.zip` select karo.
3. App MCP settings → **Custom AntiGem Plugin MCP** → `creator-studio` select karo.

## Verify

- Chat me agent se bolo: "studio health check karo" → `studio_health` call hoga.
- Studio mode band ho to tool error me guidance milegi (Studio kholo — server auto-start).

## Tools

17 tools: studio_health, studio_ideas_list, studio_idea_add, studio_idea_update,
studio_pipeline_list, studio_pipeline_start, studio_stage_next, studio_run_retry,
studio_video_list, studio_video_record, studio_brain_get, studio_brain_question_add,
studio_experiment_toggle, studio_settings_get, studio_skill_list, studio_skill_get,
studio_analytics_get.

Plus 2 skills (get_skills/get_skill): `creator-studio-overview`, `studio-stage-playbooks`.

## Update

version.json/worker.js/skills badlo → manifest me `version` bump karo → naya ZIP →
update (data/state preserve hoti hai).

// Creator Studio Bridge — worker
// Har studio_* tool Studio ke local server (Next.js, loopback) ko call karta hai.
// Server tabhi chalta hai jab app me Studio mode khula ho — error me wahi guidance.

const PORTS = [8877, 8875]; // standard flavor 8877, termux flavors 8875

function textResult(obj) {
  return { content: [{ type: "text", text: typeof obj === "string" ? obj : JSON.stringify(obj, null, 2) }] };
}

async function studioFetch(path, options = {}) {
  let lastErr = null;
  for (const port of PORTS) {
    try {
      const res = await fetch(`http://127.0.0.1:${port}${path}`, {
        ...options,
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
      });
      const raw = await res.text();
      let data;
      try {
        data = JSON.parse(raw);
      } catch {
        data = raw;
      }
      if (!res.ok) {
        const msg = data && typeof data.error === "string" ? data.error : `HTTP ${res.status}: ${String(raw).slice(0, 300)}`;
        throw new Error(msg);
      }
      return data;
    } catch (e) {
      lastErr = e;
      // network-level fail (connection refused) => agla port try karo
      if (e.cause) continue;
    }
  }
  throw new Error(
    `Creator Studio server reachable nahi hai (ports ${PORTS.join("/")}). ` +
      `Floating switcher me Studio button kholo — server auto-start hoga — phir dobara try karo. ` +
      `Detail: ${lastErr ? lastErr.message : "unknown"}`
  );
}

// advance/retry ke response ke saath stage playbook ka reminder — agent ko next kaam clear rahe
const STAGE_HINTS = {
  research: "RESEARCH stage: trends + competitor videos + facts — locked angle nikaalo. Skills: trend_research, audience_profile.",
  packaging: "PACKAGING stage: title (5 candidates → 1 final + titleNote) + thumbnail CONCEPT (2 options) — script se PEHLE lock. Brain ke thumbnail/title findings dekho.",
  script: "SCRIPT stage: hook word-by-word + pura markdown script (## HOOK format) — studio_video_record me script save karo. Visual direction bhi lock.",
  voiceover: "VOICEOVER stage: script se TTS voice (Settings ka TTS credential) — format ke hisaab se tone/pace.",
  visuals: "VISUALS stage: har script section ka matching visual/B-roll (AI generation ya stock directory se).",
  edit: "EDIT stage: final cut + captions + music + pattern interrupts — render lock.",
  final_packaging: "FINAL PACKAGING stage: edit ke BAAD final thumbnail — actual content se match, 2 options (A/B).",
  seo_prep: "SEO PREP stage: description + tags + hashtags + chapters (long) + endScreen + pinnedComment — studio_video_record ke packaging field me ek saath save karo.",
};

function withStageHint(run) {
  if (!run || !Array.isArray(run.stages)) return run;
  const active = run.stages.find((s) => s.state === "active");
  return { ...run, nextStageHint: active ? STAGE_HINTS[active.key] || null : null };
}

const tools = {
  async studio_health() {
    const d = await studioFetch("/api/cs/dashboard");
    return textResult({
      ok: true,
      summary: d.summary ?? d.totals ?? d,
      note: "Creator Studio server chal raha hai. Work flow: studio_ideas_list → owner ka idea → studio_pipeline_start → stages (playbook skill padho) → studio_stage_next … → seo_prep → upload.",
    });
  },

  async studio_ideas_list() {
    const d = await studioFetch("/api/cs/pipeline");
    return textResult({ ideas: d.ideas });
  },

  async studio_idea_add(args) {
    if (!args || typeof args.topic !== "string" || !args.topic.trim()) throw new Error("topic required");
    const d = await studioFetch("/api/cs/pipeline/ideas", {
      method: "POST",
      body: JSON.stringify({ topic: args.topic, details: typeof args.details === "string" ? args.details : "" }),
    });
    return textResult({ idea: d.idea ?? d });
  },

  async studio_idea_update(args) {
    if (!args || typeof args.id !== "string") throw new Error("id required");
    const patch = {};
    if (typeof args.topic === "string") patch.topic = args.topic;
    if (typeof args.details === "string") patch.details = args.details;
    if (typeof args.status === "string") patch.status = args.status;
    const d = await studioFetch(`/api/cs/pipeline/ideas/${encodeURIComponent(args.id)}`, {
      method: "PATCH",
      body: JSON.stringify(patch),
    });
    return textResult({ idea: d.idea ?? d });
  },

  async studio_pipeline_list() {
    const d = await studioFetch("/api/cs/pipeline");
    return textResult({ runs: d.runs });
  },

  async studio_pipeline_start(args) {
    if (!args || typeof args.ideaId !== "string") throw new Error("ideaId required");
    const d = await studioFetch(`/api/cs/pipeline/ideas/${encodeURIComponent(args.ideaId)}/start`, { method: "POST" });
    return textResult({ idea: d.idea, run: withStageHint(d.run) });
  },

  async studio_stage_next(args) {
    if (!args || typeof args.runId !== "string") throw new Error("runId required");
    const d = await studioFetch(`/api/cs/pipeline/runs/${encodeURIComponent(args.runId)}/advance`, { method: "POST" });
    return textResult({ run: withStageHint(d.run ?? d) });
  },

  async studio_run_retry(args) {
    if (!args || typeof args.runId !== "string") throw new Error("runId required");
    const d = await studioFetch(`/api/cs/pipeline/runs/${encodeURIComponent(args.runId)}/retry`, { method: "POST" });
    return textResult({ run: withStageHint(d.run ?? d) });
  },

  async studio_video_list() {
    const d = await studioFetch("/api/cs/videos");
    return textResult(d);
  },

  async studio_video_record(args) {
    if (!args || typeof args.title !== "string" || !args.title.trim()) {
      throw new Error("title required (video ka final title)");
    }
    const d = await studioFetch("/api/cs/videos", { method: "POST", body: JSON.stringify(args) });
    return textResult(d);
  },

  async studio_brain_get() {
    const d = await studioFetch("/api/cs/brain");
    return textResult(d);
  },

  async studio_brain_question_add(args) {
    if (!args || !Array.isArray(args.areas) || args.areas.length === 0) {
      throw new Error("areas required (jaise: ['thumbnail','ctr'])");
    }
    const body = { areas: args.areas };
    if (typeof args.text === "string") body.text = args.text;
    if (typeof args.videoId === "string") body.videoId = args.videoId;
    const d = await studioFetch("/api/cs/brain/questions", { method: "POST", body: JSON.stringify(body) });
    return textResult(d);
  },

  async studio_experiment_toggle(args) {
    if (!args || typeof args.experimentId !== "string" || typeof args.enabled !== "boolean") {
      throw new Error("experimentId + enabled required");
    }
    const body = {};
    if (typeof args.setAll === "boolean") body.setAll = args.setAll;
    if (typeof args.itemId === "string") body.itemId = args.itemId;
    body.enabled = args.enabled;
    const d = await studioFetch(`/api/cs/brain/experiments/${encodeURIComponent(args.experimentId)}`, {
      method: "PATCH",
      body: JSON.stringify(body),
    });
    return textResult(d);
  },

  async studio_settings_get() {
    const d = await studioFetch("/api/cs/settings");
    return textResult(d);
  },

  async studio_skill_list() {
    const d = await studioFetch("/api/cs/packages");
    return textResult({
      packages: (d.packages || []).map((p) => ({
        id: p.id,
        name: p.name,
        version: p.currentVersion,
        skills: (p.skills || []).map((s) => ({ name: s.name, version: s.version, enabled: s.enabled })),
      })),
    });
  },

  async studio_skill_get(args) {
    if (!args || typeof args.id !== "string") throw new Error("id required");
    const path = typeof args.path === "string" && args.path ? `?path=${encodeURIComponent(args.path)}` : "";
    const d = await studioFetch(`/api/cs/packages/${encodeURIComponent(args.id)}/raw${path}`);
    return textResult(d);
  },

  async studio_analytics_get() {
    const d = await studioFetch("/api/cs/dashboard");
    return textResult(d);
  },
};

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type !== "mcp_tool_call") return;
  (async () => {
    const fn = tools[message.tool];
    if (!fn) throw new Error(`Unknown tool: ${message.tool}`);
    return fn(message.args || {});
  })().then(sendResponse, (error) =>
    sendResponse({ isError: true, content: [{ type: "text", text: error.message }] })
  );
  return true;
});

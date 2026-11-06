# Creator Studio — YouTube Setup Guide (Beginner ke liye, Step-by-Step)

Ye guide unke liye hai jisne pehle kabhi Google Cloud / YouTube API setup nahi kiya.
Har click, har field, har test — sab kuch yahan likha hai. Bas upar se neeche follow karo.

App ke andar YouTube ka connection **2 hisson** me kaam karta hai:

| Hissa | Kaam | Kya chahiye |
| --- | --- | --- |
| **YouTube Upload** (Settings) | Video upload/schedule karna, video list read karna | OAuth Client ID + Client Secret + Refresh Token |
| **YouTube Analytics (Data Read)** (Settings) | Views, CTR, retention, subscribers ka data | Same 3 values (dono APIs ek hi client se authorize hoti hain) |

Ek baar setup ho gaya, to dono boxes me same values jaayengi — dobara naya client banane ki zaroorat nahi.

**Time lagakar:** ~20-30 minute (sirf pehli baar).

---

## STEP 1 — Google Cloud Project banao (free)

1. Browser me kholo: **https://console.cloud.google.com**
2. Apni Google account se sign in karo (wahi jis par channel hai — channel owner account).
3. Upar left me project selector dikhega (dropdown jisme "Select a project" likha hota hai) → click karo.
4. Dialog khulega → upar right me **NEW PROJECT** button → click.
5. **Project name** me kuch bhi likho, jaise: `creator-studio-yt`
   - Location/Billing: kuch mat chhedo (No organization theek hai; billing ki zaroorat NAHI).
6. **CREATE** → 20-30 second me ban jayega.
7. Notification bell (top-right) se project banne ka pata chalega → **SELECT PROJECT** click karo
   (ya upar wale dropdown se naya project select karo).
   - Ab screen ke upar hamesha yahi project ka naam dikhna chahiye — agla sab isi project me hoga.

---

## STEP 2 — Dono YouTube APIs enable karo

Ek YouTube connection ke liye **2 APIs** chahiye — dono isi project me on karni hain:

1. Left sidebar (☰ menu) → **APIs & Services** → **Library**.
2. Search box me likho: `YouTube Data API v3` → result me click karo → neela **ENABLE** button → click.
   (Enable hone par "Enable" ki jagah "Manage" dikhne lagega — matlab ho gaya.)
3. Wapas **Library** → search: `YouTube Analytics API` → click → **ENABLE**.
   - Ye alag API hai — Data API sirf upload/read karti hai, Analytics API channel ke
     views/CTR/retention metrics deti hai. Dono chahiye.
4. Quota ka note (aage kaam aayega): YouTube Data API free quota = **10,000 units/day**.
   Ek video upload ≈ 1,600 units — matlab ~6 uploads/day free me. Analytics API ka alag quota hai.

---

## STEP 3 — OAuth Consent Screen set karo

Ye wahi screen hai jo sign-in karte waqt dikhti hai ("Creator Studio ko ye permissions chahiye").

1. Left sidebar → **APIs & Services** → **OAuth consent screen**.
2. **Get started** / User Type pucha to: **External** select karo → **CREATE**.
3. **App information** form:
   - App name: `Creator Studio`
   - User support email: apna email select karo
   - **App logo**: skip kar sakte ho
   - **App domains**: skip (optional)
   - **Authorized domains**: skip (optional)
   - Developer contact information: apna email → **SAVE AND CONTINUE**
4. **Scopes** screen: kuch add karne ki zaroorat nahi abhi (agle step me client banega) → **SAVE AND CONTINUE**.
5. **Test users** screen (IMPORTANT — isko skip mat karo):
   - **ADD USERS** → apna Gmail (jiska channel hai) likho → **ADD** → **SAVE AND CONTINUE**.
   - Test user na ho to sign-in par "Access blocked" aayega — ye step hi wo problem solve karta hai.
6. **Summary** dikhega → **BACK TO DASHBOARD**.

> **Zaroori baat (honest note):** YouTube upload scope Google ki "restricted/sensitive" category me aata hai.
> Jab tak app "Testing" mode me hai (jo personal use ke liye sahi hai), **refresh token ~7 din baad expire**
> ho sakta hai aur sign-in dobara karna padega (STEP 6 dobara — sirf 2 minute lagte hain). Production
> verification (Google ka review process) sirf zaroorat padne par hi karna — personal use me testing mode hi chalega.

---

## STEP 4 — OAuth Client ID + Secret banao

1. Left sidebar → **APIs & Services** → **Credentials**.
2. Upar **+ CREATE CREDENTIALS** → **OAuth client ID**.
3. **Application type**: `Web application` select karo.
   (Phone app wala "Android" type NAHI — Studio token exchange web-flow karta hai.)
4. **Name**: `creator-studio-web` (koi bhi naam chalega).
5. **Authorized redirect URIs** — yahan 2 URIs add karo (**+ ADD URI** se ek-ek karke):
   - `https://developers.google.com/oauthplayground`
   - `http://localhost:51121/oauth-callback`
     (pehla — STEP 5 me refresh token lene ke liye; doosra — app ke internal sign-in flow ke liye)
6. **CREATE** → popup me **Client ID** aur **Client Secret** dikhenge:
   - Client ID: `xxxxx.apps.googleusercontent.com` jaisa lamba string
   - Client Secret: `GOCSPX-...` se shuru hota hai
   - Dono copy kar lo (ya popup band karke baad me Credentials list me client par click karke dekho).

---

## STEP 5 — Refresh Token nikalo (ek baar, 5 minute)

Refresh token wo "long-lived key" hai jisse app har baar password ke bina YouTube se baat kar pati hai.
Iske liye Google ka official **OAuth 2.0 Playground** tool use karenge:

1. Browser me kholo: **https://developers.google.com/oauthplayground**
2. Right-top me ⚙️ (gear icon) → check karo: **"Use your own OAuth credentials"** ✔️ tick karo.
   - Client ID aur Client Secret ke boxes khulenge → STEP 4 wale paste karo.
3. Left side **Step 1** me scopes list me ye 3 scopes ek-ek karke select karo (type karke bhi mil jaate hain):
   - `https://www.googleapis.com/auth/youtube.upload`
   - `https://www.googleapis.com/auth/youtube.readonly`
   - `https://www.googleapis.com/auth/yt-analytics.readonly`
4. **Authorize APIs** (neela button) → Google ka sign-in page khulega:
   - Apna channel-wala account choose karo.
   - "Google hasn't verified this app" warning aayegi — ye TUMHARA hi app hai → **Continue** / **Advanced → Go to Creator Studio (unsafe)** → Continue.
   - Saare permission checkboxes par tick karke **Allow** karo.
5. **Step 2** par aa jaoge → **Exchange authorization code for tokens** button → click.
   - Response me `"refresh_token"` field dikhega → **wo copy kar lo** (lambi string).
   - (Access token ki zaroorat nahi — sirf refresh_token chahiye.)

> **Problem:** "Token exchange" par `redirect_uri_mismatch` aaye to STEP 4.5 me
> `https://developers.google.com/oauthplayground` URI exactly waise hi add hai ya nahi dekho (spelling/`https://`).

---

## STEP 6 — App me values daalo

1. App kholo → floating switcher me **Studio** button (video icon) → Creator Studio khulega.
2. Neeche **Settings** tab → **Integrations** me do boxes milenge:
   - **YouTube Upload** → kholo → 3 fields:
     - Client ID: STEP 4 wala Client ID
     - Client Secret: STEP 4 wala Secret
     - Refresh Token: STEP 5 wala refresh token
     → **Save**. Status "Connected" dikhna chahiye.
   - **YouTube Analytics (Data Read)** → SAME 3 values paste karo → **Save**.
3. Values masked dikhti hain (•••• last 4) — ye normal hai; poori value sirf device par save hoti hai.

---

## STEP 7 — Test karo (har cheez kaam kar rahi hai ya nahi)

**Test 1 — Analytics data:**
- Studio → **Home** tab khula hai? Data boxes (totals/health) "connected" state dikhate hain?
- Fresh setup par data khali hoga — ye SAHI hai. Kaam kar raha hai ye check karne ke liye
  Settings > YouTube Analytics ka status "Connected" hona kaafi hai.

**Test 2 — Upload (safe tarika):**
- Studio → **Pipeline** → koi bhi idea par run ready karo (ya agent se banwao) →
  upload test ke waqt video ki visibility **unlisted/private** rakho (agent ko bol do).
- Upload hote hi video **Videos** section me "Uploaded" status ke saath dikhegi.

**Test 3 — Agent ke through:**
- Chat me agent ko bolo: *"studio health check karo"* → agent `studio_health` tool se jawab dega.
- *"Pipeline ke ideas dikhao"* → `studio_ideas_list` chalega.
- (Plugin install hona chahiye — neeche "Agent ka setup" section dekho.)

**Kuch bhi fail ho to** neeche Troubleshooting section padho.

---

## STEP 8 — Agent ka setup (chat se automation)

1. Plugin ZIP banao: repo me `creator-studio-plugin/` folder ko zip karo
   (zip ke ANDAR seedhe `manifest.json` ho — folder ke andar nahi).
2. App → **Plugins** section → **Install ZIP** → zip select karo → enabled ho jayega.
3. App MCP settings → **Custom AntiGem Plugin MCP** → **creator-studio** select karo.
4. Ab agent ke paas 17 `studio_*` tools + 2 playbook skills hain. Agent khud
   `get_skills` se playbooks padhta hai — use bolo "pipeline chalao" bas.

---

## Troubleshooting (har common error + fix)

| Error / Symptom | Matlab | Fix |
| --- | --- | --- |
| `403 accessNotConfigured` | API enable nahi hui us project me | STEP 2 dubara dekho — dono APIs enabled? wahi project selected hai? |
| `401 invalid_client` | Client ID/Secret galat paste hua | STEP 6 me dono values dubara paste karo (extra space na ho) |
| `redirect_uri_mismatch` | Redirect URI client se match nahi | STEP 4.5 wale 2 URIs EXACTLY waise hi hain? (http/https, port, path) |
| `Access blocked: app has not completed verification` | App Testing mode me hai aur tum test user nahi ho | STEP 3.5 me apna Gmail test users me add karo |
| `invalid_grant` sign-in par | Refresh token expire/m revoked | STEP 5 dubara karo (2 min) — naya refresh token paste karo |
| `403 dailyLimitExceeded` | Aaj ka 10,000 quota units khatam | Kal try karo, ya Google Cloud console → APIs → Quotas se increase request |
| `403 forbidden` upload par | Channel par upload permission nahi mili | STEP 5 me `youtube.upload` scope tick karna miss ho gaya — re-authorize |
| Studio button par "server start nahi hua" | Node env ready nahi | Pehle app ka Local Environment setup karo (Settings), phir Studio dubara kholo |
| Upload ke baad video nahi dikh rahi | Processing delay | YouTube Studio (site) me check karo — 2-5 min lag sakte hain |

---

## Data kahan rehta hai (privacy + update safety)

- **Sab kuch tumhare phone par**: Studio data `~/creator-studio/data/` (app ke private storage) me
  JSON files ke roop me — videos, brain, settings, skills. Koi server par kuch nahi jata.
- API keys device ke andar hi save hoti hain; Studio UI par sirf masked dikhti hain.
- **APK update karo to data delete NahI hota** — naya APK install karo, sab waisa hi milega.
- Telemetry/analytics: disabled. Koi usage data kahin nahi bheja jata.

## Roz ka use (cheat-sheet)

- Naya video: Studio → Pipeline → Idea Bank me idea (ya agent se) → run start → stages complete → seo_prep → upload
- Agent se: chat me bolo *"is idea par video banao"* → agent studio_* tools se poora flow chalayega
- Analytics dekhna: Studio → Home (summary) / Videos → video kholo (pura data + Upload Package)

# Google Play Listing Kit — Block Puzzle: Brain Blast (v2.1)

Everything you need to fill in the Play Console forms.

## 0. Account & testing requirements (read first!)

- Google Play developer account costs **$25 one-time** (play.google.com/console).
- **Personal accounts created after Nov 2023 must run a closed test with at
  least 12 testers for 14 days** before they can publish to production.
- **Privacy policy URL** is required: deploy the `web/` folder and use `https://your-domain.com/privacy.html`.
- **Developer website** in the listing must be the same domain that serves `app-ads.txt` (see §6).

## 1. Store listing text (keyword-optimised)

**App name (30 max) — 25 chars:**
```
Block Puzzle: Brain Blast
```

**Short description (80 max) — 75 chars:**
```
Relaxing block puzzle brain game! Clear lines, blast combos & play offline.
```
Alternative to A/B test later (79 chars):
```
Fun block puzzle brain game: clear lines, chain combos and beat your best score
```

**Full description (4000 max):**
```
🧠💥 Block Puzzle: Brain Blast — the colorful block puzzle brain game that is easy to learn and impossible to put down!

Drag glowing blocks onto the 8×8 board, fill complete rows or columns to blast them away, and chain clears together for massive combos. No timer, no pressure — just a relaxing, satisfying puzzle that trains your brain one move at a time.

🧩 HOW TO PLAY
• Drag a block from the tray onto the board (or tap a block, then tap the board)
• Fill a full row or column to clear it
• Clear several lines with one block for huge bonus points
• Keep clearing within 3 moves to grow your Combo multiplier
• The game ends when no block fits — then beat your best score!

🎮 THREE WAYS TO PLAY
• Classic — the endless, relaxing block puzzle. Chase your high score; your game saves automatically.
• Adventure — level after level of jewel puzzles. Clear lines holding rubies, sapphires and emeralds to collect them.
• Daily Challenge — a fresh board every day. Hit the target score and keep your daily streak alive.

✨ WHY PLAYERS LOVE IT
✔ Smooth drag & drop with smart snapping — blocks land exactly where you mean
✔ Glowing line preview shows what will clear before you drop
✔ Easy Mode: smarter blocks that always fit, hints and a quick tutorial
✔ Power-ups: Hint, Undo, New Blocks and Bomb to rescue a tricky board
✔ Second Chance revive when you run out of space
✔ "Good! Great! Excellent!" praise, combo bursts and All Clear celebrations
✔ 16 awards, daily gifts and streak rewards
✔ Relaxing music, satisfying sounds and vibration feedback (all optional)
✔ Play offline — no Wi-Fi needed
✔ Small download, runs smoothly on any phone

🧠 GREAT FOR YOUR BRAIN
Block Puzzle: Brain Blast is a perfect brain game for all ages. Plan ahead, think in shapes and sharpen your logic, focus and memory — whether you have two minutes on the bus or an hour to relax at home.

Download Block Puzzle: Brain Blast and start blasting lines today! 💥

Questions or ideas? Email us at supportbreakouttrade@gmail.com
```

**Contact details:**
- Email: `supportbreakouttrade@gmail.com`
- Website: `https://your-domain.com`
- Privacy policy: `https://your-domain.com/privacy.html`

## 2. Graphics (files in `play-store-assets/`)

| Asset | Requirement | File |
|---|---|---|
| App icon | 512×512 PNG | ✅ `icon-512-brainblast.png` (your logo, corners filled so Google's rounding is clean) |
| Feature graphic | 1024×500 | ✅ `feature-graphic-brainblast-1024x500.png` |
| Phone screenshots | 2–8, 9:16 | 📱 take on your phone (below) |

`icon-512.png` / `feature-graphic-1024x500.png` are the old v1 jewel art — don't upload them.

**Screenshots that convert** (add a short caption on top of each):
1. A glowing line about to clear — "Blast lines, chain combos!"
2. The Combo burst — "Satisfying combos & effects"
3. Adventure level with jewels — "Collect jewels in Adventure"
4. Home menu with daily streak — "New Daily Challenge every day"
5. "New Best Score!" screen — "Train your brain, beat your best"
6. Board in airplane mode — "Works offline"

Graphics must not contain "free", "#1", "best", "top", "download now" / "play now" style calls to action, or other games' names.

## 3. Console form answers

| Form | Answer |
|---|---|
| App or game | Game |
| Category | Puzzle |
| Tags | Block puzzle, Brain games, Offline, Casual, Relaxing |
| Free or paid | Free |
| **Contains ads** | **Yes** |
| In-app purchases | No |
| Target audience | **13+** (keeps Families policy and child-directed ad rules out of scope) |
| Content rating | No violence, sex, language, drugs or gambling; no user-to-user interaction → "Everyone / 3+" |
| Permissions | Internet, network state (ads), Vibrate (haptics), Advertising ID (added by AdMob) |
| Advertising ID declaration | **Yes**, used for **Advertising or marketing** |
| App access | All functionality available without login |

### Data safety (with AdMob)

Declare these as **collected and shared** (by the Google Mobile Ads SDK), **processed ephemerally: no**,
**encrypted in transit: yes**, **users can request deletion: no** (you hold none of it):

| Data type | Purpose |
|---|---|
| Device or other IDs (advertising ID) | Advertising or marketing, Analytics, Fraud prevention |
| Approximate location (from IP) | Advertising or marketing, Analytics, Fraud prevention |
| App interactions (ad views / taps) | Advertising or marketing, Analytics |
| Crash logs & diagnostics | Analytics |

Data is **not** used for account management and the game itself stores scores only on the device.
Re-check Google's current "Google Mobile Ads SDK – Data disclosure" page when you fill this in.

## 4. AdMob setup (one-time, in the AdMob console)

1. The app ID and 5 ad units are already wired in (`app/src/main/res/values/ads.xml` and the manifest).
2. **Privacy & messaging → GDPR message**: create and publish a consent message for this app.
   The app shows it automatically to EEA/UK/Swiss users, and adds **Settings → Privacy & Ad Choices** for them.
3. **App settings → app-ads.txt**: deploy `web/` (it contains `app-ads.txt`) and make sure the Play
   listing's website is that same domain. AdMob verifies it within ~24 h of the app going live.
4. After the Play listing is live, **link the app to the store** in AdMob (Apps → App settings).
5. **All builds use the live units.** Before testing on your own phone, add it to `ad_test_devices` in
   `app/src/main/res/values/ads.xml` (ID from Logcat, search `setTestDeviceIds`) so it only gets "Test Ad"
   versions. Never tap your own live ads — it gets AdMob accounts banned.

### How the ads are placed (already implemented)

| Unit | Where | Why it's player-friendly |
|---|---|---|
| Banner | Bottom of the game screen, in its own space | Never covers the board; hidden on the menu, result screen and tutorial |
| Interstitial | Only between games (after the result screen) | Not for the first 3 games, max once every 2.5 min and every 2 games, not right after a video |
| Native | Result screen, below the score | Replaces the banner there; clearly marked "Ad" |
| Rewarded | "Revive" when out of space (once per game) and "free power-up" when you run out | Always optional, player chooses to watch |
| Rewarded interstitial | Bonus power-ups after an Adventure / Daily win | 5-second countdown with "No thanks" before it starts |

## 5. Publishing checklist (in order)

- [ ] Create Play Console account ($25)
- [ ] Run `sync.ps1 -SiteUrl https://your-domain.com`, deploy `web/` (privacy policy + app-ads.txt)
- [ ] AdMob: publish the GDPR message
- [ ] Build signed `.aab` (see README-BUILD-APK.md) — version 2.1 (versionCode 3)
- [ ] Console → Create app → "App content": privacy policy, **ads = yes**, advertising ID, data safety, target audience 13+, content rating
- [ ] Upload store listing text + `icon-512-brainblast.png` + `feature-graphic-brainblast-1024x500.png` + screenshots
- [ ] Closed testing with 12+ testers for 14 days → apply for production → submit
- [ ] After approval: link the app in AdMob, set `PLAY_STORE_URL` in `block-puzzle.html`, re-run `sync.ps1`

## 6. Ranking & downloads (what actually moves the needle)

- **Keywords:** title + short + full description are indexed. "block puzzle", "brain game", "combo",
  "offline", "jewel" and "daily challenge" appear naturally — don't stuff.
- **Policy traps:** never use other games' names (including similar-sounding brands) in the listing,
  and no "free/#1/best/top/new" in the name or graphics.
- **Ratings:** the app asks for a rating with Google's in-app review prompt after a new best score
  (from the 3rd game, max every 20 days). Never buy or incentivise reviews.
- **Retention** (daily streak, gifts, auto-save, Adventure levels) and **low ad annoyance** are built in —
  Play ranks apps people keep opening and don't uninstall.
- **Update regularly** and later localise the listing (Hindi, Spanish, Portuguese, Indonesian).

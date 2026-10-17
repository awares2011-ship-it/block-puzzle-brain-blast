# Web launch & SEO checklist — Block Puzzle: Brain Blast

## Project layout

| Path | What it is |
|---|---|
| `block-puzzle.html` | **Source** of the game (edit this one) |
| `privacy.html` | **Source** of the privacy policy |
| `sync.ps1` | Builds `web/` and copies the game into both Android projects |
| `web/` | Ready-to-deploy website (index, privacy, manifest, service worker, icons, OG image, robots, sitemap, **app-ads.txt** for AdMob) |

## Already done in the code

- Title, meta description, canonical, robots, Open Graph + Twitter cards with a 1200×630 share image
- JSON-LD structured data: `WebSite`, `VideoGame`/`WebApplication` (free offer), `FAQPage`
- Real crawlable content under the menu: How to Play, Game Modes, Tips, Features, FAQ (one `<h1>`, proper `<h2>`s)
- Installable PWA: manifest, maskable icons, offline service worker, "Install App" button
- Fast: one inline HTML file, no frameworks, font loaded non-blocking, canvas renderer that idles when nothing moves
- Mobile-friendly: responsive layout, safe-area support, 48px tap targets, zoom not disabled
- Accessible: keyboard play (1/2/3 pick, arrows move, Enter place, H hint, Z undo), ARIA labels, reduced-motion support

## You need to do (in order)

1. **Pick a domain / host.** Free options: Cloudflare Pages, Netlify, GitHub Pages. A short `.com` with
   "block puzzle" in it helps a little; a custom domain beats a `*.github.io` subdomain long-term.
2. The support email (supportbreakouttrade@gmail.com) is already in the game, footer and privacy policy.
3. Build with your real URL:
   ```
   powershell -ExecutionPolicy Bypass -File .\sync.ps1 -SiteUrl https://your-domain.com
   ```
4. Deploy the **contents** of `web/` to the site root (HTTPS is required for install + offline).
   Check that `https://your-domain.com/app-ads.txt` opens — AdMob needs it, and the Play listing's
   website field must be this same domain.
5. **Google Search Console** → add the property → submit `https://your-domain.com/sitemap.xml` →
   URL Inspection → "Request indexing". Do the same in **Bing Webmaster Tools** (also feeds DuckDuckGo/Yahoo).
6. Check: [PageSpeed Insights](https://pagespeed.web.dev/) (aim for 90+), the
   [Rich Results Test](https://search.google.com/test/rich-results), and share the link once in a chat app
   to confirm the preview image shows.

## Getting players & links (the part that ranks you)

- **Game portals** bring both players and backlinks: itch.io (HTML5 upload of `web/`), CrazyGames,
  Poki (apply as developer), GameDistribution, Newgrounds, Kongregate. Link back to your domain from each.
- Post short gameplay clips (combo bursts, "All Clear!") to YouTube Shorts / Instagram Reels / TikTok
  with the link in the bio — short videos are the biggest free acquisition channel for casual puzzles.
- Every result screen has a **Share** button; on Android it shares your Play Store link automatically.
- After the Play Store listing is live, set `PLAY_STORE_URL` in `block-puzzle.html` and re-run `sync.ps1`
  to show a "Get it on Google Play" button on the website.
- Add new pages over time (e.g. "Block puzzle strategy guide", "How combos work") — each targets
  more searches and links back to the game.

## Re-deploying an update

Edit `block-puzzle.html` → run `sync.ps1 -SiteUrl ...` → upload `web/` again. The service worker fetches
the page network-first, so players get the new version on their next visit.

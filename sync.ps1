# Builds the web site (web/) and refreshes the Android app assets from the source files.
#
#   powershell -ExecutionPolicy Bypass -File .\sync.ps1                                  # placeholders kept
#   powershell -ExecutionPolicy Bypass -File .\sync.ps1 -SiteUrl https://mygame.example  # real domain
#
# Source of truth: block-puzzle.html and privacy.html in this folder. Never edit web/index.html directly.
param([string]$SiteUrl = '')

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$placeholder = 'https://YOUR-DOMAIN.com/'
$utf8 = New-Object System.Text.UTF8Encoding($false)

$url = $placeholder
if ($SiteUrl) {
    $url = $SiteUrl.Trim()
    if (-not $url.EndsWith('/')) { $url += '/' }
    if ($url -notmatch '^https://') { throw "SiteUrl must start with https:// (got '$SiteUrl')" }
}

function Build-Page([string]$name) {
    $text = [IO.File]::ReadAllText((Join-Path $root $name), $utf8)
    return $text.Replace($placeholder, $url)
}

$game = Build-Page 'block-puzzle.html'
$privacy = Build-Page 'privacy.html'

$web = Join-Path $root 'web'
[IO.File]::WriteAllText((Join-Path $web 'index.html'), $game, $utf8)
[IO.File]::WriteAllText((Join-Path $web 'privacy.html'), $privacy, $utf8)

$today = Get-Date -Format 'yyyy-MM-dd'
$robots = "User-agent: *`nAllow: /`n`nSitemap: ${url}sitemap.xml`n"
$sitemap = @"
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url><loc>$url</loc><lastmod>$today</lastmod><changefreq>weekly</changefreq><priority>1.0</priority></url>
  <url><loc>${url}privacy.html</loc><lastmod>$today</lastmod><changefreq>yearly</changefreq><priority>0.3</priority></url>
</urlset>
"@
[IO.File]::WriteAllText((Join-Path $web 'robots.txt'), $robots, $utf8)
[IO.File]::WriteAllText((Join-Path $web 'sitemap.xml'), $sitemap, $utf8)
# AdMob app-ads.txt: must be reachable at https://your-domain.com/app-ads.txt (the website listed in Play Console).
[IO.File]::WriteAllText((Join-Path $web 'app-ads.txt'), "google.com, pub-1607968585289432, DIRECT, f08c47fec0942fa0`n", $utf8)
Write-Host "web/ updated  ->  $url"

$assetDirs = @('android-app\app\src\main\assets', 'android\app\src\main\assets')
foreach ($rel in $assetDirs) {
    $dir = Join-Path $root $rel
    if (-not (Test-Path $dir)) { continue }
    [IO.File]::WriteAllText((Join-Path $dir 'index.html'), $game, $utf8)
    [IO.File]::WriteAllText((Join-Path $dir 'privacy.html'), $privacy, $utf8)
    $iconDir = Join-Path $dir 'icons'
    New-Item -ItemType Directory -Force $iconDir | Out-Null
    foreach ($icon in @('icon-192.png', 'favicon-48.png')) {
        Copy-Item (Join-Path $web "icons\$icon") (Join-Path $iconDir $icon) -Force
    }
    Write-Host "$rel updated"
}

if ($url -eq $placeholder) {
    Write-Warning "No -SiteUrl given: canonical/OG/sitemap links still point to $placeholder. Re-run with your real domain before deploying."
}

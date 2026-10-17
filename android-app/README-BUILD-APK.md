# Build Your APK / AAB — Step by Step

You already have Android Studio installed, so you're ~15 minutes from a signed release file.

## 1. Open the project

1. Open Android Studio → **File → Open** → select this `android-app` folder.
2. Wait for Gradle sync to finish (first time downloads dependencies, needs internet).
   - If Studio asks about Gradle wrapper/version, accept the defaults.
   - If it suggests upgrading the Android Gradle Plugin, accept.

## 2. Test it on a device (optional but recommended)

1. Plug in your Android phone with USB debugging enabled (or use the emulator).
2. Press the green **Run ▶** button.
3. The game should launch fullscreen with the splash screen.

## 3. Create your signing key (one time only)

1. **Build → Generate Signed App Bundle / APK…**
2. Choose **Android App Bundle** (Google Play requires `.aab` for new apps).
3. Under Key store path click **Create new…**
   - Save it somewhere safe, e.g. `D:\keys\blockpuzzle.jks`
   - Set passwords and fill in your name. **Validity: 25+ years.**
4. ⚠️ **BACK UP THIS FILE AND PASSWORDS.** If you lose the keystore you can
   never update your app again.

## 4. Build the release file

1. Continue the wizard: select **release** → **Create**.
2. Output appears at: `app/release/app-release.aab`
3. That `.aab` file is what you upload to Google Play.

Need a plain APK for sharing/testing? Same wizard but choose **APK** instead —
output at `app/release/app-release.apk`.

## 5. Before each future update

In `app/build.gradle` increase:
```
versionCode 4      // +1 every upload (currently 3)
versionName "2.2"  // what users see (currently 2.1)
```

## Project facts (useful for Play forms)

| Item | Value |
|---|---|
| App name | Block Puzzle: Brain Blast |
| Application ID | `com.nivrutti.blockpuzzlejewel` (keep it — it can never change after publishing) |
| Min Android | 8.0 (API 26) |
| Target SDK | 35 |
| Permissions | Internet + network state (ads), Vibrate (haptics), Advertising ID (merged by AdMob) |
| Ads | Google AdMob — banner, interstitial, native, rewarded, rewarded interstitial |
| Consent | Google UMP (GDPR message for EEA/UK/CH, privacy options in Settings) |
| In-app purchases | None |
| Libraries | `play-services-ads`, `user-messaging-platform`, Play In-App Review |

## Ads: live units + protecting your own phone

- **Every build** (debug and release) uses your **live** ad units from `app/src/main/res/values/ads.xml`.
- **Never tap live ads on your own phone** — AdMob treats it as invalid traffic and can ban the account.
  Protect yourself once:
  1. Run the app on your phone, open **Logcat** in Android Studio and search `setTestDeviceIds`.
  2. Copy the quoted ID from that line into `<string-array name="ad_test_devices">` in `ads.xml`
     (or add the phone in AdMob → Settings → Test devices).
  3. Run again: your phone now shows the same ads labelled **"Test Ad"** and is safe to tap.
     Everyone else sees normal live ads.
- **Blank ads at first are normal.** New AdMob apps/ad units can take a few hours to start serving, and
  until the app is published on Play and linked in AdMob, many requests return "No fill" (error code 3).
  The game simply carries on without the ad.
- If Gradle sync reports a newer `play-services-ads` / `user-messaging-platform` version, accepting
  Android Studio's upgrade suggestion is safe.

## Updating the game itself

The source of the game is `block-puzzle.html` in the project root. Never edit
`app/src/main/assets/index.html` by hand — run the sync script from the project root instead:

```
powershell -ExecutionPolicy Bypass -File .\sync.ps1 -SiteUrl https://your-domain.com
```

It copies the game (and `privacy.html`) into the app assets and the `web/` site.
Then bump `versionCode` / `versionName` in `app/build.gradle` and rebuild.

The JavaScript bridge (`AndroidBridge`) gives the game its native extras: the system back
button (closes dialogs, returns to the menu), the Android share sheet with your Play Store link,
Google's in-app rating prompt after a new best score, and all ad calls (`AdsManager.kt`).
The game itself decides when an ad is appropriate; the web version never shows ads.

package com.nivrutti.blockpuzzlejewel

import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * All ad logic lives here. The game (JavaScript) decides *when* an ad makes sense;
 * this class only loads, shows and reports back via window.onAdOpened / onAdClosed / onAdReward.
 */
class AdsManager(
    private val activity: AppCompatActivity,
    private val webView: WebView,
    private val bannerBox: FrameLayout,
    private val nativeBox: FrameLayout
) {
    @Volatile var ready = false
        private set
    @Volatile private var interstitial: InterstitialAd? = null
    @Volatile private var rewarded: RewardedAd? = null
    @Volatile private var rewardedInter: RewardedInterstitialAd? = null
    private var nativeAd: NativeAd? = null
    private var banner: AdView? = null
    private var bannerWanted = false
    private var nativeWanted = false

    private val main = Handler(Looper.getMainLooper())
    private val initStarted = AtomicBoolean(false)
    private val retries = HashMap<String, Int>()
    private lateinit var consent: ConsentInformation

    fun start() {
        consent = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()
        consent.requestConsentInfoUpdate(activity, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { initIfAllowed() }
        }, { initIfAllowed() })
        initIfAllowed()
    }

    private fun initIfAllowed() {
        if (!consent.canRequestAds() || !initStarted.compareAndSet(false, true)) return
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
                .setTestDeviceIds(activity.resources.getStringArray(R.array.ad_test_devices).toList())
                .build()
        )
        Thread {
            MobileAds.initialize(activity) {
                main.post {
                    ready = true
                    setupBanner()
                    loadInterstitial()
                    loadRewarded()
                    loadRewardedInter()
                    loadNative()
                }
            }
        }.start()
    }

    fun privacyOptionsRequired(): Boolean = ::consent.isInitialized &&
        consent.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions() = main.post {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { }
    }

    // ---------- banner (bottom of the game screen, space reserved so nothing jumps) ----------
    private fun setupBanner() {
        val dm = activity.resources.displayMetrics
        val size = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, (dm.widthPixels / dm.density).toInt())
        val ad = AdView(activity).apply {
            adUnitId = activity.getString(R.string.ad_banner)
            setAdSize(size)
        }
        bannerBox.layoutParams.height = size.getHeightInPixels(activity)
        bannerBox.addView(ad)
        ad.loadAd(AdRequest.Builder().build())
        banner = ad
        applyBanner()
    }

    fun setBanner(show: Boolean) = main.post { bannerWanted = show; applyBanner() }

    private fun applyBanner() {
        bannerBox.visibility = if (bannerWanted && ready && !nativeWanted) View.VISIBLE else View.GONE
    }

    // ---------- interstitial (between games only; pacing is decided in JS) ----------
    private fun loadInterstitial() {
        InterstitialAd.load(activity, activity.getString(R.string.ad_interstitial), AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad; retries.remove("inter") }
                override fun onAdFailedToLoad(e: LoadAdError) { interstitial = null; retry("inter") { loadInterstitial() } }
            })
    }

    fun showInterstitial(): Boolean {
        val ad = interstitial ?: return false
        interstitial = null
        main.post {
            ad.fullScreenContentCallback = callbacks("inter") { loadInterstitial() }
            ad.show(activity)
        }
        return true
    }

    // ---------- rewarded (always user-initiated: revive, free power-up) ----------
    private fun loadRewarded() {
        RewardedAd.load(activity, activity.getString(R.string.ad_rewarded), AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewarded = ad; retries.remove("rewarded") }
                override fun onAdFailedToLoad(e: LoadAdError) { rewarded = null; retry("rewarded") { loadRewarded() } }
            })
    }

    fun isRewardedReady() = ready && rewarded != null

    fun showRewarded(tag: String): Boolean {
        val ad = rewarded ?: return false
        rewarded = null
        main.post {
            ad.fullScreenContentCallback = callbacks("rewarded") { loadRewarded() }
            ad.show(activity) { js("onAdReward", tag) }
        }
        return true
    }

    // ---------- rewarded interstitial (bonus after a win, with a skippable countdown in JS) ----------
    private fun loadRewardedInter() {
        RewardedInterstitialAd.load(activity, activity.getString(R.string.ad_rewarded_interstitial), AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) { rewardedInter = ad; retries.remove("rinter") }
                override fun onAdFailedToLoad(e: LoadAdError) { rewardedInter = null; retry("rinter") { loadRewardedInter() } }
            })
    }

    fun isRewardedInterReady() = ready && rewardedInter != null

    fun showRewardedInter(tag: String): Boolean {
        val ad = rewardedInter ?: return false
        rewardedInter = null
        main.post {
            ad.fullScreenContentCallback = callbacks("rinter") { loadRewardedInter() }
            ad.show(activity) { js("onAdReward", tag) }
        }
        return true
    }

    // ---------- native (result screen only, replaces the banner there) ----------
    private fun loadNative() {
        AdLoader.Builder(activity, activity.getString(R.string.ad_native))
            .forNativeAd { ad ->
                if (activity.isDestroyed) { ad.destroy(); return@forNativeAd }
                nativeAd?.destroy()
                nativeAd = ad
                retries.remove("native")
                if (nativeWanted) renderNative()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(e: LoadAdError) { retry("native") { loadNative() } }
            })
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    fun setNative(show: Boolean) = main.post {
        nativeWanted = show && ready
        if (nativeWanted) {
            renderNative()
        } else if (nativeBox.visibility == View.VISIBLE) {
            nativeBox.visibility = View.GONE
            nativeBox.removeAllViews()
            nativeAd?.destroy()
            nativeAd = null
            loadNative()
        }
        applyBanner()
    }

    private fun renderNative() {
        val ad = nativeAd ?: return
        val view = activity.layoutInflater.inflate(R.layout.ad_native, nativeBox, false) as NativeAdView
        view.mediaView = view.findViewById<MediaView>(R.id.ad_media)
        view.headlineView = view.findViewById<TextView>(R.id.ad_headline).apply { text = ad.headline }
        view.bodyView = view.findViewById<TextView>(R.id.ad_body).apply {
            text = ad.body
            visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
        }
        view.iconView = view.findViewById<ImageView>(R.id.ad_icon).apply {
            setImageDrawable(ad.icon?.drawable)
            visibility = if (ad.icon == null) View.GONE else View.VISIBLE
        }
        view.callToActionView = view.findViewById<Button>(R.id.ad_cta).apply {
            text = ad.callToAction ?: "Open"
            visibility = if (ad.callToAction == null) View.GONE else View.VISIBLE
        }
        view.setNativeAd(ad)
        nativeBox.removeAllViews()
        nativeBox.addView(view)
        nativeBox.visibility = View.VISIBLE
    }

    // ---------- helpers ----------
    private fun callbacks(kind: String, reload: () -> Unit) = object : FullScreenContentCallback() {
        override fun onAdShowedFullScreenContent() { js("onAdOpened", kind) }
        override fun onAdDismissedFullScreenContent() { js("onAdClosed", kind); reload() }
        override fun onAdFailedToShowFullScreenContent(e: AdError) { js("onAdClosed", kind); reload() }
    }

    private fun retry(key: String, load: () -> Unit) {
        val n = (retries[key] ?: 0) + 1
        retries[key] = n
        val delay = 1000L shl minOf(n, 6)   // 2s, 4s ... capped at ~64s
        main.postDelayed(load, delay)
    }

    private fun js(fn: String, arg: String) = main.post {
        webView.evaluateJavascript("window.$fn&&window.$fn(${JSONObject.quote(arg)})", null)
    }

    fun onPause() { banner?.pause() }
    fun onResume() { banner?.resume() }
    fun onDestroy() {
        banner?.destroy()
        nativeAd?.destroy()
        main.removeCallbacksAndMessages(null)
    }
}

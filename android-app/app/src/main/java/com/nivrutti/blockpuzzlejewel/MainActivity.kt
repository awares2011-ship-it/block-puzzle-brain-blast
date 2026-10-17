package com.nivrutti.blockpuzzlejewel

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.google.android.play.core.review.ReviewManagerFactory

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var ads: AdsManager

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Game on top, then the native ad slot (result screen) and the banner slot (game screen).
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1B4DCF"))
        }
        webView = WebView(this).apply { setBackgroundColor(Color.parseColor("#1B4DCF")) }
        val nativeBox = FrameLayout(this).apply { visibility = View.GONE }
        val bannerBox = FrameLayout(this).apply {
            visibility = View.GONE
            setBackgroundColor(Color.parseColor("#24357F"))
        }
        root.addView(webView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(nativeBox, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(bannerBox, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        setContentView(root)

        // Fullscreen, but keep the game clear of camera cutouts / notches and any visible system bars.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val safe = insets.getInsets(WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars())
            v.setPadding(safe.left, safe.top, safe.right, safe.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // Serve the bundled game over https://appassets.androidplatform.net
        // so localStorage (best score, settings) works reliably.
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (uri.host == "appassets.androidplatform.net") return false   // game pages (index, privacy)
                openExternal(uri)                                              // mailto:, web links, Play Store
                return true
            }
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mediaPlaybackRequiresUserGesture = false
        }
        webView.isHapticFeedbackEnabled = true
        webView.addJavascriptInterface(GameBridge(), "AndroidBridge")

        ads = AdsManager(this, webView, bannerBox, nativeBox)
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
        ads.start()

        // Back closes dialogs / returns to the menu inside the game; only leaves from the home screen.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.url?.endsWith("index.html") == false && webView.canGoBack()) {
                    webView.goBack()
                    return
                }
                webView.evaluateJavascript("(window.handleBack&&window.handleBack())===true") { handled ->
                    if (handled != "true") moveTaskToBack(true)
                }
            }
        })
    }

    private fun openExternal(uri: Uri) {
        val intent = if (uri.scheme == "mailto") Intent(Intent.ACTION_SENDTO, uri) else Intent(Intent.ACTION_VIEW, uri)
        try { startActivity(intent) } catch (e: ActivityNotFoundException) { }
    }

    inner class GameBridge {
        @JavascriptInterface
        fun share(text: String) {
            runOnUiThread {
                val link = "https://play.google.com/store/apps/details?id=$packageName"
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "$text\n$link")
                }
                startActivity(Intent.createChooser(send, null))
            }
        }

        @JavascriptInterface
        fun requestReview() {
            runOnUiThread {
                val manager = ReviewManagerFactory.create(this@MainActivity)
                manager.requestReviewFlow().addOnCompleteListener { task ->
                    if (task.isSuccessful) manager.launchReviewFlow(this@MainActivity, task.result)
                }
            }
        }

        @JavascriptInterface fun adsEnabled(): Boolean = ads.ready
        @JavascriptInterface fun isRewardedReady(): Boolean = ads.isRewardedReady()
        @JavascriptInterface fun isRewardedInterReady(): Boolean = ads.isRewardedInterReady()
        @JavascriptInterface fun showInterstitial(): Boolean = ads.showInterstitial()
        @JavascriptInterface fun showRewarded(tag: String): Boolean = ads.showRewarded(tag)
        @JavascriptInterface fun showRewardedInterstitial(tag: String): Boolean = ads.showRewardedInter(tag)
        @JavascriptInterface fun setBanner(show: Boolean) { ads.setBanner(show) }
        @JavascriptInterface fun setNative(show: Boolean) { ads.setNative(show) }
        @JavascriptInterface fun privacyOptionsRequired(): Boolean = ads.privacyOptionsRequired()
        @JavascriptInterface fun showPrivacyOptions() { ads.showPrivacyOptions() }
    }

    override fun onPause() {
        ads.onPause()
        webView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        ads.onResume()
    }

    override fun onDestroy() {
        ads.onDestroy()
        webView.destroy()
        super.onDestroy()
    }
}

package app.manu.whatsoncrypto.activities.news

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.support.v7.app.AppCompatActivity

import android.view.LayoutInflater
import android.view.ViewGroup

import android.webkit.WebView
import app.manu.whatsoncrypto.R
import android.webkit.WebViewClient
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.widget.ImageView
import android.widget.TextView







class NewsDetailsActivity : AppCompatActivity() {

    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable { hideSystemUI() }

    private var _mRootView: ViewGroup? = null
    private var _mWebView: WebView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var news_url: String? = null
        val extras = intent.extras
        if (extras != null) {
            news_url = extras.getString("news_url")
        }
        else {
            throw RuntimeException("No news URL passed to NewsDetailsActivity Activity")
        }


        val inflater: LayoutInflater  =  this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _mRootView = inflater.inflate(R.layout.activity_news_details, null) as ViewGroup?

        setContentView(R.layout.loading)

        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                // TODO: The system bars are visible. Make any desired
                // adjustments to your UI, such as showing the action bar or
                // other navigational controls.

                delayedHide(3000)

            } else {
                // TODO: The system bars are NOT visible. Make any desired
                // adjustments to your UI, such as hiding the action bar or
                // other navigational controls.
            }
        }

        _mWebView = _mRootView!!.findViewById(R.id.newsWebView) as WebView

        _mWebView!!.settings.domStorageEnabled = true
        _mWebView!!.settings.javaScriptEnabled = true // enable javascript
        _mWebView!!.loadUrl(news_url)
        _mWebView!!.measure(100, 100)
        _mWebView!!.settings.useWideViewPort = true
        _mWebView!!.settings.loadWithOverviewMode = true

        var loadingFinished = true
        var redirect = false
        _mWebView!!.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                    view: WebView, request: WebResourceRequest): Boolean {
                if (!loadingFinished) {
                    redirect = true
                }

                loadingFinished = false
                _mWebView!!.loadUrl(request.url.toString())
                return true
            }

            override fun onPageStarted(
                    view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingFinished = false
                //SHOW LOADING IF IT ISNT ALREADY VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (!redirect) {
                    loadingFinished = true
                }

                if (loadingFinished && !redirect) {
                    //HIDE LOADING IT HAS FINISHED
                    setContentView(_mRootView)
                } else {
                    redirect = false
                }
            }
        }

        val backButton = _mRootView!!.findViewById(R.id.newsBackButton) as ImageView
        backButton.setOnClickListener { finish() }
    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
        else {
            showSystemUI()
        }
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}
package app.manu.whatsoncrypto.activities.news

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import app.manu.whatsoncrypto.R
import app.manu.whatsoncrypto.activities.BaseCompatActivity

class NewsDetailsActivity : BaseCompatActivity() {
    private var _mRootView: ViewGroup? = null
    private var _mWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var news_url: String? = null
        val extras = intent.extras
        if (extras != null) {
            news_url = extras.getString("news_url")
        } else {
            throw RuntimeException("No news URL passed to NewsDetailsActivity Activity")
        }
        val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _mRootView = inflater.inflate(R.layout.activity_news_details, null) as ViewGroup?

        setContentView(R.layout.loading)

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
                    setContentView(_mRootView!!)
                } else {
                    redirect = false
                }
            }
        }
        val backButton = _mRootView!!.findViewById(R.id.newsBackButton) as ImageView
        backButton.setOnClickListener { finish() }
    }
}
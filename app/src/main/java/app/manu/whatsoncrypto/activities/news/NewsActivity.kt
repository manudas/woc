package app.manu.whatsoncrypto.activities.news

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import app.manu.whatsoncrypto.R
import app.manu.whatsoncrypto.activities.BaseCompatActivity
import app.manu.whatsoncrypto.models.NewsModel


class NewsActivity : BaseCompatActivity() {

    internal lateinit var adapter: PaginationAdapter
    internal lateinit var linearLayoutManager: LinearLayoutManager

    internal lateinit var rv: RecyclerView
    // internal lateinit var progressBar: ProgressBar

    private var isLoading = false
    private var isLastPage = false

    /* SPECIAL NOTE FOR TOTAL_PAGES:
     * PAGE_START is 0 so it will always have one extra item.
     * If 3, it will be 0, 1, 2 and 3 (four pages in total)
     */
    private val TOTAL_PAGES = 3
    private var currentPage = PAGE_START

    private lateinit var newsModel: NewsModel
    private var till_timestamp_news: Long? = null

    private lateinit var mLocalBroadcastManager: LocalBroadcastManager

    private var _mRootView: View? = null


    inner class NewsDataWasUpdatedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("HIT NewsDataWasUpdatedReceiver")

            val intentAction = intent.action
            if (intentAction == NewsModel.intent_image_attached) {

                val decorView = window.peekDecorView()
                // val rootView = decorView.rootView

                val loading_view = decorView.findViewById(R.id.loading) as View?

                if (loading_view != null) {
                    // if (rootView.id == R.layout.loading) {
                    setContentView(_mRootView!!)
                    // initDrawer()
                } else {
                    rv.invalidate()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        till_timestamp_news = System.currentTimeMillis() / 1000

        newsModel = NewsModel(this)
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)

        // we register the broadcast receiver for image attached
        val br = NewsDataWasUpdatedReceiver()
        val intentf = IntentFilter(NewsModel.intent_image_attached)
        // and finally we register the broadcast receiver within our app
        mLocalBroadcastManager.registerReceiver(br, intentf)

        setContentView(R.layout.loading)

        val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _mRootView = inflater.inflate(R.layout.activity_news_layout, null) as ViewGroup?

        rv = _mRootView!!.findViewById<View>(R.id.newsDataContainer) as RecyclerView
        // progressBar = findViewById<View>(R.id.newsProgressBar) as ProgressBar

        adapter = PaginationAdapter(this, ELEMENTS_BY_PAGE)

        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.setLayoutManager(linearLayoutManager)

        rv.setItemAnimator(DefaultItemAnimator())

        rv.setAdapter(adapter)

        val activity = this

        rv.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {

            override val totalPageCount: Int
                get() = TOTAL_PAGES

            override var isLastPage: Boolean
                get() = activity.isLastPage
                set(value) {
                    activity.isLastPage = value
                }
            override var isLoading: Boolean
                get() = activity.isLoading
                set(value) {
                    activity.isLoading = value
                }

            protected override fun loadMoreItems() {
                isLoading = true
                currentPage += 1
                adapter.addLoadingFooter()
                // mocking network delay for API call
                Handler().postDelayed({ loadNextPage() }, 1000)
            }
        })

        // mocking network delay for API call
        Handler().postDelayed({ loadFirstPage() }, 1000)

    }


    private fun loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ")

        val addNews: (Any?) -> Any? = {
            val till = ELEMENTS_BY_PAGE
            adapter.addAll(newsModel.getList().subList(0, till))
            // adapter.removeLoadingFooter()
        }

        val func_list: List<(Any?) -> Any?> = listOf(
                // it es el parametro, ya que no se especificó otro delante de una flecha ->
                newsModel::cacheNews as (Any?) -> Any?,
                addNews as (Any?) -> Any?
        )
        this.newsModel.getNews(this.till_timestamp_news, func_list)

        if (currentPage < TOTAL_PAGES)
        // on first page is not needed the loading footer as we will use our own loading screen
        // adapter.addLoadingFooter()
        else
            isLastPage = true

    }

    private fun loadNextPage() {
        Log.d(TAG, "loadNextPage: $currentPage")

        if (currentPage < TOTAL_PAGES)
        // adapter.addLoadingFooter()
        else
            isLastPage = true

        val sublist = newsModel.getList().subList(
                currentPage * ELEMENTS_BY_PAGE,
                currentPage * ELEMENTS_BY_PAGE + ELEMENTS_BY_PAGE)

        val addNews: (Any?) -> Any? = {
            adapter.removeLoadingFooter()
            /* I have to keep the sublist definition instead of using
             * sublist val here because in the moment I need to use
             * this var, it could be empty (search for more news)
             * and being and old reference, It won't be updated
             */
            adapter.addAll(newsModel.getList().subList(
                    currentPage * ELEMENTS_BY_PAGE,
                    currentPage * ELEMENTS_BY_PAGE + ELEMENTS_BY_PAGE))

        }

        lateinit var func_list: List<(Any?) -> Any?>

        if (sublist.size <= 0) {

            till_timestamp_news = -60 * 60 * 24

            func_list = listOf(
                    // it es el parametro, ya que no se especificó otro delante de una flecha ->
                    newsModel::cacheNews as (Any?) -> Any?,
                    addNews as (Any?) -> Any?
            )
            this.newsModel.getNews(this.till_timestamp_news, func_list)
        }

        isLoading = false

        // do we have elements in the sublist ?
        if (sublist.size > 0) {
            func_list = listOf(
                    // @TODO: to delete: No need to invoke asnynmachine to download news, so no need to add onFinish functions
            )
            this.newsModel.downloadImagesFromNewsList(currentPage * ELEMENTS_BY_PAGE, ELEMENTS_BY_PAGE)
            addNews(null)
        }
    }

    companion object {
        private val TAG = "NewsActivity"
        private val PAGE_START = 0
        private val ELEMENTS_BY_PAGE = 6
    }
}


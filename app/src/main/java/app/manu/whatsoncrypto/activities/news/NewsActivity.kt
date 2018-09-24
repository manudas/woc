package app.manu.whatsoncrypto.activities.news

import app.manu.whatsoncrypto.R

import android.graphics.Movie
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import app.manu.whatsoncrypto.classes.news.News
import app.manu.whatsoncrypto.models.NewsModel

// import com.suleiman.pagination.utils.PaginationScrollListener

class NewsActivity : AppCompatActivity() {

    internal lateinit var adapter: PaginationAdapter
    internal lateinit var linearLayoutManager: LinearLayoutManager

    internal lateinit var rv: RecyclerView
    internal lateinit var progressBar: ProgressBar

    private var isLoading = false
    private var isLastPage = false
    private val TOTAL_PAGES = 3
    private var currentPage = PAGE_START

    private val newsModel: NewsModel = NewsModel()
    private var till_timestamp_news: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        till_timestamp_news = System.currentTimeMillis()/1000

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_layout)

        rv = findViewById<View>(R.id.newsDataContainer) as RecyclerView
        progressBar = findViewById<View>(R.id.newsProgressBar) as ProgressBar

        adapter = PaginationAdapter(this)

        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.setLayoutManager(linearLayoutManager)

        rv.setItemAnimator(DefaultItemAnimator())

        rv.setAdapter(adapter)

        rv.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {

            override val totalPageCount: Int
                get() = TOTAL_PAGES

            override val isLastPage: Boolean
                get() = isLastPage

            override var isLoading: Boolean = false
                get() = isLoading

            protected override fun loadMoreItems() {
                isLoading = true
                currentPage += 1

                loadNextPage()
            }
        })

        loadFirstPage()
    }


    private fun loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ")

        val addAllNews: (Any?) -> Any? = {adapter.addAll(newsModel.getList())}

        val func_list: List<(Any?) -> Any?> = listOf(
            // it es el parametro, ya que no se especificó otro delante de una flecha ->
            newsModel::cacheNews as (Any?) -> Any?,
            addAllNews as (Any?) -> Any?
        )

        this.newsModel.getNews(this.till_timestamp_news, func_list)

        if (currentPage <= TOTAL_PAGES)
            adapter.addLoadingFooter()
        else
            isLastPage = true

    }

    private fun loadNextPage() {

        till_timestamp_news =- 60*60*24

        Log.d(TAG, "loadNextPage: $currentPage")


        val addAllNews: (List<News>) -> Unit = {news_list -> adapter.addAll(news_list)}

        val func_list: List<(Any?) -> Any?> = listOf(
                // it es el parametro, ya que no se especificó otro delante de una flecha ->
                newsModel::cacheNews as (Any?) -> Any?,
                addAllNews as (Any?) -> Any?
        )

        this.newsModel.getNews(this.till_timestamp_news, func_list)

        if (currentPage <= TOTAL_PAGES)
            adapter.addLoadingFooter()
        else
            isLastPage = true

        isLoading = false

    }

    companion object {

        private val TAG = "NewsActivity"

        private val PAGE_START = 0
    }


}


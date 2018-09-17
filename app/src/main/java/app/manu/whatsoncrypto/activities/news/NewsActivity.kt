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

// import com.suleiman.pagination.utils.PaginationScrollListener

class NewsActivity : AppCompatActivity() {

    internal var adapter: PaginationAdapter? = null
    internal var linearLayoutManager: LinearLayoutManager? = null

    internal var rv: RecyclerView? = null
    internal var progressBar: ProgressBar? = null

    private var isLoading = false
    private var isLastPage = false
    private val TOTAL_PAGES = 3
    private var currentPage = PAGE_START

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv = findViewById<View>(R.id.main_recycler) as RecyclerView
        progressBar = findViewById<View>(R.id.main_progress) as ProgressBar

        adapter = PaginationAdapter(this)

        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.setLayoutManager(linearLayoutManager)

        rv.setItemAnimator(DefaultItemAnimator())

        rv.setAdapter(adapter)

        rv.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {

            val totalPageCount: Int
                get() = TOTAL_PAGES

            val isLastPage: Boolean
                get() = isLastPage

            var isLoading: Boolean
                get() = isLoading

            protected fun loadMoreItems() {
                isLoading = true
                currentPage += 1

                // mocking network delay for API call
                Handler().postDelayed({ loadNextPage() }, 1000)
            }
        })


        // mocking network delay for API call
        Handler().postDelayed({ loadFirstPage() }, 1000)

    }


    private fun loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ")
        val movies = Movie.createMovies(adapter.getItemCount())
        progressBar.visibility = View.GONE
        adapter.addAll(movies)

        if (currentPage <= TOTAL_PAGES)
            adapter.addLoadingFooter()
        else
            isLastPage = true

    }

    private fun loadNextPage() {
        Log.d(TAG, "loadNextPage: $currentPage")
        val movies = Movie.createMovies(adapter.getItemCount())

        adapter.removeLoadingFooter()
        isLoading = false

        adapter.addAll(movies)

        if (currentPage != TOTAL_PAGES)
            adapter.addLoadingFooter()
        else
            isLastPage = true
    }

    companion object {

        private val TAG = "NewsActivity"

        private val PAGE_START = 0
    }


}


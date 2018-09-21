package app.manu.whatsoncrypto.activities.news

import app.manu.whatsoncrypto.R

import android.content.Context

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.manu.whatsoncrypto.classes.news.News

import java.util.ArrayList

/**
 * Created by Suleiman on 19/10/16.
 */

class PaginationAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var news: MutableList<News>? = null

    private var isLoadingAdded = false

    val isEmpty: Boolean
        get() = itemCount == 0

    init {
        news = ArrayList()
    }

    fun getNews(): List<News>? {
        return news
    }

    fun setNews(news: MutableList<News>) {
        this.news = news
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> viewHolder = getViewHolder(parent, inflater)
            LOADING -> {
                val v2 = inflater.inflate(R.layout.progress_circle, parent, false)
                viewHolder = LoadingVH(v2)
            }
        }
        return viewHolder!!
    }

    private fun getViewHolder(parent: ViewGroup, inflater: LayoutInflater): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        val v1 = inflater.inflate(R.layout.news_item_layout, parent, false)
        viewHolder = MovieVH(v1)
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val _news = news!![position]

        when (getItemViewType(position)) {
            ITEM -> {
                val movieVH = holder as MovieVH

                movieVH.textView.setText(_news.getTitle())
            }
            LOADING -> {
            }
        }//                Do nothing

    }

    override fun getItemCount(): Int {
        return if (news == null) 0 else news!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == news!!.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    /*
   Helpers
   _________________________________________________________________________________________________
    */

    fun add(n: News) {
        news!!.add(n)
        notifyItemInserted(news!!.size - 1)
    }

    fun addAll(newsList: List<News>) {
        for (n in newsList) {
            add(n)
        }
    }

    fun remove(_news: News?) {
        val position = news!!.indexOf(_news)
        if (position > -1) {
            news!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }


    fun addLoadingFooter() {
        isLoadingAdded = true
        // add(Movie())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        //val position = news!!.size - 1
       // val item = getItem(position)

        //if (item != null) {
            //news!!.removeAt(position)
           // notifyItemRemoved(position)
        //}
    }

    fun getItem(position: Int): News? {
        return news!![position]
    }


    /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */
    protected inner class MovieVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView

        init {

            textView = itemView.findViewById(R.id.item_text) as TextView
        }
    }


    protected inner class LoadingVH(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {

        private val ITEM = 0
        private val LOADING = 1
    }


}
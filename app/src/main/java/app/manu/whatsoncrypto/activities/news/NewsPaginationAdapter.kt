package app.manu.whatsoncrypto.activities.news

import app.manu.whatsoncrypto.R

import android.content.Context

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.manu.whatsoncrypto.classes.news.News

import java.util.ArrayList
import kotlin.math.ceil

/**
 * Created by Suleiman on 19/10/16.
 */

class PaginationAdapter(private val context: Context, private val elements_by_page: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

                calculateLayoutParams(parent, v2)

                viewHolder = LoadingVH(v2)
            }
        }
        return viewHolder!!
    }

    private fun calculateLayoutParams(parent: ViewGroup, view: View){

        val layoutParams : ViewGroup.LayoutParams = view.layoutParams
        val params = LinearLayout.LayoutParams(
                layoutParams
        )
        val isFirst = isEmpty

        val elements_by_page_to_hide_one_element_in_Bottom = elements_by_page - 1

        // as the sum of margins are 0.1, the height factor must be 0.9 so te sum of all is 1
        val heightFactor = 0.9
        // bottomMarginFactor + topMarginFactor should the rest of the sums of the layout heights
        val bottomMarginFactor = 1-heightFactor

        val topMargin : Int =  0

        var bottomMarginDouble = (parent.height / elements_by_page_to_hide_one_element_in_Bottom) * bottomMarginFactor

        var bottomMargin = if (isFirst) {
            ceil(bottomMarginDouble).toInt()
        }
        else {
            ((parent.height / elements_by_page_to_hide_one_element_in_Bottom) * bottomMarginFactor).toInt()
        }

        params.setMargins(0, topMargin, 0, bottomMargin)

        var height = if (isFirst){
            ceil(parent.height * heightFactor / elements_by_page_to_hide_one_element_in_Bottom).toInt()
        }
        else {
            (parent.height * heightFactor / elements_by_page_to_hide_one_element_in_Bottom).toInt()
        }

        params.height = height
        view.layoutParams = params
    }

    private fun getViewHolder(parent: ViewGroup, inflater: LayoutInflater): RecyclerView.ViewHolder {
        /*
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.itemview, parent, false);

            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.height = (int) (parent.getHeight() * 0.1);
            itemView.setLayoutParams(layoutParams);

            return new MyViewHolder(itemView)
         */
        val viewHolder: RecyclerView.ViewHolder
        val v1 = inflater.inflate(R.layout.news_item_layout, parent, false)

        calculateLayoutParams(parent, v1)

        viewHolder = NewsVH(v1)
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val _news = news!![position]

        when (getItemViewType(position)) {
            ITEM -> {
                val NewsVH = holder as NewsVH

                NewsVH.headlineNews.setText(_news.headline)
                NewsVH.advanceNews.setText(_news.body)
                NewsVH.urlNews.setText(_news.url)

                // aqui debemos resolver una promesa:
                // cuando esté listo el bitmap se asigna

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
        // dummy News to add the loading footer
        add(News("", ""))
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = news!!.size - 1
        val item = getItem(position)

        if (item != null) {
            news!!.removeAt(position)
            notifyItemRemoved(position)
        }
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
    protected inner class NewsVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headlineNews: TextView
        val advanceNews: TextView
        val urlNews: TextView
        val imageNews: ImageView

        init {

            headlineNews = itemView.findViewById(R.id.headlineNews) as TextView
            advanceNews = itemView.findViewById(R.id.advanceNews) as TextView
            urlNews = itemView.findViewById(R.id.urlNews) as TextView
            imageNews = itemView.findViewById(R.id.imageNews) as ImageView
        }
    }


    protected inner class LoadingVH(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {

        private val ITEM = 0
        private val LOADING = 1
    }


}
package app.manu.whatsoncrypto.activities.news

import app.manu.whatsoncrypto.R

import android.content.Context
import android.graphics.Movie
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.ArrayList

/**
 * Created by Suleiman on 19/10/16.
 */

class PaginationAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var movies: MutableList<Movie>? = null

    private var isLoadingAdded = false

    val isEmpty: Boolean
        get() = itemCount == 0

    init {
        movies = ArrayList()
    }

    fun getMovies(): List<Movie>? {
        return movies
    }

    fun setMovies(movies: MutableList<Movie>) {
        this.movies = movies
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> viewHolder = getViewHolder(parent, inflater)
            LOADING -> {
                val v2 = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingVH(v2)
            }
        }
        return viewHolder
    }

    private fun getViewHolder(parent: ViewGroup, inflater: LayoutInflater): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        val v1 = inflater.inflate(R.layout.item_list, parent, false)
        viewHolder = MovieVH(v1)
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val movie = movies!![position]

        when (getItemViewType(position)) {
            ITEM -> {
                val movieVH = holder as MovieVH

                movieVH.textView.setText(movie.getTitle())
            }
            LOADING -> {
            }
        }//                Do nothing

    }

    override fun getItemCount(): Int {
        return if (movies == null) 0 else movies!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == movies!!.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    /*
   Helpers
   _________________________________________________________________________________________________
    */

    fun add(mc: Movie) {
        movies!!.add(mc)
        notifyItemInserted(movies!!.size - 1)
    }

    fun addAll(mcList: List<Movie>) {
        for (mc in mcList) {
            add(mc)
        }
    }

    fun remove(city: Movie?) {
        val position = movies!!.indexOf(city)
        if (position > -1) {
            movies!!.removeAt(position)
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
        add(Movie())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = movies!!.size - 1
        val item = getItem(position)

        if (item != null) {
            movies!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getItem(position: Int): Movie? {
        return movies!![position]
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
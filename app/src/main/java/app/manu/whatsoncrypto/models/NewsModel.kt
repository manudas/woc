package app.manu.whatsoncrypto.models

import android.graphics.Bitmap
import app.manu.whatsoncrypto.classes.news.News
import org.json.JSONObject
import org.json.JSONArray
import app.manu.whatsoncrypto.utils.JSON.JSONParser
import app.manu.whatsoncrypto.classes.myCustomAsynTask
import app.manu.whatsoncrypto.utils.bitmaputils.BitmapUtils


class NewsModel {
    private lateinit var _coinAPI_BaseUrl: String
    private var _myAsyncMachine: myCustomAsynTask = myCustomAsynTask()

    companion object {
        enum class action (val path: String) {
            NEWS("v2/news/?")
        }

        val _mNewsList: MutableList<News> = mutableListOf()
    }


    init {
        _coinAPI_BaseUrl = "https://min-api.cryptocompare.com/data/"
        resetAsynTask()
    }

    private fun resetAsynTask() {
        this._myAsyncMachine.resetAsynTask()
    }


    public fun getList(from: Int = 0, to: Int = _mNewsList.size): List<News> {
        return _mNewsList.subList(from, to)
    }

    public fun getNews(beforeTimeStap: Long? = null, onFinish : List<(Any?) -> Any?>){

        this._myAsyncMachine.resetOnFinishFunctions( onFinish )

        this._myAsyncMachine.resetCoreFunctions()

        val function_to_exec: (Array<out String?>) -> Any? = fun(param: Array<out String?>) : Any? {

            // if beforeTimeStap == null API will return the news of today
            val timeParameter = if (beforeTimeStap != null)  "lTs=${beforeTimeStap}" else ""

            var parameters = ""

            parameters += timeParameter

            val jParser = JSONParser()
            // Getting JSON from URL
            val url = _coinAPI_BaseUrl + action.NEWS.path + parameters
            val json: JSONObject? = jParser.getJSONFromUrl(url)
            return json
        }
        this._myAsyncMachine.addCoreFunctions (function_to_exec)

        _myAsyncMachine!!.execute()
    }

    public fun cacheNews(json_query_result: JSONObject) : List<News> {
        val result = this.jsonNewsAsNewsArray(json_query_result)
        NewsModel._mNewsList.addAll(result)
        return NewsModel._mNewsList
    }

    private fun jsonNewsAsNewsArray(json_query_result: JSONObject?) : List<News> {
        val data = json_query_result!!.get("Data") as JSONArray
        val result_list = mutableListOf<News>()
        val imageUrl = Array<String?>(data.length(), {i-> null})
        for (i in 0 until data.length()) {
            val newsJSON_obj = data[i] as JSONObject

            val published_timestamp = newsJSON_obj.get("published_on").toString().toLong()
            val imageurl = newsJSON_obj.get("imageurl") as String
            val headline = newsJSON_obj.get("title") as String
            val url = newsJSON_obj.get("url") as String
            val body = newsJSON_obj.get("body") as String

            val news = News(headline,body,null, imageurl, url, published_timestamp)
            result_list.add(news)

            imageUrl.set(i, imageurl)

        }

        fun f(result_l: MutableList<News>) : (Any?) -> Any? {
            return {m : Any? -> downloadFiles_aux_saveBitmap(result_l, m)}
        }

        val onFinish: List<(Any?) -> Any?> = listOf(
                f(result_list)
        )
        downloadImages(imageUrl, onFinish)
        return result_list
    }


    private fun downloadFiles_aux_saveBitmap (result_list: MutableList<News>, m: Any?) : Any?  {
        val mapa = m as Map<String?, Bitmap?>
        val keys = mapa.keys // it must have only one key
        val url_key = keys.elementAt(0)
        val value = mapa.get(url_key)
        if (value != null) {
            for (news in result_list){
                if (news.imageURL == url_key) {
                    news.picture = value
                    break
                }
            }
        }
        return null
    }

    private fun downloadImages(url_arr: Array<String?>,
                               onFinish: List<(Any?) -> Any?>,
                               offset: Int = 0,
                               limit: Int = 10) {


        val index_from = offset*limit
        val index_to = offset*limit + limit
        val subArray = url_arr.sliceArray(IntRange(index_from, index_to).step(1).toList())
        val needed_resources = Math.min(subArray.size, limit)
        this._myAsyncMachine.initMultiple(needed_resources)

        for (i in 0 until needed_resources) {
            this._myAsyncMachine.resetOnFinishFunctions(onFinish, i = i)
            val function_to_exec: (Array<out String?>) -> Any? = fun(param: Array<out String?>) : Any? {
                val result: MutableMap<String?, Bitmap?> = mutableMapOf()
                val urldisplay = url_arr[i]
                val mImage: Bitmap? = BitmapUtils.getBitmapFromURL(urldisplay)
                result.put(urldisplay, mImage)
                return result
            }
            this._myAsyncMachine.resetCoreFunctions(listOf(function_to_exec), i)
            this._myAsyncMachine.execute(i)
        }
    }
}
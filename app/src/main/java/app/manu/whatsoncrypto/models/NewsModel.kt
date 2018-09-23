package app.manu.whatsoncrypto.models

import android.graphics.Bitmap
import android.os.AsyncTask
import app.manu.whatsoncrypto.classes.news.News
import org.json.JSONObject
import org.json.JSONArray
import app.manu.whatsoncrypto.utils.JSON.JSONParser
import android.graphics.BitmapFactory
import android.util.Log
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

            imageUrl.set(i, imageurl)

        }

        val f: (Any?) -> Unit = {
            m ->
                val mapa = m as Map<String?, Bitmap?>
                for ((integer_key, url_key) in imageUrl.withIndex()) {
                    val value = mapa.get(url_key)
                    if (value != null) {
                        for (news in result_list){
                            if (news.imageURL == url_key) {
                                news.picture = value
                                break
                            }
                        }
                    }
                }
        }

        val onFinish: List<(Any?) -> Any?> = listOf(
                f
        )

        downloadImages(imageUrl, onFinish)
        return result_list
    }

    private fun downloadImages(url_arr: Array<String?>,
                               onFinish: List<(Any?) -> Any?>,
                               ofsset: Int = 0,
                               limit: Int = 10) {


        llevarse esta funcion a otro lado ?


        _mOnFinishAsyncMachineFunctions.clear()

        _mOnFinishAsyncMachineFunctions.addAll( onFinish )

        _mAsyncCode.clear()

        val function_to_exec: (Array<out String?>) -> Any? = fun(param: Array<out String?>) : Any? {
            val loop_limit = Math.min(url_arr.size, limit)
            val result: MutableMap<String?, Bitmap?> = mutableMapOf()
            for (i in 0 until loop_limit) {

                val urldisplay = url_arr[i]
                val mImage: Bitmap? = BitmapUtils.getBitmapFromURL(urldisplay)

                result.put(urldisplay, mImage)
            }
            return result
        }
        _mAsyncCode.add( function_to_exec )
        _myAsyncMachine!!.execute()
    }
}
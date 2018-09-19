package app.manu.whatsoncrypto.models

import android.graphics.Bitmap
import android.os.AsyncTask
import app.manu.whatsoncrypto.classes.news.News
import org.json.JSONObject
import org.json.JSONArray
import app.manu.whatsoncrypto.utils.JSON.JSONParser
import android.graphics.BitmapFactory
import android.util.Log


class NewsModel {
    private lateinit var _coinAPI_BaseUrl: String
    private lateinit var _myAsyncMachine: AsyncTask<String, Unit, Any?>
    private val _mOnFinishAsyncMachineFunctions : MutableList<(Any?) -> Any?> = mutableListOf<(Any?) -> Any?>()
    private val _mAsyncCode : MutableList<(Array<out String?>) -> Any?> = mutableListOf<(Array<out String?>) -> Any?>()
    private val _mAsyncResult : MutableList<Any?> = mutableListOf<Any?>()

    companion object {
        enum class action (val path: String) {
            NEWS("v2/news/?")
        }
    }


    init {
        _coinAPI_BaseUrl = "https://min-api.cryptocompare.com/data/"
        resetAsynTask()
    }

    private fun resetAsynTask() {
        this._myAsyncMachine = object: AsyncTask <String, Unit, Any?>() {
            override fun doInBackground(vararg params: String?): Any? {
                _mAsyncResult.clear()
                for (function in _mAsyncCode) {
                    _mAsyncResult.add(function(params))
                }
                return _mAsyncResult
            }

            override fun onPostExecute(result: Any?) {


                for ((index, function) in _mOnFinishAsyncMachineFunctions.withIndex()) {
                    val function_result = _mAsyncResult.getOrNull(index)
                    function(function_result)
                }
            }
        }
    }


    public fun getNews(beforeTimeStap: Long? = null, onFinish : List<(Any?) -> Any?>){
        _mOnFinishAsyncMachineFunctions.clear()
        _mOnFinishAsyncMachineFunctions.addAll( onFinish )

        _mAsyncCode.clear()

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
        _mAsyncCode.add( function_to_exec )
        _myAsyncMachine!!.execute()
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

    private fun downloadImages(url_arr: Array<String?>, onFinish: List<(Any?) -> Any?>, limit: Int = 10) {
        _mOnFinishAsyncMachineFunctions.clear()

        _mOnFinishAsyncMachineFunctions.addAll( onFinish )

        _mAsyncCode.clear()

        val function_to_exec: (Array<out String?>) -> Any? = fun(param: Array<out String?>) : Any? {
            val loop_limit = Math.min(url_arr.size, limit)
            val result: MutableMap<String?, Bitmap?> = mutableMapOf()
            for (i in 0 until loop_limit) {
                val urldisplay = url_arr[i]
                var mImage: Bitmap? = null
                try {
                    val `in` = java.net.URL(urldisplay).openStream()
                    mImage = BitmapFactory.decodeStream(`in`)
                } catch (e: Exception) {
                    Log.e("Error", e.message)
                    e.printStackTrace()
                }
                result.put(urldisplay, mImage)
            }
            return result
        }
        _mAsyncCode.add( function_to_exec )
        _myAsyncMachine!!.execute()
    }
}
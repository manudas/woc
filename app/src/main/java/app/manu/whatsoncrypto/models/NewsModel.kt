package app.manu.whatsoncrypto.models

import android.os.AsyncTask
import app.manu.whatsoncrypto.classes.news.News
import org.json.JSONObject
import org.json.JSONArray
import app.manu.whatsoncrypto.utils.JSON.JSONParser


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
        for (i in 0 until data.length()) {
            val newsJSON_obj = data[i] as JSONObject





            val published_timestamp = newsJSON_obj.get("published_on") as String
            val picture_url = newsJSON_obj.get("published_on") as String
            val published_timestamp = newsJSON_obj.get("published_on") as String






        }
        return result_list
    }
}
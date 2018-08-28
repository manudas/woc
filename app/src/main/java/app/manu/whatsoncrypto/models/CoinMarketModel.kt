package app.manu.whatsoncrypto.models

import android.os.AsyncTask
import android.util.SparseArray
import app.manu.whatsoncrypto.CoinMarket
import app.manu.whatsoncrypto.classes.coin.Coin
import org.json.JSONObject
import app.manu.whatsoncrypto.utils.JSON.JSONParser
import org.json.JSONArray
import java.util.*


class CoinMarketModel {

    private val _coinAPI_BaseUrl: String
    private var _myAsyncMachine: AsyncTask<String, Unit, Any?>? = null
    private val _mOnFinishAsyncMachineFunctions : MutableList<(Any?) -> Any?> = mutableListOf<(Any?) -> Any?>()
    private val _mAsyncCode : MutableList<(Array<out String?>) -> Any?> = mutableListOf<(Array<out String?>) -> Any?>()
    private val _mAsyncResult : MutableList<Any?> = mutableListOf<Any?>()

    companion object {

        private val maxResultPerApiCall = 2000
        private val maxApiMinuteResolution : Long = 60 * 24 * 7  // in seconds 60 seconds an hour / 24 hours a day / 7 days

        /**
         * Enumerator that tells us how much information we need
         * splitted in periods
         */



        public enum class _api_price_granularity (val associatedPricePeriod: Coin.Companion.price_period, val action: action, val feched_time_in_seconds_from_api: Int, val limitApiResults: Int){
            MINUTE(Coin.Companion.price_period.MINUTE, action.MINUTE_HISTORICAL,
                    24 * 60 * 60 /* one day */,
                    1440 /* 1440 minutes per day */),
            DAILY(Coin.Companion.price_period.DAILY, action.DAILY_HISTORICAL,
                    24 * 30 * 60 * 60 /* one month */,
                    30 /* one result per day as is a daily search */),
            HOURLY(Coin.Companion.price_period.HOURLY, action.HOURLY_HISTORICAL,
                    24 * 60 * 60 /* one day */,
                    24 /* one result per hour */)

        }

        enum class action (val path: String) {
            LIST("all/coinlist"),
            LIST_BY_VOLUME("top/totalvol?"), // limit=10&tsym=USD
            DETAIL_PAIRS("pricemultifull?"),
            HISTORICAL("pricehistorical"),
            DAILY_HISTORICAL("histoday"),
            MINUTE_HISTORICAL("histominute"),
            HOURLY_HISTORICAL("histohour"),
            PRICE("price"),
            DAY_AVERAGE("dayAvg")
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

    /**
     * Asks the api for the whole list of coins
     * ordered in the way that seems most important
     * to itself
     *
     * USED IN: not used at the moment
     *
     */
    public fun getCoinList(onFinish : List<(Any?) -> Any?>) {
        _mOnFinishAsyncMachineFunctions.clear()
        _mOnFinishAsyncMachineFunctions.addAll( onFinish )

        _mAsyncCode.clear()

        val function_to_exec: (Array<out String?>) -> Any? = fun(param: Array<out String?>) : Any? {
            val jParser = JSONParser()
            // Getting JSON from URL
            val url = _coinAPI_BaseUrl+action.LIST.path
            val json: JSONObject? = jParser.getJSONFromUrl(url)
            return json
        }
        _mAsyncCode.add( function_to_exec )
        _myAsyncMachine!!.execute()
    }

    /**
     * Asks the api for the most traded coins
     * by volume in the current day
     *
     * USED IN ConMarket Activity
     *
     */
    public fun getCoinListByVolume(limit: Int?, page: Int?, to: String?, onFinish : List<(Any?) -> Any?>) {
        _mOnFinishAsyncMachineFunctions.clear()
        _mOnFinishAsyncMachineFunctions.addAll( onFinish )

        _mAsyncCode.clear()

        val function_to_exec: (Array<out String?>) -> Any? = fun(param: Array<out String?>) : Any? {
            val jParser = JSONParser()

            // Getting JSON from URL
            val limit_parameter = if (limit != null)  "limit=${limit}" else "limit=10"
            val page_parameter = if (page != null)  "page=${page}" else ""
            val to_parameter = if (to != null)  "tsym=${to}" else "tsym=USD"

            var parameters = ""
            parameters += limit_parameter
            if (parameters != "" && page_parameter != "") parameters += "&${page_parameter}" else parameters += page_parameter
            if (parameters != "" && to_parameter != "") parameters += "&${to_parameter}" else parameters += to_parameter

            val url = _coinAPI_BaseUrl + action.LIST_BY_VOLUME.path + parameters

            val json: JSONObject? = jParser.getJSONFromUrl(url)
            return json
        }

        _mAsyncCode.add( function_to_exec )
        _myAsyncMachine!!.execute()
    }

    /**
     * Asks the API for the details of the most
     * traded coins by volume in the current day
     *
     * USED IN ConMarket Activity
     *
     */
    public fun getCoinDetailsListByVolume(limit: Int?, page: Int?, to: String?, onFinish : List<(Any?) -> Any?>) {

        /* Function to execute once we have the top list by volume */
        val onFirstFinish: (JSONObject?) -> Any? = fun(param: JSONObject?) : Any? {

            _mOnFinishAsyncMachineFunctions.clear()
            _mOnFinishAsyncMachineFunctions.addAll( onFinish )

            _mAsyncCode.clear()

            val function_to_exec: (Array<out String?>) -> Any? = fun(param: Array<out String?>) : Any? {
                val jParser = JSONParser()

                val coinSymbolArr = Coin.getAvailableSymbolList()

                // Getting JSON from URL
                var coinSymbolStr = coinSymbolArr.joinToString ( separator="," )
                coinSymbolStr = if (coinSymbolStr != null)  "fsyms=${coinSymbolStr}" else ""

                val limit_parameter = if (limit != null)  "limit=${limit}" else ""
                val page_parameter = if (page != null)  "page=${page}" else ""
                val to_parameter = if (to != null)  "tsyms=${to}" else "tsyms=USD,EUR" // en plural tsymS

                var parameters = ""
                parameters += limit_parameter
                if (parameters != "" && page_parameter != "") parameters += "&${page_parameter}" else parameters += page_parameter
                if (parameters != "" && coinSymbolStr != "") parameters += "&${coinSymbolStr}" else parameters += coinSymbolStr
                if (parameters != "" && to_parameter != "") parameters += "&${to_parameter}" else parameters += to_parameter

                val url = _coinAPI_BaseUrl + action.DETAIL_PAIRS.path + parameters
                val json: JSONObject? = jParser.getJSONFromUrl(url)
                return json
            }
            cacheCoinList(_mAsyncResult.getOrNull(0) as JSONObject?)

            resetAsynTask()

            _mAsyncCode.add( function_to_exec )
            _myAsyncMachine!!.execute()
            return null
        }
        val func_array = listOf<(Any?) -> Any?>(
                onFirstFinish as (Any?) -> Any?
        )
        getCoinListByVolume(limit, page, to, func_array)
    }

    /**
     * Saves, as a cache, the list of most traded coins
     * by volume, that we previously got from the API
     * in form of JSON
     *
     * USED IN CoinMarket Activity
     *
     */
    public fun cacheCoinList(json_query_result: JSONObject?, update: Boolean = false) {
        var arr = jsonCoinByVolumeAsSortedCoinArray(json_query_result)
        if (!update) {
            Coin.sortedCoinList.clear()
        }
        for(i in 0 until arr.size()) {
            val real_index = arr.keyAt(i)
            Coin.sortedCoinList.put(real_index, arr[real_index])
            Coin.setCoinIndex(arr[real_index].name!!, real_index)
        }
    }
    /**
     * Returns the list of most traded coins by volume, that
     * we previously got from the API in form of JSON, and
     * returns it in an array.
     *
     * USED IN CoinMarket Activity
     *
     */
    private fun jsonCoinByVolumeAsSortedCoinArray(json_query_result: JSONObject?) : SparseArray<Coin> {
        val data = json_query_result!!.get("Data") as JSONArray
        val sparse_result = SparseArray<Coin>()
        for (i in 0 until data.length()) {
            val coinJSON_obj = data[i] as JSONObject
            val coinInfo = coinJSON_obj.get("CoinInfo") as JSONObject
            val name = coinInfo.get("Name") as String
            val fullName = coinInfo.get("FullName") as String
            val coin = Coin.getCoinBySymbol(name)
            if (coin.fullName == null) {
                coin.fullName = fullName
            }
            sparse_result.put(i, coin)
        }
        return sparse_result
    }

    /**
     * Saves the info returned from the API call to get the
     * percentage, current price, percentage variation, etc...
     * in Companion cache.
     *
     * USED IN ConMarket Activity
     *
     */
    public fun cacheCoinDetailsList(json_query_result: JSONObject?) : Map<String, Coin> {
        return jsonCoinDetailsByVolumeAsMap(json_query_result)
    }

    /**
     * Saves the details used in CoinMarket Activity as a Map
     * Used to fetch the looked for data from the cryptocompare
     * API, saving it into a Coin object
     *
     * IN USE IN: CoinMarket activity as an auxiliary function
     */
    private fun jsonCoinDetailsByVolumeAsMap(json_query_result: JSONObject?) : MutableMap<String, Coin> {
        val data = json_query_result!!.get("RAW") as JSONObject

        val keys = data.keys()
        val result = mutableMapOf<String, Coin>()

        for (coin_name in keys){
            val coin = Coin.getCoinBySymbol(coin_name)
            val priceToValues = data.get(coin_name) as JSONObject

            val keys_conversionTo = priceToValues.keys()

            for (key_currency in keys_conversionTo) {

                val priceValueStructure = priceToValues.get(key_currency) as JSONObject
                val time = priceValueStructure.get("LASTUPDATE")
                val max =  priceValueStructure.get("HIGH24HOUR")
                val min =  priceValueStructure.get("LOW24HOUR")
                val current_price = priceValueStructure.get("PRICE")
                val open = priceValueStructure.get("OPEN24HOUR")
                val supply = priceValueStructure.get("SUPPLY")
                val volume = priceValueStructure.get("VOLUME24HOUR")

                val dataPriceMap: MutableMap<String, Any?> = mutableMapOf()
                dataPriceMap["time"] = time
                dataPriceMap["max"] = max
                dataPriceMap["min"] = min
                dataPriceMap["price"] = current_price
                dataPriceMap["open"] = open
                dataPriceMap["supply"] = supply
                dataPriceMap["volume"] = volume
                dataPriceMap["price_period"] = Coin.Companion.price_period.DAILY

                coin.addHistorical(coin_name, dataPriceMap)

                if (current_price != null) {
                    // ARREGLAR ESTO coin.lastPrice = current_price
                }
            }
            result.put(coin_name, coin)
        }
        return result
    }



    /**
     * Asks the API for the PRICE details of the 
     * coin passed as a parameter
     *
     * USED IN ConMarketDetails Activity
     *
     */
    public fun getPriceDetails(coin: String, granularityApi: _api_price_granularity, toDate: Date?, destination_currency: String?, onFinish : List<(Any?) -> Any?> ) {

        val mCalendar = Calendar.getInstance(TimeZone.getTimeZone("utc"))
        val current_utc_millies = mCalendar.timeInMillis

        var millies = 0L

        if (toDate == null) {
            millies = current_utc_millies
        } else {
            millies = toDate.time
        }

        var final_granularityApi: _api_price_granularity? = null

        if ((granularityApi == _api_price_granularity.MINUTE)
                && ((millies - _api_price_granularity.MINUTE.feched_time_in_seconds_from_api) > (current_utc_millies - (maxApiMinuteResolution*60)))) {

            final_granularityApi = granularityApi
            // nothing IMPORTANT to do as is into the allowed minute margin given by the api
            // I do this way instead of inverting greater to minus than for clarity reasons
        }
        else if (granularityApi == _api_price_granularity.MINUTE) {
            // out of api margin, change price granularityApi to hours
            final_granularityApi = _api_price_granularity.HOURLY
        }
        val time_stamp = millies/1000

        var final_destination_currency: String? = null
        if (destination_currency == null){
            final_destination_currency = CoinMarket.Companion.SelectedCurrencyTo
        }
        else {
            final_destination_currency = destination_currency
        }

        val limit_parameter = "limit=${final_granularityApi!!.limitApiResults}"
        val coinFromSymbl = "fsym=${coin.toUpperCase()}"
        val to_currency_parameter = "tsym=${final_destination_currency}"
        val to_timestamp = "toTs=${time_stamp}"

        var parameters = "?"
        parameters += limit_parameter
        if (parameters != "" && coinFromSymbl != "") parameters += "&${coinFromSymbl}" else parameters += coinFromSymbl
        if (parameters != "" && to_timestamp != "") parameters += "&${to_timestamp}" else parameters += to_timestamp
        if (parameters != "" && to_currency_parameter != "") parameters += "&${to_currency_parameter}" else parameters += to_currency_parameter


        val url = _coinAPI_BaseUrl + granularityApi.action.path + parameters


        val function_to_exec: (Array<out String?>) -> Any? = fun(param: Array<out String?>) : Any? {
            val jParser = JSONParser()
            // Getting JSON from URL
            val url = url
            val json: JSONObject? = jParser.getJSONFromUrl(url)
            return json
        }

        _mOnFinishAsyncMachineFunctions.clear()
        _mOnFinishAsyncMachineFunctions.addAll( onFinish )

        _mAsyncCode.clear()
        _mAsyncCode.add( function_to_exec )
        _myAsyncMachine!!.execute()

    }

    /**
     * Saves, as a cache, the PRICE details of
     * the coin passed as a parameter
     *
     * USED IN ConMarketDetails Activity
     *
     */
    public fun saveCoinDetails(vararg details: Any?): Any? {
        val _coinnameFrom = details[0] as String
        val _coinnameTo = details[1] as String
        val _details = (details[2]) as JSONObject?

        val coinFrom = _coinnameFrom.toUpperCase()
        val coinTo = _coinnameTo.toUpperCase()

        val coin = Coin.getCoinData(coinFrom)

        val _details_data = _details!!.get("Data") as JSONArray

        for (index in 0 until _details_data.length()){
            var current_price_detail = _details_data[index] as JSONObject
            val time = current_price_detail.get("time").toString().toLong()

            val priceDataInTime = mutableMapOf<String, Any?>()
            priceDataInTime["time"] = time


            /* this is the data we get from the API:

                "time":1535480640,
                "close":7070.58,
                "high":7071,
                "low":7070.07,
                "open":7070.36,
                "volumefrom":12.71,
                "volumeto":89721.14 // VOLUME IN THE coinTo currency

             */

            val max = current_price_detail.get("high").toString().toDouble()
            val min = current_price_detail.get("low").toString().toDouble()
            val current_price = current_price_detail.get("close").toString().toDouble()
            val open = current_price_detail.get("open").toString().toDouble()
            val volume = current_price_detail.get("open").toString().toDouble()

            priceDataInTime["time"] = time
            priceDataInTime["max"] = max
            priceDataInTime["min"] = min
            priceDataInTime["price"] = current_price
            priceDataInTime["open"] = open
            priceDataInTime["supply"] = null
            priceDataInTime["volume"] = volume

            coin!!.addHistorical(coinTo, priceDataInTime)
        }
        return null
    }
}
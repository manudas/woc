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

        private val CoinSymbolMap = hashMapOf(
                "eur" to "€",
                "usd" to "$"
        )

        public fun getSymbol(coin_name: String): String {
            val symbol = CoinSymbolMap.get(coin_name.toLowerCase())
            if (symbol != null) {
                return symbol
            }
            else {
                return coin_name.toUpperCase()
            }
        }

        val CoinIconMap = hashMapOf(
                "adc" to "\uf000",
                "aeon" to "\uf001",
                "amp" to "\uf002",
                "anc" to "\uf003",
                "ardr" to "\uf004",
                "aur" to "\uf005",
                "bay" to "\uf006",
                "bcn" to "\uf007",
                "brk" to "\uf008",
                "brx" to "\uf009",
                "bsd" to "\uf00a",
                "bta" to "\uf00b",
                "btc" to "\uf00c",
                "btc-alt" to "\uf00d",
                "btcd" to "\uf00e",
                "bts" to "\uf00f",
                "clam" to "\uf010",
                "cloak" to "\uf011",
                "dash" to "\uf012",
                "dcr" to "\uf013",
                "dgb" to "\uf014",
                "dgd" to "\uf015",
                "dgx" to "\uf016",
                "dmd" to "\uf017",
                "doge" to "\uf018",
                "emc" to "\uf019",
                "erc" to "\uf01a",
                "etc" to "\uf01b",
                "eth" to "\uf01c",
                "fct" to "\uf01d",
                "flo" to "\uf01e",
                "frk" to "\uf01f",
                "ftc" to "\uf020",
                "game" to "\uf021",
                "gld" to "\uf022",
                "gnt" to "\uf023",
                "grc" to "\uf024",
                "grs" to "\uf025",
                "heat" to "\uf026",
                "icn" to "\uf027",
                "ifc" to "\uf028",
                "incnt" to "\uf029",
                "ioc" to "\uf02a",
                "kmd" to "\uf02b",
                "kobo" to "\uf02c",
                "kore" to "\uf02d",
                "lbc" to "\uf02e",
                "ldoge" to "\uf02f",
                "lsk" to "\uf030",
                "ltc" to "\uf031",
                "maid" to "\uf032",
                "mint" to "\uf033",
                "mona" to "\uf034",
                "mue" to "\uf035",
                "neos" to "\uf036",
                "nlg" to "\uf037",
                "nmc" to "\uf038",
                "note" to "\uf039",
                "nuc" to "\uf03a",
                "nxt" to "\uf03b",
                "ok" to "\uf03c",
                "omni" to "\uf03d",
                "pink" to "\uf03e",
                "pivx" to "\uf03f",
                "pot" to "\uf040",
                "ppc" to "\uf041",
                "qrk" to "\uf042",
                "rby" to "\uf043",
                "rdd" to "\uf044",
                "rep" to "\uf045",
                "rise" to "\uf046",
                "sjcx" to "\uf047",
                "sls" to "\uf048",
                "steem" to "\uf049",
                "strat" to "\uf04a",
                "sys" to "\uf04b",
                "trig" to "\uf04c",
                "ubq" to "\uf04d",
                "unity" to "\uf04e",
                "usdt" to "\uf04f",
                "vrc" to "\uf050",
                "vtc" to "\uf051",
                "waves" to "\uf052",
                "xcp" to "\uf053",
                "xem" to "\uf054",
                "xmr" to "\uf055",
                "xrp" to "\uf056",
                "zec" to "\uf057",
                "?" to "\ufffd"
        )
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
    public fun cacheCoinDetailsList(json_query_result: JSONObject?) : SparseArray<MutableMap<String, MutableMap<String, Any>>>? {
        var arr = jsonCoinDetailsByVolumeAsMap(json_query_result)

        val keys = arr.keys
        for (key in keys) {
            val index_in_coin_list = mCurrentIndexToArraySymbolMap.get(key) as Int
            val coinData = mCurrentCoinList!!.get(index_in_coin_list)
            //val coinData = coinSuperMap[key]

            val values_to_add_to_coin = arr[key]!!
            coinData!!.putAll(values_to_add_to_coin)
        }
        return mCurrentCoinList
    }

    /**
     * Returns the details used in ConMarket Activity as a Map
     */
    private fun jsonCoinDetailsByVolumeAsMap(json_query_result: JSONObject?) : MutableMap<String, Map<String, Any>> {
        val data = json_query_result!!.get("RAW") as JSONObject

        val keys = data.keys()

        val coinMap = mutableMapOf<String, Map<String, Any>> ()

        for (coin_name in keys){
            val priceToValues = data.get(coin_name) as JSONObject

            val keys_conversionTo = priceToValues.keys()

            val values_currency = mutableMapOf<String, Any> ()

            for (key_currency in keys_conversionTo) {
                val valuePriceFull = mutableMapOf<String, Any?> ()

                val priceValueStructure = priceToValues.get(key_currency) as JSONObject
                saveJSONObject_InMap(valuePriceFull, priceValueStructure)

/*
                val keys_pricevalueFull = priceValueStructure.keys()

                for (key_pricevalueFull in keys_pricevalueFull){
                    val value_pricevalueFull = priceValueStructure.get(key_pricevalueFull)
                    valuePriceFull.put(key_pricevalueFull, value_pricevalueFull)
                }
*/
                values_currency.put(key_currency, valuePriceFull)
            }
            coinMap.put(coin_name, values_currency)
        }
        return coinMap
    }

    /**
     * Returns the list of coins given by the API
     * as a JSON converted in an SparseArray
     * 
     * USED IN: not used at the moment, to be used
     * just in case in conjunction with getCoinList
     * 
     */
    private fun jsonAllCoinAsArray(json_query_result: JSONObject?) : SparseArray<Map<String, Map<String, Any>>> {
        val data = json_query_result!!.get("Data") as JSONObject
        val sparse_result = SparseArray<Map<String, Map<String, Any>>>()

        val keys = data.keys()
        for ((index, coin_key) in keys.withIndex()) {
            val coin = data.get(coin_key) as JSONObject
            val name = coin.get("Name") as String
            val symbol = coin.get("Symbol") as String
            val order = (coin.get("SortOrder") as String).toInt()

            val map = hashMapOf(
                    name to hashMapOf(
                            "name" to name,
                            "symbol" to symbol
                    )
            )
            sparse_result.put(order, map)
        }
        return sparse_result
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
        val coin_index = Companion.mCurrentIndexToArraySymbolMap.get(coinFrom)

        val array_element = Companion.mCurrentCoinList!![coin_index!!]
        val datamap = array_element[coinFrom] // array_element is a map, being accessed as an array
        val coinToDataMap = datamap!!.get(coinTo) as MutableMap<Any?, Any?>

        var details_coinToDataMap : SortedMap<Long, in Any?>? = coinToDataMap.get("details") as SortedMap<Long, in Any?>?
        if (details_coinToDataMap == null) {
            details_coinToDataMap = hashMapOf<Long, Any?>().toSortedMap()
            coinToDataMap.put("details", details_coinToDataMap)
        }

        val _details_data = _details!!.get("Data") as JSONArray
        for (index in 0 until _details_data.length()){
            var current_price_detail = _details_data[index] as JSONObject

            // val index_Set = current_price_detail!!.keys()

            val priceDataInTime = mutableMapOf<Any, Any?>()

            saveJSONObject_InMap(priceDataInTime, current_price_detail)
            /*
            for (index in index_Set) {
                val value = current_price_detail.get(index)
                priceDataInTime.put(index, value)
            }
            */
            val _time = current_price_detail.get("time").toString()
            var time: Long? = _time.toLong()

            details_coinToDataMap.put(time, priceDataInTime)
        }
        return null
    }


    private fun saveJSONObject_InMap(map: MutableMap<out Any, Any?>, json_object: JSONObject): Map<out Any, Any?>? {
        val index_Set = json_object!!.keys()
        if (json_object.length() > 0) {
            for (index in index_Set) {
                val value = json_object.get(index)
                (map as MutableMap<Any, Any?>).put(index, value)
            }
            return map
        }
        else {
            return null
        }
    }
}
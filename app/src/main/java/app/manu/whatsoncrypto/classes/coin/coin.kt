package app.manu.whatsoncrypto.classes.coin

import android.util.SparseArray
import java.lang.Math.abs
import java.util.*

class Coin (name: String?) {

    companion object {
        public enum class price_period(val time_lapse_in_seconds: Int) {
            MINUTE(60 /* one minute in seconds */),
            DAILY(60 * 60 * 24 /* one day in seconds */),
            HOURLY(60 * 60 /* one hour in seconds */)
        }

        private val mCurrentIndexToArraySymbolMap: MutableMap<String, Int> = hashMapOf<String, Int>()
        fun getCoinIndex(coin: String): Int? {
            return mCurrentIndexToArraySymbolMap.get(coin.toUpperCase())
        }

        fun setCoinIndex(coin: String, i: Int) {
            mCurrentIndexToArraySymbolMap.set(coin.toUpperCase(), i)
        }

        public val sortedCoinList: SparseArray<Coin> = SparseArray<Coin>()
            get() = field

        public fun getCoinBySymbol(symbol: String): Coin {
            val _coin = symbol.toUpperCase()
            val coin_index = mCurrentIndexToArraySymbolMap.get(_coin)
            var result: Coin? = null
            if (coin_index != null) {
                result = sortedCoinList.get(coin_index)
            } else {
                result = Coin(_coin)

            }
            return result!!
        }

        public fun getCoinData(coin: String): Coin? {
            val _coin = coin.toUpperCase()
            val coin_index = mCurrentIndexToArraySymbolMap.get(_coin)
            var result: Coin? = null
            if (coin_index != null) {
                result = sortedCoinList.get(coin_index)
            }
            return result
        }

        public fun getAvailableSymbolList(): Array<String> {
            return mCurrentIndexToArraySymbolMap.keys.toTypedArray()
        }

        private val CoinSymbolMap = hashMapOf(
                "eur" to "€",
                "usd" to "$"
        )

        public fun getSymbol(coin_name: String): String {
            val symbol = CoinSymbolMap.get(coin_name.toLowerCase())
            if (symbol != null) {
                return symbol
            } else {
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

    var name = name
        get() = field
        set(value: String?) {
            if (this.name == null) {
                field = value
            }
        }

    /*
     * Removed as is redundant with historical
     * and can cause inconsistencies. This way
     * I try to keep the system as simple as possible
     *
     */
    // public val lastPrice: MutableMap <String, Double> = mutableMapOf<String, Double>() // USD, EUR and so on

    var fullName: String? = null
        get() = field
        set(value: String?) {
            if (this.fullName == null) {
                field = value
            }
        }

    var supply: Double? = null
        get() = field
        set(value: Double?) {
            field = value
        }
    var maxSupply: Double? = null
        get() = field
        set(value: Double?) {
            field = value
        }

    /* usd/eur -> time -> min/max in time period -> price */
    // private val historical: MutableMap<String, MutableMap<price_period, SortedMap<Long, MutableMap<String, Any?>>>> = mutableMapOf ()
    public val historical = mutableMapOf<String, CoinHistorical>()

    /**
     * Add a new item to the price history of this coin
     * converte to the currency represented by toSym
     *
     */
    public fun addHistorical(toSym: String, dataPriceMap: MutableMap<String, Any?>, period: Companion.price_period = price_period.MINUTE) {


        if (!historical.contains(toSym)) {
            historical[toSym] = CoinHistorical()
        }

        // historical[toSym]!![period]
        var toSymbolMap = historical[toSym]!![period]



        if (toSymbolMap == null) {
            toSymbolMap = mutableMapOf<Long, MutableMap<String, Any?>>().toSortedMap()
            historical[toSym]!![period] = toSymbolMap
        }
        val time: Long = dataPriceMap.get("time").toString().toLong()
        var priceMap = toSymbolMap.get(time)
        if (priceMap == null) {

            /* INFORMATION THAT IS SUPPOSED TO BE STORED IN dataPriceMap
             * (some could not come or come empty or null):
             *
                dataPriceMap["time"] = time
                dataPriceMap["max"] = max
                dataPriceMap["min"] = min
                dataPriceMap["price"] = current_price
                dataPriceMap["open"] = open
                dataPriceMap["supply"] = supply
                dataPriceMap["volume"] = volume
             *
             */

            toSymbolMap[time] = dataPriceMap
        } else return
    }

    /**
     * Returns the price (or another value if
     * specified ) of this coin converted
     * to the toSym currency. It takes the info
     * from the historical at the specified time.
     * If no time is specified, it will return
     * the last price in the historical, the
     * more recent that has been stored
     *
     */
    public fun getValueFromHistorical(toSym: String, time: Long?, value: String = "price", decimals: Int = 3, period: price_period = price_period.MINUTE): Any? {
        var toSymbolMap = historical[toSym]!![period]
        var price_or_historical_value: Any? = null
        if (toSymbolMap != null) {
            var _time: Long? = null
            if (time == null) {
                _time = toSymbolMap.lastKey() // more recent price_or_historical_value stored
            } else {
                _time = time
            }
            if (_time != null) {
                /*
                var min_resolution_in_milliseconds =
                        if (period == Coin.Companion.price_period.MINUTE) {
                            60 /* seconds */ * 1000 /* milliseconds each second */
                        }
                        else if (period == Coin.Companion.price_period.HOURLY) {
                            60 * 60 * 1000
                        }
                        else { // DAILY
                            60 * 60 * 24 * 1000
                        }

                val roundedTime = ((_time + min_resolution_in_milliseconds / 2) / 1000) * 1000

                val price_map = toSymbolMap[roundedTime]
                */
                val price_map = toSymbolMap[_time]


                if (price_map != null) {
                    price_or_historical_value = price_map[value].toString()
                    if ((price_or_historical_value == null) && (value == "price")) {
                        price_or_historical_value = (price_map["max"].toString().toDouble() + price_map["min"].toString().toDouble()) / 2
                    }
                }

                if ((price_or_historical_value != null) && (price_or_historical_value.toString().toDoubleOrNull() != null)) {
                    // rounding to three decimal places
                    var _aux: Double = price_or_historical_value.toString().toDouble()
                    val decimal_factor = Math.pow(10.0, decimals.toDouble())
                    _aux *= decimal_factor
                    _aux = Math.round(_aux).toDouble()
                    _aux /= decimal_factor
                    price_or_historical_value = _aux
                }
            }
        }
        return price_or_historical_value
    }


    /**
     * Useful to find last recorded values of some
     * entries such as Market Capitalization and
     * volume
     *
     */
    public fun getLastValueFromHistorical(toSym: String, value: String = "price", decimals: Int = 3, period : price_period = price_period.MINUTE): Any? {
        var toSymbolMap = historical[toSym]!![period]
        val keys = toSymbolMap!!.keys.toTypedArray().reversedArray() // reversed array to start for the last item
        keys.forEach() {
            // it es la key actual
            val value = getValueFromHistorical(toSym, it, value, decimals, period)
            if (value != null) {
                return value
            }
        }
        return null
    }

    public fun aggregateValuesFromHistorical(
                        toSym: String,
                        howLongFromTimeTo: Long = price_period.DAILY.time_lapse_in_seconds.toLong(),
                        timeTo: Long? = null,
                        value: String,
                        decimals: Int = 3,
                        period : price_period = price_period.MINUTE) : Double?
    {

        var toSymbolMap = historical[toSym]!![period]
        val keys = toSymbolMap!!.keys.toTypedArray().reversedArray() // reversed array to start for the last item

        var _valueResult: Double? = null

        var _timeTo : Long? = timeTo
        if (_timeTo == null) {
            _timeTo = keys[0]
        }

        if (_timeTo != null) {
            var _timeFrom = _timeTo - howLongFromTimeTo
            _timeFrom = findNearestHistoricalTime(toSym, _timeFrom, period)?.let { it } ?: -1L
            if (_timeFrom != -1L) {
                _valueResult = .0
                for (time in keys) {
                    if (time > _timeTo) {
                        continue
                    } else if (time < _timeFrom) {
                        break
                    }

                    _valueResult += toSymbolMap[time]!![value].toString().toDouble()
                }

                val decimal_factor = Math.pow(10.0, decimals.toDouble())
                _valueResult *= decimal_factor
                _valueResult = Math.round(_valueResult).toDouble()
                _valueResult /= decimal_factor
            }
        }
        return _valueResult
    }

    public fun findNearestHistoricalTime(toSym: String, time: Long, period : price_period = price_period.MINUTE): Long? {
        var toSymbolMap = historical[toSym]!![period]
        val keys = toSymbolMap!!.keys.toTypedArray().reversedArray() // reversed array to start for the last item

        var higher : Long? = null
        var lower : Long ? = null

        for (current_time in keys) {
            if (higher == null) {
                higher = current_time
            }
            if (lower == null) {
                lower = current_time
            }
            if (time <= higher!! && time >= lower!!) {
                break // we found our desired result
            }
            else {
                higher = lower
                lower = current_time // as is a descendent array, from bigger to lower
            }
        }
        var winner = if (higher != null && lower != null)
                                if (abs(time-higher!!) < abs(time-lower!!)) higher else lower
                            else null
        return winner
    }
}
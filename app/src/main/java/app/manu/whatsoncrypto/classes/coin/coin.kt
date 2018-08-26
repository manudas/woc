package app.manu.whatsoncrypto.classes.coin

import android.util.SparseArray
import java.util.*

class Coin (name: String?, symbol: String?) {

    companion object {
        enum class price_period(val time_lapse_in_seconds: Int) {
            MINUTE(60 /* one minute in seconds */),
            DAILY(60 * 60 * 24 /* one day in seconds */),
            HOURLY(60 * 60 /* one hour in seconds */)
        }

        private val mCurrentIndexToArraySymbolMap: MutableMap<String, Int> = hashMapOf<String, Int>()
        fun getCoinIndex(coin: String): Int?{
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
            }
            else {
                result = Coin(null, _coin)

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

        public fun getAvailableSymbolList() : Array<String> {
            return mCurrentIndexToArraySymbolMap.keys.toTypedArray()
        }

    }

    var name = name
        get() = this.name
        set(value: String?) {
            if (this.name == null){
                field = value
            }
        }

    var symbol = symbol
        get() = this.symbol
        set(value: String?) {
            if (this.symbol == null){
                field = value
            }
        }

    public val lastPrice: MutableMap <String, Double> = mutableMapOf<String, Double>() // USD, EUR and so on

    var fullName: String? = null
        get() = this.fullName
        set(value: String?) {
            if (this.fullName == null) {
                field = value
            }
        }

    private var supply: Long
        get() = this.supply
        set(value: Long) {
            supply = value
        }
    private var maxSupply: Long
        get() = this.maxSupply
        set(value: Long) {
            maxSupply = value
        }

    /* usd/eur -> time -> min/max in time period -> price */
    private val historical: MutableMap<String, SortedMap<Long, MutableMap<String, Any>>>
            = mutableMapOf<String, SortedMap<Long, MutableMap<String, Any>>>() // USD, EUR and so on

    public fun addHistorical(fromS: String, time: Long, min: Double, max: Double, price_period: price_period){
        var fromSymbolMap = historical[fromS]
        if (fromSymbolMap == null){
            fromSymbolMap = mutableMapOf<Long, MutableMap<String, Any>>().toSortedMap()
            historical[fromS] = fromSymbolMap
        }
        var priceMap = fromSymbolMap.get(time)
        if (priceMap == null){
            priceMap = hashMapOf(
                    "min" to min,
                    "max" to max,
                    "price_period" to price_period
            )
            fromSymbolMap[time] = priceMap
        }
        else return
    }
}
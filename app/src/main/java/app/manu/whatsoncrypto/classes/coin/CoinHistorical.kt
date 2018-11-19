package app.manu.whatsoncrypto.classes.coin

import java.util.*
import kotlin.collections.LinkedHashMap


class CoinHistorical :
        MutableMap<Coin.Companion.price_period, SortedMap<Long, MutableMap<String, Any?>>> by mutableMapOf(),
        Iterable<MutableMap<String, Any?>> // interfaz iterable
{

    val historicalObject = this

    override operator fun iterator(): Iterator<MutableMap<String, Any?>> {
        val it = object : Iterator<MutableMap<String, Any?>>{
            private var current_index: Long? = null

            /**
             * Returns the next available element in the historical
             * hiterator, It launchs an exception if no next element
             * is found
             */
            override fun next(): MutableMap<String, Any?> {
                var next_index: Long?  = null
                next_index = this.findNextIndex(true)
                if (next_index != null) {
                    val price_period_values = Coin.Companion.price_period.values()

                    for (period in price_period_values) {
                        val period_map = historicalObject[period]
                        val next_index_price_map = period_map?.let { period_map[next_index] }
                        if (next_index_price_map != null) {
                            return next_index_price_map
                        }
                    }
                }
                throw NoSuchElementException("No next element found in coin historical")
            }

            /**
             * Private function used to calculate the next index
             * @param saveResult: used to indicate wheter we want
             * to save the found index as the next index or not
             */
            private fun findNextIndex(saveResult: Boolean = true) : Long ? {

                var selectedIndex: Long? = null
                if (this.current_index == null) {
                    selectedIndex = this.findFirstIndex()
                    if (saveResult) {
                        this.current_index = selectedIndex
                    }
                }
                else {
                    val price_period_values = Coin.Companion.price_period.values()
                    val array_result = arrayOf<Long>(0L, 0L, 0L)

                    var index = 0;
                    for (period in price_period_values) {
                        val period_map = historicalObject[period]
                        val keySet = period_map?.keys
                        if (keySet != null) {
                            for (key in keySet) {
                                if (key <= this.current_index!!) {
                                    continue
                                } else {
                                    array_result[index] = key
                                    break
                                }
                            }
                        }
                        index++
                    }

                    Arrays.sort(array_result)
                    var pre_result: Long = 0L
                    for (time in array_result) {
                        if (time != 0L) {
                            pre_result = time
                            break
                        }
                    }

                    if (pre_result == 0L) {
                        selectedIndex = null
                    } else {
                        selectedIndex = pre_result
                    }
                    if (saveResult) {
                        this.current_index = selectedIndex
                    }
                }
                return selectedIndex
            }


            /**
             * Walk all the inner arrays to find the min
             * time stamp mark and returns it
             */
            private fun findFirstIndex() : Long? {
                val price_period_values = Coin.Companion.price_period.values()

                var minIndexFound: Long? = null

                for (period in price_period_values) {
                    val period_map = historicalObject[period]
                    val firstKey = period_map?.let { period_map.firstKey() }
                    if (firstKey != null) {
                        val next_index_price_map = period_map?.let { period_map[firstKey] }
                        if (next_index_price_map != null) { // first index exists
                            if ((minIndexFound == null) || (firstKey < minIndexFound)) {
                                minIndexFound = firstKey
                            }
                        }
                    }
                }

                return minIndexFound
            }

            /**
             * Return wheter or not we can find
             * another element in the price historical
             */
            override fun hasNext(): Boolean {
                val partialResult = this.findNextIndex(false)
                return partialResult != null
            }
        }
        return it
    }
}
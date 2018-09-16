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
                next_index = if (this.current_index == null) {
                    this.findFirstIndex()
                }
                else {
                    this.findNextIndex(true)
                }
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
                if (this.current_index == null) {
                    return this.findFirstIndex()
                }

                val price_period_values = Coin.Companion.price_period.values()
                val array_result = arrayOf<Long>(0L, 0L, 0L)

                var index = 0;
                for (period in price_period_values) {
                    val period_map = historicalObject[period]
                    val keySet = period_map?.keys
                    if (keySet != null) {
                        for (key in keySet) {
                            if (key < this.current_index!!) {
                                continue
                            } else {
                                array_result[index] = key
                            }
                        }
                    }
                    index++
                }

                Arrays.sort(array_result)
                if (array_result[0] == 0L) {
                    return null
                }
                else if (saveResult){
                    this.current_index = array_result[0]
                }
                return array_result[0]
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
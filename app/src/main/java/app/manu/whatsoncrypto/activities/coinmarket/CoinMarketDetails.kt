package app.manu.whatsoncrypto.coinmarket

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.manu.whatsoncrypto.R
import app.manu.whatsoncrypto.activities.BaseCompatActivity
import app.manu.whatsoncrypto.classes.coin.Coin
import app.manu.whatsoncrypto.models.CoinMarketModel
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_coin_market_details.*
import java.text.SimpleDateFormat
import java.util.*

class CoinMarketDetails : BaseCompatActivity() {

    private val mCoinMarketModel = CoinMarketModel()

    private var coinTo: String? = null
    private var coinFrom: String? = null

    private var mNumLabels = 4

    private var mGranurality = CoinMarketModel.Companion._api_price_granularity.MINUTE;

    private var mCurrentX_axis = mutableMapOf<Long, Date>().toSortedMap()
    private var _mRootView: View? = null
    private var topBound: Long? = null
    private var bottomBound: Long? = null
    private var scaleY_factor: Float? = null

    private val mGraphColor = Color.argb(80, 255, 255, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _mRootView = inflater.inflate(R.layout.activity_coin_market_details, null) as ViewGroup?

        setContentView(R.layout.loading)

        val extras = intent.extras
        if (extras != null) {
            val coin_from_name = extras.getString("coin_from_name")
            val coin_to_name = extras.getString("coin_to_name")
            this.coinFrom = coin_from_name.toUpperCase()
            this.coinTo = coin_to_name.toUpperCase()
            //The key argument here must match that used in the other activity

            val f_SavePriceDetails: (Any?) -> Any? = { _details: Any? ->
                mCoinMarketModel.saveCoinDetails(this.coinFrom, this.coinTo, _details)
            }
            val f_drawGraph: (Any?) -> Any? = { _details: Any? ->
                this.drawGraph(this.coinFrom!!)
            }
            val f_setNewLayout: (Any?) -> Any? = { _details: Any? ->
                setContentView(_mRootView!!)
            }
            val f_setCoinDataInView = { _details: Any? ->
                setCoinDataInView(this.coinFrom!!)
            }

            val onFinish_func_array =
                    listOf<(Any?) -> Any?>(
                            f_SavePriceDetails as (Any?) -> Any?,
                            f_drawGraph as (Any?) -> Any?,
                            f_setNewLayout,
                            f_setCoinDataInView
                    )
            mCoinMarketModel.getPriceDetails(this.coinFrom!!, this.mGranurality, null, null, onFinish_func_array)
            // this.setBounds()
            this.setScalable()
            this.setScrollable()
            // this.setScale(null)
            this.setDateLabelFormatter()

            val backButton = _mRootView!!.findViewById(R.id.marketCapBackButton) as ImageView
            backButton.setOnClickListener { finish() }

        } else {
            throw RuntimeException("No coin passed to CoinMarketDetails Activity")
        }
    }

    private fun setCoinDataInView(coin_name: String) {
        val coin_logo_textview = _mRootView!!.findViewById(R.id.coin_logo_details) as TextView

        val lowerCoinName = coin_name.toLowerCase()
        val upperCoinName = coin_name.toUpperCase()

        var coin_font_char = Coin.CoinIconMap.get(lowerCoinName)
        if (coin_font_char == null) {
            coin_font_char = Coin.CoinIconMap["?"]
        }
        coin_logo_textview.text = coin_font_char

        val coin_name_textview = _mRootView!!.findViewById(R.id.coin_name_details) as TextView
        coin_name_textview.text = upperCoinName

        val price_details_textview = _mRootView!!.findViewById(R.id.price_details) as TextView
        val percentage_details_textview = _mRootView!!.findViewById(R.id.percentage_detail) as TextView

        val coin = Coin.getCoinData(coin_name)

        val coinToSymbol = Coin.getSymbol(this.coinTo!!)
        val coinFromSymbol = Coin.getSymbol(this.coinFrom!!)

        val price = coin!!.getValueFromHistorical(this.coinTo!!, null, "price").toString().toDoubleOrNull()

        if (price != null) {
            price_details_textview.text = coinToSymbol + " " + price
        }
        val open = coin!!.getValueFromHistorical(this.coinTo!!, null, "open").toString().toDoubleOrNull()
        if (open != null && price != null) {

            var percentage_change = 100 - (open * 100 / price)
            // rounding to two decimal places
            percentage_change *= 100
            percentage_change = Math.round(percentage_change).toDouble()
            percentage_change /= 100

            percentage_details_textview.text = percentage_change.toString() + "%"
            if (percentage_change > 0.0) {
                // green market
                percentage_details_textview.setTextColor(Color.GREEN)
            } else {
                // bearish market
                percentage_details_textview.setTextColor(Color.RED)
            }
        }

        if (price == null) {
            price_details_textview.text = ""
            percentage_details_textview.text = ""
        }
        if (price == null || open == null) {
            percentage_details_textview.text = ""
        }


        // val supply = coin.getLastValueFromHistorical(this.coinTo!!, "supply").toString().toDoubleOrNull()
        val supply = coin.supply

        val marketCapCoinTo = if (supply != null && price != null) supply * price else .0

        val marketCapCoinTo_str = String.format(coinToSymbol + " %,.2f", marketCapCoinTo) // format the number separating by thousand and with two decimals

        val marketCapCoinFrom_str = String.format(coinFromSymbol + " %,.2f", supply) // format the number separating by thousand and with two decimals

        val marketCapCoinTo_textview = _mRootView!!.findViewById(R.id.market_cap_details_cointo) as TextView
        marketCapCoinTo_textview.text = marketCapCoinTo_str

        val marketCapCoinFrom_textview = _mRootView!!.findViewById(R.id.market_cap_details_coinfrom) as TextView
        marketCapCoinFrom_textview.text = marketCapCoinFrom_str

        val _24hInSecs = Coin.Companion.price_period.DAILY.time_lapse_in_seconds.toLong()

        val volume24hFrom = coin.aggregateValuesFromHistorical(this.coinTo!!, _24hInSecs, null, "volume", 3, Coin.Companion.price_period.MINUTE)
        val volume24hFrom_str = String.format(coinFromSymbol + " %,.2f", volume24hFrom) // format the number separating by thousand and with two decimals
        val volume24hFrom_textview = _mRootView!!.findViewById(R.id.volume_24h_details_from) as TextView


        val volume24hTo = if (volume24hFrom != null && price != null) volume24hFrom * price else .0
        val volume24hTo_str = String.format(coinToSymbol + " %,.2f", volume24hTo) // format the number separating by thousand and with two decimals
        val volume24hTo_textview = _mRootView!!.findViewById(R.id.volume_24h_details_to) as TextView

        volume24hTo_textview.text = volume24hTo_str
        volume24hFrom_textview.text = volume24hFrom_str

        val circulating_supply_str = String.format(coinFromSymbol + " %,.2f", supply) // format the number separating by thousand and with two decimals
        val circulating_supply_textview = _mRootView!!.findViewById(R.id.supply_details) as TextView
        circulating_supply_textview.text = circulating_supply_str

        val max_circulating_supply_textview = _mRootView!!.findViewById(R.id.max_supply_details) as TextView
        max_circulating_supply_textview.text = ""
    }

    private fun setScalable() {
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView
        // enable scaling
        graphView.viewport.isScalable = true; // X Axis
        // graphView.getViewport().setScalableY(true); // Y Axis
    }

    private fun setScrollable() {
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView
        // enable scrollable
        graphView.viewport.isScrollable = true // X
        // graphView.getViewport().isScrollableY = true; // Y Axis
    }

    private fun setBounds() {
        /* UTC:
        val mCalendar = Calendar.getInstance(TimeZone.getTimeZone("utc"))
        val current_utc_millies = mCalendar.timeInMillis
        */
        val now = System.currentTimeMillis()

        this.topBound = now

        val coin = Coin.getCoinData(this.coinFrom!!)

        val coinPriceHistoryIterator = coin!!.historical[this.coinTo!!]!!.iterator()
        val hasValues = coinPriceHistoryIterator.hasNext()

        var lowest_time: Long
        if (hasValues) {
            val historical_map_value = coinPriceHistoryIterator.next()
            lowest_time = historical_map_value.get("time").toString().toLong()
        }
        this.bottomBound = if (this.mGranurality == CoinMarketModel.Companion._api_price_granularity.MINUTE) {
            now - (Coin.Companion.price_period.MINUTE.time_lapse_in_seconds * 60 * 1000) // 1H
        } else if (this.mGranurality == CoinMarketModel.Companion._api_price_granularity.HOURLY) {
            now - (Coin.Companion.price_period.MINUTE.time_lapse_in_seconds * 1000 * 3) // 3H
        } else {
            now - (Coin.Companion.price_period.DAILY.time_lapse_in_seconds * 3 * 1000) // 3D for now
        }
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView

        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.isYAxisBoundsManual = false
        graphView.viewport.setMinX(this.bottomBound!!.toDouble())
        graphView.viewport.setMaxX(this.topBound!!.toDouble())
    }

    private fun setScale(scale: Float?): Unit {
        if (scale == null) {
            this.scaleY_factor =
                    if (this.mGranurality == CoinMarketModel.Companion._api_price_granularity.MINUTE) {
                        60f // 1H
                    } else if ((this.mGranurality == CoinMarketModel.Companion._api_price_granularity.HOURLY)) {
                        60 * 10f // depende de cuanto se dibuje se comprimirá, en principio 10 Horas
                    } else {
                        60 * 10 * 4f // depende de cuanto se dibuje se comprimirá, en principio 4 días
                    }
        } else {
            this.scaleY_factor = -scale
        }
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView
        graphView.scaleY = this.scaleY_factor!!
    }

    private fun setDateLabelFormatter() {
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView

        val _format =
                when (this.mGranurality) {
                    CoinMarketModel.Companion._api_price_granularity.MINUTE -> "H:mm"
                    CoinMarketModel.Companion._api_price_granularity.HOURLY -> "Y-m-d H:mm"
                    CoinMarketModel.Companion._api_price_granularity.DAILY -> "Y-m-d"
                }
        val format = SimpleDateFormat(_format)
        format.timeZone = TimeZone.getDefault()

        // set date label formatter
        graphView.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(graphView.context, format)
        graphView.gridLabelRenderer.numHorizontalLabels = mNumLabels
        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graphView.gridLabelRenderer.setHumanRounding(false)
    }

    private fun drawGraph(coin: String) {
        val _coin = coin.toUpperCase()

        this.setBackgroundColor()
        this.prepareX_Axis(_coin)
        // this.setBounds(coin) it is being done in onCreate, and does not need coin parameter
        this.setGraphSeries(_coin)
        this.setBounds()
    }

    private fun setBackgroundColor() {
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView
        graphView.setBackgroundColor(mGraphColor)
    }

    private fun average(f: Double, f2: Double): Double {
        return (f + f2) / 2
    }

    private fun setGraphSeries(coin: String) {
        // you can directly pass Date objects to DataPoint-Constructor
        // this will convert the Date to double via Date#getTime()
        val series = LineGraphSeries<DataPoint>()
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView

        val coin = Coin.getCoinData(this.coinFrom!!)

        val coinPriceHistoryIterator = coin!!.historical[this.coinTo!!]!!.iterator()
        while (coinPriceHistoryIterator.hasNext()) {
            val next_history_element = coinPriceHistoryIterator.next()

            val price = next_history_element["price"].toString().toDouble()
            val time = next_history_element["time"].toString().toLong()
            val current_date = this.mCurrentX_axis[time]
            val point: DataPoint = DataPoint(current_date, price)
            series.appendData(point, true, mCurrentX_axis.size)
        }
        graphView.addSeries(series)
    }

    private fun prepareX_Axis(coin: String) {
        mCurrentX_axis.clear()

        val coin = Coin.getCoinData(this.coinFrom!!)

        val coinPriceHistoryIterator = coin!!.historical[this.coinTo!!]!!.iterator()
        while (coinPriceHistoryIterator.hasNext()) {
            val next_history_element = coinPriceHistoryIterator.next()
            val time = next_history_element["time"].toString().toLong()
            val date = Date((time * 1000)) // from seconds to milliseconds
            val indice = time
            mCurrentX_axis.put(indice, date)
        }
    }
}
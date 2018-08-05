package app.manu.whatsoncrypto.coinmarket

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import app.manu.whatsoncrypto.R
import app.manu.whatsoncrypto.models.CoinMarketModel
import com.jjoe64.graphview.GraphView

import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.text.SimpleDateFormat
import java.util.*


class CoinMarketDetails : AppCompatActivity() {

    private var _mRootView: ViewGroup? = null

    private val mCoinMarketModel = CoinMarketModel()

    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable { hideSystemUI() }

    private var coinTo : String? = null
    private var coinFrom : String? = null

    private var mCurrentX_axis = mutableMapOf<Long, Date>().toSortedMap()

    private var mNumLabels = 4

    private var mGranurality = CoinMarketModel.Companion.price_granularity.MINUTE;

    private var topBound: Long? = null
    private var bottomBound: Long? = null

    private val mGraphColor = Color.argb(80, 255, 255, 255)

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
        else {
            showSystemUI()
        }
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val inflater: LayoutInflater =  this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _mRootView = inflater.inflate(R.layout.activity_coin_market_details, null) as ViewGroup?

        super.onCreate(savedInstanceState)

        setContentView(R.layout.loading)

        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                // TODO: The system bars are visible. Make any desired
                // adjustments to your UI, such as showing the action bar or
                // other navigational controls.

                delayedHide(3000)

            } else {
                // TODO: The system bars are NOT visible. Make any desired
                // adjustments to your UI, such as hiding the action bar or
                // other navigational controls.
            }
        }

        val extras = intent.extras
        if (extras != null) {
            val coin_from_name = extras.getString("coin_from_name")
            val coin_to_name = extras.getString("coin_to_name")
            this.coinFrom = coin_from_name.toUpperCase()
            this.coinTo = coin_to_name.toUpperCase()
            //The key argument here must match that used in the other activity


            val f_SavePriceDetails : (Any?) -> Any? = { _details: Any? ->
                mCoinMarketModel.saveCoinDetails(this.coinFrom, this.coinTo, _details)
            }
            val f_drawGraph: (Any?) -> Any? =  {
                _details: Any? -> this.drawGraph(this.coinFrom!!)
            }
            val f_setNewLayout: (Any?) -> Any? =  {
                _details: Any? -> setContentView(_mRootView)
            }

            val onFinish_func_array =
                    listOf<(Any?) -> Any?>(
                       f_SavePriceDetails as (Any?) -> Any?,
                       f_drawGraph as (Any?) -> Any?,
                       f_setNewLayout
                    )

            /* function to call:

            mCoinMarketModel::getPriceDetails(coin: String,
               granularity: price_granularity,
               toDate: Date?,
               destination_currency: CoinMarket.Companion.CURRENCY_TO?,
               onFinish : List<(Any?) -> Any?> ) {

            */

            mCoinMarketModel.getPriceDetails(this.coinFrom!!, this.mGranurality , null, null, onFinish_func_array )
            this.setBounds()
            this.setScalable()
            this.setDateLabelFormatter()

        }
        else {
            throw RuntimeException("No coin passed to CoinMarketDetails Activity")
        }
    }

    private fun setScalable() {
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView

        // enable scaling
        graphView.getViewport().setScalable(true); // X Axis
        // graphView.getViewport().setScalableY(true); // Y Axis
    }
    private fun setBounds() {
        val mCalendar = Calendar.getInstance(TimeZone.getTimeZone("utc"))
        val current_utc_millies = mCalendar.timeInMillis

        this.topBound = current_utc_millies
        this.bottomBound = current_utc_millies - this.mGranurality.time_lapse_in_seconds

        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView

        graphView.viewport.isXAxisBoundsManual = true
        graphView.viewport.isYAxisBoundsManual = false
    }
    private fun setDateLabelFormatter(){
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView

        val _format =
                when (this.mGranurality){
                    CoinMarketModel.Companion.price_granularity.MINUTE -> "H:m"
                    CoinMarketModel.Companion.price_granularity.HOURLY -> "Y-m-d H:m"
                    CoinMarketModel.Companion.price_granularity.DAYLY -> "Y-m-d"
                }

        val format = SimpleDateFormat(_format)

        // set date label formatter
        graphView.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(graphView.context, format)
        graphView.gridLabelRenderer.numHorizontalLabels = mNumLabels
        // as we use dates as labels, the human rounding to nice readable numbers
        // is not nessecary
        graphView.gridLabelRenderer.setHumanRounding(false)
    }

    private fun drawGraph(coin: String){
        val _coin = coin.toUpperCase()

        this.setBackgroundColor()
        this.prepareX_Axis(_coin)
        // this.setBounds(coin) it is being done in onCreate, and does not need coin parameter
        this.setGraphSeries(_coin)


    }

    private fun setBackgroundColor() {
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView
        graphView.setBackgroundColor(mGraphColor)
    }

    private fun setGraphSeries(coin: String){
        // you can directly pass Date objects to DataPoint-Constructor
        // this will convert the Date to double via Date#getTime()
        val series = LineGraphSeries<DataPoint>()
        val graphView = this._mRootView!!.findViewById(R.id.graph_view) as GraphView
        val coin_price_details = this.getCoinDetailsMap(coin) as Map<Long, Map<String, Any?>>// is a map
        for(index in this.mCurrentX_axis.keys) {
            val element = coin_price_details!![index] as Map<String, Any?>// is a map
            val current_date = this.mCurrentX_axis[index]
            val higher_price_in_period = element.get("high").toString().toDouble()
            val lower_price_in_period = element.get("low").toString().toDouble()
            val average_price_in_period = (higher_price_in_period + lower_price_in_period) / 2

            val point: DataPoint = DataPoint(current_date, average_price_in_period)
            series.appendData(point,false, mCurrentX_axis.size)
        }

        // val series2 = LineGraphSeries(arrayOf(DataPoint(0.0, 1.0), DataPoint(1.0, 5.0), DataPoint(2.0, 3.0)))
        graphView.addSeries(series)
        // graphView.addSeries(series2)
    }

    private fun getCoinDetailsMap(coin: String): SortedMap<Long, Any?>? {
        val _coin = coin.toUpperCase()
        val coin_index = CoinMarketModel.mCurrentIndexToArraySymbolMap.get(_coin)

        val array_element = CoinMarketModel.mCurrentCoinList!![coin_index!!]
        val datamap = array_element[coinFrom] // array_element is a map, being accessed as an array
        val coinToDataMap = datamap!!.get(coinTo) as MutableMap<Any?, Any?>

        val details_coinToDataMap : SortedMap<Long, Any?>? = coinToDataMap.get("details") as  SortedMap<Long, Any?>?
        return details_coinToDataMap
    }


    private fun prepareX_Axis(coin: String){
        val details_coinToDataMap = this.getCoinDetailsMap(coin)
        val keys = details_coinToDataMap!!.keys

        mCurrentX_axis.clear()

        for (key : Long in keys) {
            val date = Date(key)
            val indice = key
            mCurrentX_axis.put(indice, date)
        }
    }
}

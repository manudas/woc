package app.manu.whatsoncrypto

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.support.v7.app.AppCompatActivity

import app.manu.whatsoncrypto.models.CoinMarketModel

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.math.BigInteger

import android.content.Intent

import app.manu.whatsoncrypto.coinmarket.CoinMarketDetails


class CoinMarket : AppCompatActivity() {

    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable { hideSystemUI() }
    private val mCoinMarketModel = CoinMarketModel()

    private var _mRootView: ViewGroup? = null
    private var _mDataRootView: ViewGroup? = null
    private var _mCoinLimitByPage = 10
    private var _mCoinListCurrentPage = 0


    companion object {
        public var SelectedCurrencyTo: String = "USD"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val inflater: LayoutInflater  =  this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _mRootView = inflater.inflate(R.layout.activity_coin_market, null) as ViewGroup?
        _mDataRootView = _mRootView!!.findViewById(R.id.coinDataContainer)

        // LayoutInflater.from(this).inflate(R.layout.activity_coin_market, null)
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

        val func_array = listOf<(Any?) -> Any?>(
                mCoinMarketModel::cacheCoinDetailsList as (Any?) -> Any?,
                this::drawCoinList as (Any?) -> Any?,
                this::assingDetailsClickEvent as (Any?) -> Any?)
        mCoinMarketModel.getCoinDetailsListByVolume(null, null, null, func_array )

    }

    private fun assingDetailsClickEvent(unused: Any?){
        val coinList = CoinMarketModel.getCoinList()
        val keyLenght = coinList!!.size()
        for ( i in 0 until keyLenght){
            var real_key = coinList.keyAt(i)
            val coin_map = coinList.get(real_key)
            val coin= coin_map
            val coin_symbol = coin!!.keys.first()
            // val name = coin.get("Name") as String?
            val _details_id = this.codify_string_as_int(coin_symbol!!.toLowerCase())

            val imageView : View = _mDataRootView!!.findViewById(_details_id)

            imageView.setOnClickListener(this::showDetails)
        }
    }

    private fun showDetails(v: View): Unit {
        //todo

        /*
         * 1 - Coger nueva clase de detais (SERA UNA ACTIVITY)
         * 2 - Invocar nueva activity pasandole el nombre de la coin
         * 3 - Pasarle CoinMarketModel para guardar las cachés? Son objetos companion/estáticos, no deberían perderse
         */
        val id = v.id
        /*
        val coin_name_superstring = decode_string_from_int(id)
        val coin_name_str = coin_name_superstring.substring("details_".length)
        * now the name doesn't contain details_ so we pass the whole string
        */
        val coin_name_from_str = decode_string_from_int(id)
        val coin_name_to_str = Companion.SelectedCurrencyTo
        val myIntent = Intent(this@CoinMarket, CoinMarketDetails::class.java)
        myIntent.putExtra("coin_from_name", coin_name_from_str) //Optional parameters
        myIntent.putExtra("coin_to_name", coin_name_to_str) //Optional parameters
        this@CoinMarket.startActivity(myIntent)
    }

    private fun drawCoinList(unused: Any?) {
        val data = CoinMarketModel.getCoinList()

        if (_mDataRootView!!.measuredHeight <= 0) {
            val specWidth = View.MeasureSpec.makeMeasureSpec(0 /* any */, View.MeasureSpec.UNSPECIFIED)
            // as specHeight is the same as specWidth, we could have used specWidth twice
            val specHeight = View.MeasureSpec.makeMeasureSpec(0 /* any */, View.MeasureSpec.UNSPECIFIED)

            _mDataRootView!!.measure(specWidth, specHeight)
        }

        for( i in 0 until data!!.size()) {
            val index = data.keyAt(i)
            val coin_map = data.get(index)
            val coin= coin_map
            val coin_symbol = coin!!.keys.first()
            this.drawCoin(coin.get(coin_symbol)!!)

            if (_mCoinListCurrentPage*_mCoinLimitByPage + index >= (_mCoinListCurrentPage+1)*_mCoinLimitByPage) {
                break
            }
        }
        _mRootView!!.setWillNotDraw(false)
        setContentView(_mRootView)
        _mRootView!!.invalidate()
    }

    private fun drawCoin(coin: Map<String, Any>) : Unit {
        val coin_martket_item_view = LayoutInflater.from(this).inflate(R.layout.coin_market_item, _mDataRootView, false)
        // val current_params = coin_martket_item_view.layoutParams

        // val params = LinearLayout.LayoutParams(this, current_params)

        val coin_name_textview : TextView = coin_martket_item_view.findViewById(R.id.coinName)

        val coin_logo_textview : TextView = coin_martket_item_view.findViewById(R.id.coinLogo)
        val coin_price_textview : TextView = coin_martket_item_view.findViewById(R.id.price)
        val coin_evolutionPercentage_textview : TextView = coin_martket_item_view.findViewById(R.id.evolutionPercentage)

        val name = coin.get("Name") as String?

        var coin_font_char = CoinMarketModel.CoinIconMap[name!!.toLowerCase()]
        if (coin_font_char == null){
            coin_font_char = CoinMarketModel.CoinIconMap["?"]
        }
        coin_logo_textview.text = coin_font_char

        coin_name_textview.text = name
        val conversion_obj = coin.get(SelectedCurrencyTo.toUpperCase()) as Map<String, Any>
        val price_any = conversion_obj.get("PRICE") as Any?
        var price_conv: Any? = null
        var price: Double? = null
        price_conv = price_any!!.toString().toDoubleOrNull()
        if (price_conv != null) {
            price = price_conv
        }
        else { // Integer
            price = price_any!!.toString().toInt().toDouble()
        }

        // rounding to three decimal places
        price *= 1000
        price = Math.round(price).toDouble()
        price /= 1000


        coin_price_textview.text = price.toString() + " " + CoinMarketModel.getSymbol(SelectedCurrencyTo)

        var percentage_change = conversion_obj.get("CHANGEPCT24HOUR").toString().toDouble()

        // rounding to two decimal places
        percentage_change *= 100
        percentage_change = Math.round(percentage_change).toDouble()
        percentage_change /= 100

        coin_evolutionPercentage_textview.text = percentage_change.toString() + " %"

        val coin_details_button : ImageView = coin_martket_item_view.findViewById(R.id.coinDetails)
        val _details_new_id = this.codify_string_as_int(name!!.toLowerCase())

        coin_details_button.setId(_details_new_id)
        _mDataRootView!!.addView(coin_martket_item_view)
    }

    private fun codify_string_as_int(str: String): Int {
        // convert to integer
        val bigInt = BigInteger(str.toByteArray())
        // str.toInt()
        return bigInt.toInt()
    }

    private fun decode_string_from_int (integer: Int): String{
        // convert back to string
        val bi = BigInteger.valueOf(integer.toLong())
        val textBack = String(bi.toByteArray())
        return textBack
    }

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
}
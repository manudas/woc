package app.manu.whatsoncrypto.coinmarket

import android.os.Bundle
import android.os.Handler

import android.support.v7.app.AppCompatActivity
import android.view.View
import app.manu.whatsoncrypto.R
import app.manu.whatsoncrypto.models.CoinMarketModel


class CoinMarketDetails : AppCompatActivity() {

    private val mCoinMarketModel = CoinMarketModel()

    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable { hideSystemUI() }

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
            val coin_name = extras.getString("coin_name")
            //The key argument here must match that used in the other activity


            /* function to call:

             mCoinMarketModel::getPriceDetails(coin: String,
                granularity: price_granularity,
                toDate: Date?,
                destination_currency: CoinMarket.Companion.CURRENCY_TO?,
                onFinish : List<(Any?) -> Any?> ) {

             */
            val onFinish_func_array = listOf<(Any?) -> Any?>(
                    mCoinMarketModel::saveCoinDetails as (Any?) -> Any?)
            mCoinMarketModel.getPriceDetails(coin_name, CoinMarketModel.Companion.price_granularity.MINUTE , null, null, onFinish_func_array )
        }
        else {
            throw RuntimeException("No coin passed to CoinMarketDetails Activity")
        }
    }

}

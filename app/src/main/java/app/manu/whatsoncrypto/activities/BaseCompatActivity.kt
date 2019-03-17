package app.manu.whatsoncrypto.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import app.manu.whatsoncrypto.CoinMarket
import app.manu.whatsoncrypto.R
import app.manu.whatsoncrypto.activities.news.NewsActivity
import app.manu.whatsoncrypto.utils.AppNetworkStatus.AppNetworkStatus
import kotlin.reflect.KClass

open class BaseCompatActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable { hideSystemUI() }

    protected var _mViewStub: FrameLayout? = null // ROOT view to attach elements inside te DrawerLayout
    private var _mDrawer: DrawerLayout? = null
    private var _mMenuView: ImageView? = null
    private var _mDrawerRoot: NavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        if (AppNetworkStatus.getInstance(this).isOnline) {
            Toast.makeText(applicationContext,"Fetching data",Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext,"You are not online!!!!",Toast.LENGTH_LONG).show()
            finish()
        }

        super.onCreate(savedInstanceState)
        // The base layout that contains your navigation drawer
        super.setContentView(R.layout.app_base_layout_with_drawer_menu)
        _mViewStub = findViewById<FrameLayout>(R.id.view_stub)
        _mDrawerRoot = findViewById(R.id.navigation_view)

        initDrawer()

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
    }

        /* Override all setContentView methods to put the content view to the FrameLayout view_stub
     * so that, we can make other activity implementations looks like normal activity subclasses.
     */
    override fun setContentView(layoutResID: Int) {
        if (_mViewStub != null) {
            checkNavBar(layoutResID)
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val lp = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            val stubView = inflater.inflate(layoutResID, _mViewStub, false)
            _mViewStub!!.addView(stubView, lp)
        }
    }

    override fun setContentView(view: View) {
        if (_mViewStub != null) {
            checkNavBar(view.id)
            _mViewStub!!.removeAllViews()
            val lp = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            _mViewStub!!.addView(view, lp)
        }
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        if (_mViewStub != null) {
            checkNavBar(view.id)
            _mViewStub!!.addView(view, params)
        }
    }

    protected fun initDrawer() {
        _mDrawer = findViewById<DrawerLayout>(R.id.drawer_layout) as DrawerLayout
        _mMenuView = findViewById<ImageView>(R.id.menuView)
        _mMenuView!!.setOnClickListener {_mDrawer!!.openDrawer(Gravity.START)}

        if (_mDrawerRoot != null) {
            _mDrawerRoot!!.setNavigationItemSelectedListener(this)
        }
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

    override fun onBackPressed() {
        if (this._mDrawer!!.isDrawerVisible(_mDrawerRoot!! as View)) {
            this._mDrawer!!.closeDrawer(_mDrawerRoot!! as View, true)
        } else {
            super.onBackPressed()
        }
    }

    private fun checkNavBar(newLayout: Int){
        val navBar = findViewById<View>(R.id.included_top_bar)
        if (newLayout == R.layout.loading) {
            navBar.visibility = View.GONE
        }
        else { //
            navBar.visibility = View.VISIBLE
        }
    }

    override fun onNavigationItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.nav_item_news -> {
            // User chose the "News" item, show the News Activity ...
            openActivity(NewsActivity::class)
            true
        }

        R.id.nav_item_coinmarketcap -> {
            // User chose the "Coin Market CAP" action, open Coin Market CAP Activity
            openActivity(CoinMarket::class)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            false
            // super.onNavigationItemSelected(item)
        }
    }

    private fun openActivity(newActivity: KClass<*>): Unit {
        if (newActivity != this::class) {
            val myIntent = Intent(this, newActivity.java)
            this.startActivity(myIntent)
        }
    }
}



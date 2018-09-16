package app.manu.whatsoncrypto.classes.progressbar

import android.view.animation.Animation
import android.widget.FrameLayout
import android.graphics.Shader.TileMode
import android.graphics.drawable.BitmapDrawable
import android.content.Context
import android.graphics.Bitmap

import android.widget.ImageView
import android.util.AttributeSet
import android.graphics.drawable.Drawable
import android.view.View

import app.manu.whatsoncrypto.R
import app.manu.whatsoncrypto.utils.LayoutAnimator.Animator
import app.manu.whatsoncrypto.utils.bitmaputils.BitmapUtils

class Progressbar(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val mProgressImage: ImageView
    private var mProgressAnimation: Animator? = null
    //private var mProgressAnimation: TranslateAnimation? = null
    private val mClipImage: ImageView?
    private val mComposedImage: ImageView?

    init {

        val attributes = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.Progressbar,
                0, 0)
        val src = attributes.getDrawable(R.styleable.Progressbar_src)
        var clipMask = attributes.getDrawable(R.styleable.Progressbar_clip_mask)

        mProgressImage = ImageView(context)

        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        mProgressImage.layoutParams = layoutParams
        setBackgroundAsTile(src)



        mProgressImage.visibility = View.INVISIBLE

        // clipMask = null




        if (clipMask != null) {
            mClipImage = ImageView(context)
            mClipImage.background = clipMask

            mClipImage.visibility = View.INVISIBLE

            mComposedImage = ImageView(context)
            val cropLayoutParam = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            mClipImage.layoutParams = cropLayoutParam

            val composedLayoutParam = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            mComposedImage!!.layoutParams = composedLayoutParam

            addView(mProgressImage)
            addView(mClipImage)
            addView(mComposedImage)
        }
        else {
            mClipImage = null
            mComposedImage = null

            mProgressImage.visibility = View.VISIBLE

            addView(mProgressImage)
        }

        initAnimation(mProgressImage!!.background.intrinsicWidth)
        this.startAnimation()
    }

    fun setBackgroundAsTile(src: Drawable) {
        val tileBitmap = BitmapUtils.getBitmapFromVectorDrawable(src)
        val tileRepeatedBitmap = BitmapDrawable(resources, tileBitmap)
        tileRepeatedBitmap.tileModeX = TileMode.REPEAT
        mProgressImage.background = tileRepeatedBitmap
    }

    private fun initAnimation(tileImageWidth: Int) {
/*
        val layoutParams = mProgressImage.layoutParams as FrameLayout.LayoutParams
        layoutParams.setMargins(-tileImageWidth, 0, 0, 0)

        // *HACK* tileImageWidth-3 is used because of *lags*(slow pause) in the moment
        // of animation END-RESTART.
        mProgressAnimation = TranslateAnimation(0f, tileImageWidth.toFloat() - 3, 0f, 0f)
        mProgressAnimation!!.interpolator = LinearInterpolator()
        mProgressAnimation!!.duration = 1000
        mProgressAnimation!!.repeatCount = Animation.INFINITE

        mProgressImage.startAnimation(mProgressAnimation)
*/

        val layoutParams = mProgressImage.layoutParams as FrameLayout.LayoutParams
        layoutParams.setMargins(0, 0, -tileImageWidth, 0)

        if (mClipImage != null) {
            val layoutParamsComposedImage = mComposedImage!!.layoutParams as FrameLayout.LayoutParams
            layoutParamsComposedImage.setMargins(0, 0, -tileImageWidth, 0)
        }

        mProgressAnimation = object : Animator(1000, Animation.INFINITE, context, 60){
            override fun onAnimationFrame(){

                var percentage: Double = getTimePercentage()

                //Log.d("Percentage", percentage.toString())
                var finalLeftMargin = (tileImageWidth - 3).toFloat()
                var startLeftMargin = 0
                var leftMargin = -(startLeftMargin * (1.0 - percentage) + percentage * finalLeftMargin).toInt()

                mProgressImage.translationX = leftMargin.toFloat()
                clip_progress_bar()
            }
            override fun onAnimationStart() {
                mProgressImage.translationX = 0.toFloat()
            }
            override fun onAnimationEnd(){
                var finalLeftMargin = (tileImageWidth - 3).toFloat()
                mProgressImage.translationX = finalLeftMargin
            }
            override fun onAnimationRepeat(){

            }
        }
    }

    fun startAnimation() {
        mProgressAnimation!!.start()
        // mProgressImage.startAnimation(mProgressAnimation)
    }

    protected fun clip_progress_bar() {

        val moving_progress_bar: Bitmap? = BitmapUtils.getBitmapFromView(mProgressImage, null)

        if (mClipImage != null){
           //  val bitmap_src: Bitmap = BitmapUtils.drawableToBitmap(mProgressImage.background)

            var clip_src: Bitmap = BitmapUtils.drawableToBitmap(mClipImage.background)


            if (mClipImage.measuredHeight <= 0) {
                val specWidth = MeasureSpec.makeMeasureSpec(0 /* any */, MeasureSpec.UNSPECIFIED)
                // as specHeight is the same as specWidth, we could have used specWidth twice
                val specHeight = MeasureSpec.makeMeasureSpec(0 /* any */, MeasureSpec.UNSPECIFIED)

                mClipImage.measure(specWidth, specHeight)
            }
            clip_src = Bitmap.createScaledBitmap(clip_src, mClipImage.measuredWidth, mClipImage.measuredHeight, false)
           //  clip_src.width = mClipImage.measuredWidth
            // mComposedImage!!.background = BitmapDrawable(resources, BitmapUtils.getCroppedBitmap(bitmap_src, clip_src))
            mComposedImage!!.background = BitmapDrawable(resources, BitmapUtils.getCroppedBitmap(moving_progress_bar!!, clip_src))
            //(mComposedImage!!.background as BitmapDrawable).tileModeX = TileMode.REPEAT
        }
    }

}
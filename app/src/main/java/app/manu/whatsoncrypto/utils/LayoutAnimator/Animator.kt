package app.manu.whatsoncrypto.utils.LayoutAnimator

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.animation.Animation

abstract class Animator {

    private var duration: Int = 0

    private var repetitions: Int = 0

    private var mStartTime: Long? = null

    private var current_repetition = 0

    val AnimatorRunnable: Runnable = object: Runnable {

        var running: Boolean = true

        fun terminate() {
            running = false
        }

        override fun run() {
            // do stuff here

            while(running) {
                // Log.d("Current mStartTime", mStartTime.toString())
                if (mStartTime!! + duration > System.currentTimeMillis()
                        && (repetitions == Animation.INFINITE || current_repetition < repetitions)) {
                    if(context != null) {
                        val activity = context!! as Activity
                        activity.runOnUiThread(
                                Runnable {
                                        onAnimationFrame()
                                    }
                        )
                    }
                    else {
                        onAnimationFrame()
                    }
                } else {
                    if (repetitions != Animation.INFINITE && current_repetition < repetitions) {
                        if(context != null) {
                            val activity = context!! as Activity
                            activity.runOnUiThread(
                                    Runnable {
                                        onAnimationRepeat()
                                    }
                            )
                        }
                        else {
                            onAnimationRepeat()
                        }
                        current_repetition++
                        initAnimation()
                    } else if (repetitions == Animation.INFINITE) {
                        if(context != null) {
                            val activity = context!! as Activity
                            activity.runOnUiThread(
                                    Runnable {
                                        onAnimationRepeat()
                                    }
                            )
                        }
                        else {
                            onAnimationRepeat()
                        }
                        initAnimation()
                    } else {
                        if(context != null) {
                            val activity = context!! as Activity
                            activity.runOnUiThread(
                                    Runnable {
                                        onAnimationEnd()
                                    }
                            )
                        }
                        else {
                            onAnimationEnd()
                        }
                        terminate()
                    }
                }
                if (mAnimatorThread == null) { // is a Handler, not a thread
                    timerHandler!!.postDelayed(this, 1000.toLong()/FPS!!.toLong())
                    break
                }
            }
        }
    }

    private var mAnimatorThread: Thread? = null

    private var context: Context?

    private var timerHandler: Handler? = null

    private var FPS: Int? = null

    constructor(duration: Int, repetitions: Int, context: Context?, FPS: Int?){
        this.duration = duration!!
        this.repetitions = repetitions!!
        this.context = context
        if (FPS != null) {
            this.FPS = FPS
            this.timerHandler = Handler()
        }
        else {
            this.mAnimatorThread = Thread(AnimatorRunnable)
        }
    }

    abstract fun onAnimationFrame()
    abstract fun onAnimationStart()
    abstract fun onAnimationEnd()
    abstract fun onAnimationRepeat()

    fun getTimePercentage(): Double {
        val currentMilliseconds = System.currentTimeMillis()
        val currentMillisecondsRelative = getRelativeTimeToStartTime(currentMilliseconds)
        //val maxTime = mStartTime!! + duration!!

        val percentage = (duration - currentMillisecondsRelative).toDouble() / duration.toDouble()
        //Log.d("Relative percentage", percentage.toString())
        // val percentage = currentMilliseconds.toDouble() / (maxTime).toDouble()
        var result: Double
        if ((percentage < 1.0) && (percentage > .0)) {
            result = percentage
        } else if (percentage > 1.0){
            result = 1.0
        } else {
            result = .0
        }
        return result
    }

    fun getRelativeTimeToStartTime(currentMilliseconds: Long): Long {
        return currentMilliseconds - mStartTime!!
    }


    fun initAnimation(){
        mStartTime = System.currentTimeMillis()
        // Log.d("NEW mStartTime", mStartTime.toString())
        if(context != null) {
            val activity = context!! as Activity
            activity.runOnUiThread(
                    Runnable {
                        onAnimationStart()
                    }
            )
        }
        else {
            onAnimationStart()
        }
    }

    fun start(){
        initAnimation()

        if (mAnimatorThread != null) {
            var thread: Thread = mAnimatorThread as Thread
            thread.start()
        }
        else {
            timerHandler!!.postDelayed(AnimatorRunnable, 1000.toLong()/FPS!!.toLong())
            // timerHandler!!.postDelayed(AnimatorRunnable, 10.toLong())
        }
    }

}
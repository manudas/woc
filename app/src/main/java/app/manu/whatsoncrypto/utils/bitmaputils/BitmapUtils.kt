package app.manu.whatsoncrypto.utils.bitmaputils

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.graphics.drawable.DrawableCompat

import android.view.View

import android.view.View.MeasureSpec
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


/**
 * Created by tarek on 6/17/17.
 */

object BitmapUtils {

    fun getCroppedBitmap(src: Bitmap, crop_img: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(src.width,
                src.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = -0x1000000

        canvas.drawBitmap(crop_img, 0f, 0f, paint)

        // Keeps the source pixels that cover the destination pixels,
        // discards the remaining source and destination pixels.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        canvas.drawBitmap(src, 0f, 0f, paint)

        return output
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        var bitmap: Bitmap? = null

        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap!!)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun getBitmapFromVectorDrawable(drawable: Drawable): Bitmap? {
        // var drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
        var aux_drawable: Drawable? = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            aux_drawable = DrawableCompat.wrap(drawable).mutate()
        }
        else {
            aux_drawable = drawable
        }

        val bitmap = Bitmap.createBitmap(
                aux_drawable!!.intrinsicWidth,
                aux_drawable!!.intrinsicHeight,
                Bitmap.Config.ARGB_8888) ?: return null
        val canvas = Canvas(bitmap)
        aux_drawable!!.setBounds(0, 0, canvas.width, canvas.height)
        aux_drawable!!.draw(canvas)

        return bitmap
    }


    fun getBitmapFromView(v: View, bitmap: Bitmap ?): Bitmap? {

        var height: Int
        var width: Int

        if (v.measuredHeight <= 0) {
            val specWidth = MeasureSpec.makeMeasureSpec(0 /* any */, MeasureSpec.UNSPECIFIED)
            // as specHeight is the same as specWidth, we could have used specWidth twice
            val specHeight = MeasureSpec.makeMeasureSpec(0 /* any */, MeasureSpec.UNSPECIFIED)

            v.measure(specWidth, specHeight)
            val questionWidth = v.measuredWidth
            val questionHeight = v.measuredHeight

            height = questionHeight
            width = questionWidth
        }
        else {
            height = v.measuredHeight
            width = v.measuredWidth
        }

        var returned_bitmap = bitmap
        if (returned_bitmap == null) {
            returned_bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        else {
            returned_bitmap.recycle()
        }

        val c = Canvas()

        c.setBitmap(returned_bitmap)


        val bgDrawable = v.background
        if (bgDrawable != null) {

            bgDrawable.setBounds(0, 0, width, height)
            // c.matrix = v.matrix
            bgDrawable.draw(c)
            c.matrix = v.matrix

        } else {
            c.drawColor(Color.WHITE)
        }

        v.draw(c)
        return returned_bitmap
    }


    fun getBitmapFromURL(src: String): Bitmap? {
        try {
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input = connection.getInputStream()
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            return null
        }

    }

}
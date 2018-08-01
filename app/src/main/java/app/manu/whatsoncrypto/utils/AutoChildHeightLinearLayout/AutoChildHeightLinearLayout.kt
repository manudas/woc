package app.manu.whatsoncrypto.utils.AutoChildHeightLinearLayout
// todo: eliminar, as with relative layout is not needed
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.LinearLayout
import java.lang.Math.ceil

class AutoChildHeightLinearLayout : LinearLayout {

    constructor(context: Context): this(context, null) {}

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0){}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
            this(context, attrs, defStyleAttr, 0) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int , defStyleRes: Int ):
            super(context, attrs, defStyleAttr, defStyleRes) {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas?) {
        val child_counter = this.childCount
        var i = 0
        val parent_height = this.measuredHeight
        val height_by_child = ceil(parent_height.toDouble() / child_counter.toDouble()).toInt()
        while( i < child_counter){
            var current_child = this.getChildAt(i)
            val current_layout = current_child.layoutParams
            val current_width = current_layout.width
            current_child.layoutParams = LinearLayout.LayoutParams(current_width, height_by_child)
            i++
        }
        super.onDraw(canvas)
    }
}
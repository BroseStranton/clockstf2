package com.example.clockstf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ColorPickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var color: Int = Color.WHITE
    private var listener: OnColorChangedListener? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val huePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val saturationValueRect = RectF()
    private val hueRect = RectF()
    private var selectionX = 0f
    private var selectionY = 0f
    private var selectionHue = 0f

 fun interface OnColorChangedListener {
        fun onColorChanged(color: Int)
    }

    init {
        selectionPaint.style = Paint.Style.STROKE
        selectionPaint.strokeWidth = 4f
        selectionPaint.color = Color.WHITE
    }

    fun setColor(color: Int) {
        this.color = color
        selectionX = getSaturation(color) * saturationValueRect.width() + saturationValueRect.left
        selectionY = (1f - getValue(color)) * saturationValueRect.height() + saturationValueRect.top
        selectionHue = getHue(color)
        invalidate()
    }

    fun getColor(): Int {
        return color
    }

    fun setOnColorChangedListener(listener: OnColorChangedListener) {
        this.listener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = 20f
        saturationValueRect.set(padding, padding, w - padding, h - padding)
        hueRect.set(padding, padding, w - padding, h - padding)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSaturationValueRect(canvas)
        drawHueGradient(canvas)
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawCircle(selectionX, selectionY, 20f, paint)
        selectionPaint.color = if (Color.luminance(color) > 0.5f) Color.BLACK else Color.WHITE
        canvas.drawCircle(selectionX, selectionY, 20f, selectionPaint)
    }

    private fun drawSaturationValueRect(canvas: Canvas) {
        val hue = selectionHue
        val left = saturationValueRect.left
        val top = saturationValueRect.top
        val right = saturationValueRect.right
        val bottom = saturationValueRect.bottom

        val gradient1 = LinearGradient(
            left, top, right, top,
            intArrayOf(Color.WHITE, Color.HSVToColor(floatArrayOf(hue, 1f, 1f))),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient1
        canvas.drawRect(left, top, right, bottom, paint)

        val gradient2 = LinearGradient(
            left, top, left, bottom,
            intArrayOf(Color.HSVToColor(floatArrayOf(hue, 1f, 1f)), Color.BLACK),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient2
        canvas.drawRect(left, top, right, bottom, paint)
        paint.shader = null
    }

    private fun drawHueGradient(canvas: Canvas) {
        val left = hueRect.left
        val top = hueRect.top
        val right = hueRect.right
        val bottom = hueRect.bottom

        val colors = IntArray(7)
        for (i in 0..6) {
            colors[i] = Color.HSVToColor(floatArrayOf(i * 60f, 1f, 1f))
        }

        val hueGradient = LinearGradient(
            left, top, left, bottom,
            colors,
            floatArrayOf(0f, 0.17f, 0.33f, 0.5f, 0.67f, 0.83f, 1f),
            Shader.TileMode.CLAMP
        )

        huePaint.shader = hueGradient
        huePaint.style = Paint.Style.STROKE
        huePaint.strokeWidth = 20f
        canvas.drawRect(hueRect, huePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x.coerceIn(saturationValueRect.left, saturationValueRect.right)
                val y = event.y.coerceIn(saturationValueRect.top, saturationValueRect.bottom)

                selectionX = x
                selectionY = y

                val saturation = (x - saturationValueRect.left) / saturationValueRect.width()
                val value = 1f - (y - saturationValueRect.top) / saturationValueRect.height()

                color = Color.HSVToColor(floatArrayOf(selectionHue, saturation.coerceIn(0f, 1f), value.coerceIn(0f, 1f)))
                listener?.onColorChanged(color)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getHue(color: Int): Float {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[0]
    }

    private fun getSaturation(color: Int): Float {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[1]
    }

    private fun getValue(color: Int): Float {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[2]
    }
}

package com.multilingreborn.keyboard

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.ColorUtils
import kotlin.math.abs

data class Key(
    val code: Int,
    val label: String,
    val subLabel: String = "",
    var x: Int = 0,
    var y: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
    val isSpecial: Boolean = false,
    val weight: Float = 1f
)

data class KeyboardTheme(
    val background: Int = Color.parseColor("#1A1A2E"),
    val keyNormal: Int = Color.parseColor("#16213E"),
    val keySpecial: Int = Color.parseColor("#0F3460"),
    val keyPressed: Int = Color.parseColor("#E94560"),
    val textNormal: Int = Color.WHITE,
    val textSpecial: Int = Color.parseColor("#A0C4FF"),
    val subText: Int = Color.parseColor("#7B7B8A"),
    val keyRadius: Float = 8f,
    val textSize: Float = 15f,
    val subTextSize: Float = 9f
)

class KeyboardView(context: Context, private val layoutManager: LayoutManager) : FrameLayout(context) {

    interface KeyboardListener {
        fun onKey(code: Int, label: String)
        fun onSwipeLeft()
        fun onSwipeRight()
        fun onLongPress(code: Int, label: String)
    }

    var listener: KeyboardListener? = null
    private var theme = KeyboardTheme()
    private val canvasView = KeyCanvasView(context)
    private var emojiContainer: View? = null

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var keys: List<Key> = emptyList()
    private var pressedKey: Key? = null
    private var shifted = false
    private var capsLock = false
    private var currentMode = KeyboardService.KeyboardMode.ALPHA
    private val handler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null

    // Swipe detection
    private var touchStartX = 0f
    private var touchStartY = 0f
    private val SWIPE_THRESHOLD = 80f

    private val rowHeightDp = 48
    private val hPadDp = 3
    private val vPadDp = 3

    init {
        setWillNotDraw(false)
        addView(canvasView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        canvasView.setWillNotDraw(false)
    }

    fun applyTheme(t: KeyboardTheme) {
        theme = t
        setBackgroundColor(t.background)
        canvasView.invalidate()
    }

    fun setMode(mode: KeyboardService.KeyboardMode, shift: Boolean, caps: Boolean) {
        currentMode = mode
        shifted = shift
        capsLock = caps
        emojiContainer?.visibility = GONE
        canvasView.visibility = VISIBLE
        keys = layoutManager.getKeys(mode, shift)
        requestLayout()
        canvasView.invalidate()
    }

    fun showEmojiPanel(panel: EmojiPanel) {
        canvasView.visibility = GONE
        if (emojiContainer == null) {
            emojiContainer = panel.buildView(context)
            addView(emojiContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }
        emojiContainer?.visibility = VISIBLE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val rows = layoutManager.getRowCount(currentMode)
        val density = resources.displayMetrics.density
        val h = (rowHeightDp * density * rows + vPadDp * density * (rows + 1)).toInt()
        setMeasuredDimension(w, h)
        canvasView.measure(
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
        )
        computeKeyBounds(w, h)
    }

    private fun computeKeyBounds(totalW: Int, totalH: Int) {
        val density = resources.displayMetrics.density
        val vPad = (vPadDp * density).toInt()
        val hPad = (hPadDp * density).toInt()
        val rows = layoutManager.getRowDefs(currentMode, shifted)
        val rowH = ((totalH - vPad * (rows.size + 1)) / rows.size).coerceAtLeast(1)
        val newKeys = mutableListOf<Key>()
        var yOff = vPad
        for (row in rows) {
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val usable = totalW - hPad * (row.size + 1)
            var xOff = hPad
            for (key in row) {
                val kw = ((key.weight / totalWeight) * usable).toInt()
                newKeys.add(key.copy(x = xOff, y = yOff, width = kw, height = rowH))
                xOff += kw + hPad
            }
            yOff += rowH + vPad
        }
        keys = newKeys
    }

    // ── Canvas drawing ────────────────────────────────────────────────────────

    inner class KeyCanvasView(context: Context) : View(context) {
        override fun onDraw(canvas: Canvas) {
            for (key in keys) drawKey(canvas, key)
        }
    }

    private fun drawKey(canvas: Canvas, key: Key) {
        val pressed = key == pressedKey
        val bg = when {
            pressed       -> theme.keyPressed
            key.isSpecial -> theme.keySpecial
            else          -> theme.keyNormal
        }
        keyPaint.color = bg
        keyPaint.style = Paint.Style.FILL
        val rect = RectF(key.x.toFloat(), key.y.toFloat(),
            (key.x + key.width).toFloat(), (key.y + key.height).toFloat())
        canvas.drawRoundRect(rect, theme.keyRadius, theme.keyRadius, keyPaint)

        // Subtle top highlight
        keyPaint.color = ColorUtils.blendARGB(bg, Color.WHITE, 0.07f)
        val topRect = RectF(rect.left, rect.top, rect.right, rect.top + theme.keyRadius * 2)
        canvas.drawRoundRect(topRect, theme.keyRadius, theme.keyRadius, keyPaint)

        val label = if (shifted && key.label.length == 1) key.label.uppercase() else key.label
        textPaint.color = if (key.isSpecial) theme.textSpecial else theme.textNormal
        textPaint.textSize = when (label) {
            "⌫", "⏎", "⇧", "123", "#+=", "ABC", "🌐", "☺" -> theme.textSize * resources.displayMetrics.density * 0.85f
            else -> theme.textSize * resources.displayMetrics.density * 0.9f
        }
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD

        val cx = key.x + key.width / 2f
        val cy = key.y + key.height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(label, cx, cy, textPaint)

        // Sub-label
        if (key.subLabel.isNotEmpty()) {
            subTextPaint.color = theme.subText
            subTextPaint.textSize = theme.subTextSize * resources.displayMetrics.density
            subTextPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(key.subLabel,
                key.x + key.width - 4f * resources.displayMetrics.density,
                key.y + theme.subTextSize * resources.displayMetrics.density * 1.2f,
                subTextPaint)
        }
    }

    // ── Touch handling ────────────────────────────────────────────────────────

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = e.x; touchStartY = e.y
                val key = findKey(e.x, e.y)
                pressedKey = key
                canvasView.invalidate()
                key?.let { scheduleLP(it) }
            }
            MotionEvent.ACTION_UP -> {
                cancelLP()
                val dx = e.x - touchStartX
                val dy = e.y - touchStartY
                if (abs(dx) > SWIPE_THRESHOLD && abs(dx) > abs(dy)) {
                    if (dx < 0) listener?.onSwipeLeft() else listener?.onSwipeRight()
                } else {
                    pressedKey?.let { listener?.onKey(it.code, it.label) }
                }
                pressedKey = null
                canvasView.invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                cancelLP(); pressedKey = null; canvasView.invalidate()
            }
        }
        return true
    }

    private fun findKey(x: Float, y: Float): Key? =
        keys.find { k -> x >= k.x && x <= k.x + k.width && y >= k.y && y <= k.y + k.height }

    private fun scheduleLP(key: Key) {
        longPressRunnable = Runnable { listener?.onLongPress(key.code, key.label) }.also {
            handler.postDelayed(it, 600)
        }
    }

    private fun cancelLP() {
        longPressRunnable?.let { handler.removeCallbacks(it) }
        longPressRunnable = null
    }
}

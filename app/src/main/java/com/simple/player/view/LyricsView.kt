package com.simple.player.view

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.Scroller
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.simple.player.lyrics.Lrc

class LyricsView : View, DefaultLifecycleObserver {

    private val paint = Paint()

    private var lyrics: Lrc? = null
    private var activeLine = 0
    private lateinit var timeAnimator: TimeAnimator
    private lateinit var scroller: Scroller
    private var lifecycle: Lifecycle? = null
    private var visibleLineCount = 0
    private var elapsedLine = 0

    var defaultColor: Int = Color.BLUE//Color.parseColor("#80EDEDED")

    var activeColor: Int = Color.parseColor("#FFDC2C1F")

    var lineSpace: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        32F,
        resources.displayMetrics
    )

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        if (context is LifecycleOwner) {
            val lifecycleOwner = context as LifecycleOwner
            lifecycle = lifecycleOwner.lifecycle
            lifecycle?.addObserver(this)
        }


        paint.isAntiAlias = true
        paint.isDither = true
        paint.textAlign = Paint.Align.CENTER
        paint.color = defaultColor
        paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16F, resources.displayMetrics)
        lyrics = LyricsProvider.lrc

        scroller = Scroller(context)
        val startTime = System.currentTimeMillis()

        timeAnimator = TimeAnimator().apply {
            setTimeListener { animation, totalTime, deltaTime ->
                lyrics?.let {
                    val lineList = it.lrcLineList
                    var i = lineList.size - 1
                    val current = (System.currentTimeMillis() - startTime)
                    while (i >= 0) {
                        val lyricsWord = lineList[i]
                        if (lyricsWord.startTime < current) {
                            if (i >= (visibleLineCount shr 1)) {
                                scrollToPosition(i)
                                if (activeLine != i) {
                                    elapsedLine++
                                }
                            }
                            activeLine = i
                            break
                        }
                        i--
                    }
                    invalidate()
                }
            }
            start()
        }
    }

    private fun scrollToPosition(position: Int) {

    }

    override fun computeScroll() {
        super.computeScroll()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        if (visibleLineCount == 0) {
            visibleLineCount = (contentHeight / (paint.textSize + lineSpace)).toInt()
        }

        canvas.apply {
            if (lyrics == null) {
                drawText("尚未发现歌词", contentWidth / 2F, contentHeight / 2F, paint)
            } else {
                lyrics?.let {
                    var i = elapsedLine
                    var drawIndex = 0
                    while (i < (elapsedLine + visibleLineCount) && i < it.lrcLineList.size) {
                        paint.color = if (i == activeLine) activeColor else defaultColor
                        drawText(it.lrcLineList[i].content, contentWidth / 2F, (paint.textSize + lineSpace) * (drawIndex + 1), paint)
                        i++
                        drawIndex++
                    }
                }

            }
        }

    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        timeAnimator.removeAllUpdateListeners()
        timeAnimator.cancel()
    }

    companion object {
        const val TAG = "LyricsView"
    }
}
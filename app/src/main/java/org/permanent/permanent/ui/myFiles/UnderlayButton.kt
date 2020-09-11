package org.permanent.permanent.ui.myFiles

import android.content.res.Resources
import android.graphics.*


class UnderlayButton(
    var text: String,
    var imageResId: Int,
    var color: Int,
    var clickListener: UnderlayButtonClickListener
) {

    private var pos = 0
    private var clickRegion: RectF? = null

    fun onClick(x: Float, y: Float): Boolean {
        if (clickRegion?.contains(x, y)!!) {
            clickListener.onClick(pos)
            return true
        }
        return false
    }

    fun onDraw(c: Canvas, rect: RectF, pos: Int) {
        val p = Paint()

        // Draw background
        p.setColor(color)
        c.drawRect(rect, p)

        // Draw Text
        p.setColor(Color.WHITE)
        //p.setTextSize(LayoutHelper.getPx(MyApplication.getAppContext(), 12));
        p.setTextSize(Resources.getSystem().getDisplayMetrics().density * 12)
        val r = Rect()
        val cHeight = rect.height()
        val cWidth = rect.width()
        p.setTextAlign(Paint.Align.LEFT)
        p.getTextBounds(text, 0, text!!.length, r)
        val x: Float = cWidth / 2f - r.width() / 2f - r.left
        val y: Float = cHeight / 2f + r.height() / 2f - r.bottom
        c.drawText(text, rect.left + x, rect.top + y, p)
        clickRegion = rect
        this.pos = pos
    }

    interface UnderlayButtonClickListener {
        fun onClick(pos: Int)
    }
}
package org.permanent.permanent.ui.myFiles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class SwipeeHelper(context: Context): ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    val BUTTON_WIDTH = 200
    private var recyclerView: RecyclerView? = null
    private var buttons: List<UnderlayButton> = ArrayList()
    private var gestureDetector: GestureDetector
    private var swipedPos = -1
    private var swipeThreshold = 0.5f
    private var buttonsBuffer: HashMap<Int, ArrayList<UnderlayButton>> = HashMap()
    private var recoverQueue: Queue<Int> = object : LinkedList<Int>() {
        override fun add(element: Int): Boolean {
            return if (contains(element)) false else super.add(element)
        }
    }

    private val gestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (button in buttons) {
                if (button.onClick(e.x, e.y)) break
            }
            return true
        }
    }
    init {
        gestureDetector = GestureDetector(context, gestureListener)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val pos = viewHolder.adapterPosition

        if (swipedPos != pos) recoverQueue.add(swipedPos)

        swipedPos = pos

        buttons = if (buttonsBuffer.containsKey(swipedPos)) buttonsBuffer.get(swipedPos)!!
        else ArrayList()

        buttonsBuffer = HashMap()
        swipeThreshold = 0.5f * buttons.size * BUTTON_WIDTH
        recoverSwipedItem()
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val pos = viewHolder.adapterPosition
        var translationX = dX
        val itemView = viewHolder.itemView
        if (pos < 0) {
            swipedPos = pos
            return
        }
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                var buffer: ArrayList<UnderlayButton> = ArrayList()
                if (!buttonsBuffer.containsKey(pos)) {
                    instantiateUnderlayButton(viewHolder, buffer)
                    buttonsBuffer[pos] = buffer
                } else {
                    buffer = buttonsBuffer[pos]!!
                }
                translationX = dX * buffer.size * BUTTON_WIDTH / itemView.width
                drawButtons(canvas, itemView, buffer, pos, translationX)
            }
        }
        super.onChildDraw(
            canvas, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive)
    }

    private fun drawButtons(
        canvas: Canvas,
        itemView: View,
        buffer: List<UnderlayButton>,
        pos: Int,
        dX: Float
    ) {
        var right = itemView.right.toFloat()
        val dButtonWidth = -1 * dX / buffer.size
        for (button in buffer) {
            val left = right - dButtonWidth
            button.onDraw(
                canvas,
                RectF(
                    left,
                    itemView.top.toFloat(),
                    right,
                    itemView.bottom.toFloat()
                ),
                pos
            )
            right = left
        }
    }

    private fun instantiateUnderlayButton(
        viewHolder: RecyclerView.ViewHolder,
        underlayButtons: ArrayList<UnderlayButton>
    ) {
        underlayButtons.add(UnderlayButton(
            "Delete",
            0,
            Color.parseColor("#FF3C30"),
            object : UnderlayButton.UnderlayButtonClickListener {
                override fun onClick(pos: Int) {
//                    final String item = itemAdapter.getData().get(pos);
//                    itemAdapter.removeItem(pos);

//                    Toast.makeText(context, "Item was removed from the list.", Toast.LENGTH_LONG).show()
                }
            }
        ));
        underlayButtons.add(UnderlayButton(
            "Like",
            0,
            Color.parseColor("#FF9502"),
            object : UnderlayButton.UnderlayButtonClickListener {
                override fun onClick(pos: Int) {
//                    Toast.makeText(getApplicationContext(), "You clicked like on item position " + pos, Toast.LENGTH_LONG).show();
                }
            }
        ));
        underlayButtons.add(UnderlayButton(
            "Share",
            0,
            Color.parseColor("#C7C7CB"),
            object : UnderlayButton.UnderlayButtonClickListener {
                override fun onClick(pos: Int) {
//                    Toast.makeText(getApplicationContext(), "You clicked share on item position " + pos, Toast.LENGTH_LONG).show();
                }
            }
        ));
    }

    @SuppressLint("ClickableViewAccessibility")
    fun attachToRecyclerView(rv: RecyclerView) {
        recyclerView = rv
        recyclerView!!.setOnTouchListener(onTouchListener)
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private val onTouchListener: View.OnTouchListener = object : View.OnTouchListener {

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, e: MotionEvent): Boolean {
            if (swipedPos < 0) return false
            val point = Point(e.rawX.toInt(), e.rawY.toInt())
            val swipedViewHolder = recyclerView!!.findViewHolderForAdapterPosition(swipedPos)
            val swipedItem: View = swipedViewHolder!!.itemView
            val rect = Rect()
            swipedItem.getGlobalVisibleRect(rect)
            if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_MOVE) {
                if (rect.top < point.y && rect.bottom > point.y) gestureDetector.onTouchEvent(e) else {
                    recoverQueue.add(swipedPos)
                    swipedPos = -1
                    recoverSwipedItem()
                }
            }
            return false
        }
    }

    @Synchronized
    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            val pos = recoverQueue.poll()
            if (pos > -1) {
                recyclerView!!.adapter!!.notifyItemChanged(pos)
            }
        }
    }
}
package com.example.customcalendar

import android.animation.ObjectAnimator
import android.graphics.Path
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

class CalendarItemAnimator: DefaultItemAnimator() {

    init {
        Log.d(TAG , "CalendarItemAnimator()")
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        Log.d(TAG, "animateAdd()")
        return super.animateAdd(holder)
    }

    override fun animateMove(
        holder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        Log.d(TAG, "animateMove()")
        return super.animateMove(holder, fromX, fromY, toX, toY)
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preInfo: ItemHolderInfo,
        postInfo: ItemHolderInfo
    ): Boolean {
        Log.d(TAG, "animateChange1()")
        return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder?,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        Log.d(TAG, "animateChange2()")
        if(oldHolder == null && newHolder == null) {
            return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)
        }
        val path = Path().apply {
            moveTo(toX.toFloat(), toY.toFloat())
        }
        ObjectAnimator.ofFloat(newHolder!!.itemView, View.X, View.Y, path).apply {
            interpolator = AccelerateInterpolator()
            duration = 1000
            start()
        }

        dispatchChangeFinished(newHolder, false)
        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)
    }

    companion object {
        const val TAG = "CalendarItemAnimator"
    }

}
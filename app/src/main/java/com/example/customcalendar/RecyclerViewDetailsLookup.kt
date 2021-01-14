package com.example.customcalendar

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewDetailsLookup(private val recyclerView: RecyclerView): ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        view?.let {
            return when(val holder = recyclerView.getChildViewHolder(it)) {
                is CalendarViewAdapter.EmptyViewHolder -> {
                    holder.getItemDetails()
                }
                is CalendarViewAdapter.DayOfWeekViewHolder -> {
                    holder.getItemDetails()
                }
                is CalendarViewAdapter.DayOfMonthViewHolder -> {
                    holder.getItemDetails()
                }
                else -> null
            }
        }
        return null
    }
}
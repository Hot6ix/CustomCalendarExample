package com.example.customcalendar

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView

class StableIdKeyProvider(private val recyclerView: RecyclerView): ItemKeyProvider<Long>(SCOPE_MAPPED) {
    override fun getKey(position: Int): Long? = (recyclerView.adapter as CalendarViewAdapter?)?.getItemId(position)

    override fun getPosition(key: Long): Int {
        recyclerView.findViewHolderForItemId(key)?.let {
            return it.layoutPosition
        }

        return RecyclerView.NO_POSITION
    }
}
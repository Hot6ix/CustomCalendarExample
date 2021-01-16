package com.example.customcalendar

import android.content.Context
import android.icu.text.DateFormatSymbols
import android.icu.util.Calendar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.customcalendar.databinding.ItemDayOfMonthBinding
import com.example.customcalendar.databinding.ItemDayOfWeekBinding
import com.example.customcalendar.databinding.ItemEmptyBinding

class CalendarViewAdapter(private val context: Context, private val contents: Array<Pair<Long, String>>? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dateUtils = DateUtils(Calendar.getInstance())
    private val calendarItems = ArrayList<Triple<Int, Int, Int>>()
    private var contentItems: Array<Pair<Long, String>> = contents ?: emptyArray()

    private var tracker: SelectionTracker<Long>? = null
    private var weekendOnSetPositions: Array<Int> = emptyArray()
    private var weekendCeasePositions: Array<Int> = emptyArray()
    private var dayOfWeekHeight: Int = -1

    init {
        setHasStableIds(true)
    }

    fun attachTracker(tracker: SelectionTracker<Long>) {
        this.tracker = tracker
    }

    fun updateCalendar(calendar: Calendar?): Boolean {
        calendar?.let {
            calendarItems.clear()

            dateUtils.calendar = calendar
            updateWeekendData()

            calendarItems.addAll(dateUtils.getDaysOfWeek().map { Triple(-1, -1, it) })
            calendarItems.addAll(dateUtils.getDaysOfMonthWithCalendarFormat())
            notifyItemRangeChanged(0, calendarItems.size)

            return true
        }

        return false
    }

    fun updateContents(contents: Array<Pair<Long, String>>?): Boolean {
        contents?.let {
            contentItems = contents
            notifyDataSetChanged()

            return true
        }

        return false
    }

    fun notifyAllItemsChanged() {
        notifyItemRangeChanged(0, calendarItems.size)
    }

    // Weekends are different between countries
    private fun updateWeekendData() {
        weekendOnSetPositions = Array(7) { -1 }
        weekendCeasePositions = Array(7) { -1 }

        var onSetPosition = dateUtils.getIndexOfWeekendOnSet(dateUtils.getDaysOfWeek())
        var ceasePosition = dateUtils.getIndexOfWeekendCease(dateUtils.getDaysOfWeek())

        for(i in 0 until 6) {
            weekendOnSetPositions[i] = onSetPosition
            weekendCeasePositions[i] = ceasePosition

            onSetPosition += 7
            ceasePosition += 7
        }
    }

    override fun getItemCount(): Int = calendarItems.size

    override fun getItemId(position: Int): Long = calendarItems[position].hashCode().toLong()

    override fun getItemViewType(position: Int): Int {
        Log.d(TAG, "getItemViewType()")
        return when {
            position in 0..6 -> TYPE_DAY_OF_WEEK
            calendarItems[position].second < 0 -> TYPE_EMPTY
            else -> TYPE_DAY_OF_MONTH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "onCreateViewHolder()")
        return when(viewType) {
            TYPE_EMPTY -> {
                val binding = DataBindingUtil.inflate<ItemEmptyBinding>(
                    LayoutInflater.from(context),
                    R.layout.item_empty,
                    parent,
                    false
                )
                EmptyViewHolder(binding)
            }
            TYPE_DAY_OF_WEEK -> {
                val binding = DataBindingUtil.inflate<ItemDayOfWeekBinding>(
                    LayoutInflater.from(context),
                    R.layout.item_day_of_week,
                    parent,
                    false
                )

                // Measure height of layout of day of week
                if(dayOfWeekHeight < 0) {
                    binding.itemDayOfWeekLayout.measure(
                            View.MeasureSpec.UNSPECIFIED,
                            View.MeasureSpec.UNSPECIFIED
                    )

                    dayOfWeekHeight = binding.itemDayOfWeekLayout.measuredHeight
                }

                DayOfWeekViewHolder(binding)
            }
            TYPE_DAY_OF_MONTH -> {
                val binding = DataBindingUtil.inflate<ItemDayOfMonthBinding>(
                        LayoutInflater.from(context),
                        R.layout.item_day_of_month,
                        parent,
                        false
                )

                // To make calendar fit to recyclerview width and height
                val itemHeight = (parent.measuredHeight - dayOfWeekHeight) / DateUtils.DEFAULT_LINE
                binding.itemDayOfMonthLayout.layoutParams.height = itemHeight

                DayOfMonthViewHolder(binding)
            }
            else -> {
                val binding = DataBindingUtil.inflate<ItemEmptyBinding>(
                    LayoutInflater.from(context),
                    R.layout.item_empty,
                    parent,
                    false
                )

                EmptyViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder()")
        val yearInt = calendarItems[holder.adapterPosition].first
        val monthInt = calendarItems[holder.adapterPosition].second
        val dayInt = calendarItems[holder.adapterPosition].third

        when(getItemViewType(holder.adapterPosition)) {
            TYPE_DAY_OF_WEEK -> {
                val weekday = DateFormatSymbols().shortWeekdays[dayInt]
                (holder as DayOfWeekViewHolder).binding.apply {
                    day = weekday

                    // set color
                    when {
                        weekendCeasePositions.contains(holder.adapterPosition) -> {
                            itemDayOfWeekText.setTextColor(ContextCompat.getColor(context, R.color.red))
                        }
                        weekendOnSetPositions.contains(holder.adapterPosition) -> {
                            itemDayOfWeekText.setTextColor(ContextCompat.getColor(context, R.color.teal_700))
                        }
                        else -> {
                            itemDayOfWeekText.setTextColor(ContextCompat.getColor(context, R.color.black))
                        }
                    }
                }
            }
            TYPE_DAY_OF_MONTH -> {
                val content = contentItems.filter { it.first == "$yearInt$monthInt$dayInt".toLong() }

                (holder as DayOfMonthViewHolder).binding.apply {
                    index = holder.adapterPosition
                    calendar = dateUtils.calendar
                    year = yearInt
                    month = monthInt
                    day = dayInt

                    // set color
                    val textColor = when {
                        yearInt != dateUtils.calendar.get(Calendar.YEAR) || monthInt != dateUtils.calendar.get(Calendar.MONTH) -> {
                            ContextCompat.getColor(context, R.color.gray)
                        }
                        weekendCeasePositions.contains(holder.adapterPosition) -> {
                            ContextCompat.getColor(context, R.color.red)
                        }
                        weekendOnSetPositions.contains(holder.adapterPosition) -> {
                            ContextCompat.getColor(context, R.color.teal_700)
                        }
                        else -> {
                            ContextCompat.getColor(context, R.color.black)
                        }
                    }
                    itemDayOfMonthDay.setTextColor(textColor)

                    itemDayOfMonthLayout.setOnClickListener {
                    }

                    tracker?.let {
                        itemDayOfMonthLayout.isActivated = it.isSelected(holder.itemId)
                    }
                }
            }
            TYPE_EMPTY -> { }
            else -> { }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        Log.d(TAG, "onViewAttachedToWindow()")
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        Log.d(TAG, "onViewDetachedFromWindow()")
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        Log.d(TAG, "onAttachedToRecyclerView()")
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        Log.d(TAG, "onDetachedFromRecyclerView()")
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        Log.d(TAG, "onFailedToRecycleView()")
        return super.onFailedToRecycleView(holder)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        Log.d(TAG, "onViewRecycled()")
    }

    inner class EmptyViewHolder(binding: ItemEmptyBinding): RecyclerView.ViewHolder(binding.root) {
        fun getItemDetails() = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int {
                return adapterPosition
            }

            override fun getSelectionKey(): Long {
                return itemId
            }
        }
    }

    inner class DayOfWeekViewHolder(val binding: ItemDayOfWeekBinding): RecyclerView.ViewHolder(binding.root) {
        fun getItemDetails() = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int {
                return adapterPosition
            }

            override fun getSelectionKey(): Long {
                return itemId
            }
        }
    }

    inner class DayOfMonthViewHolder(val binding: ItemDayOfMonthBinding): RecyclerView.ViewHolder(binding.root) {
        fun getItemDetails() = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int {
                return adapterPosition
            }

            override fun getSelectionKey(): Long {
                return itemId
            }
        }
    }

    companion object {
        const val TAG = "CalendarViewAdapter"

        const val TYPE_EMPTY = 0
        const val TYPE_DAY_OF_WEEK = 1
        const val TYPE_DAY_OF_MONTH = 2
    }
}
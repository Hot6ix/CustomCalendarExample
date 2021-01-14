package com.example.customcalendar

import android.icu.util.Calendar

class DateUtils(var calendar: Calendar = Calendar.getInstance()) {

    companion object {
        const val DEFAULT_LINE = 6
    }

    fun getDaysOfWeek(): ArrayList<Int> {
        return with(calendar) {
            val list = ArrayList<Int>()

            var dayOfWeek = firstDayOfWeek
            for(i in 0 until 7) {
                list.add(dayOfWeek)

                dayOfWeek++
                if(dayOfWeek > 7) dayOfWeek = 1
            }

            list
        }
    }

    fun getDaysOfMonthWithDayOfWeek(): LinkedHashMap<Int, Int> {
        val map = LinkedHashMap<Int, Int>()

        return with(calendar) {
            val first = getActualMinimum(Calendar.DAY_OF_MONTH)
            val last = getActualMaximum(Calendar.DAY_OF_MONTH)

            set(Calendar.DAY_OF_MONTH, first)

            for(i in first..last) {
                (clone() as Calendar).run {
                    set(Calendar.DAY_OF_MONTH, i)
                    map.put(i, get(Calendar.DAY_OF_WEEK))
                }
            }

            map
        }
    }

    fun getDaysOfMonthWithCalendarFormat(line: Int = DEFAULT_LINE): ArrayList<Triple<Int, Int, Int>> {
        val maxItems = line * 7
        val list = ArrayList<Triple<Int, Int, Int>>()

        return with(calendar) {
            val first = getActualMinimum(Calendar.DAY_OF_MONTH)
            val last = getActualMaximum(Calendar.DAY_OF_MONTH)

            set(Calendar.DAY_OF_MONTH, first)

            val dayOfWeek = get(Calendar.DAY_OF_WEEK)

            val previousMonth = (calendar.clone() as Calendar).apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }

            // add days of previous month
            if(dayOfWeek >= firstDayOfWeek) {
                previousMonth.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - firstDayOfWeek) + 1)

                for(i in firstDayOfWeek until dayOfWeek) {
                    list.add(
                            Triple(
                                    previousMonth.get(Calendar.YEAR),
                                    previousMonth.get(Calendar.MONTH),
                                    previousMonth.get(Calendar.DAY_OF_MONTH)
                            )
                    )
                    previousMonth.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            else {
                previousMonth.add(Calendar.DAY_OF_MONTH, -dayOfWeek + 1)

                for(i in 0 until dayOfWeek) {
                    list.add(
                            Triple(
                                    previousMonth.get(Calendar.YEAR),
                                    previousMonth.get(Calendar.MONTH),
                                    previousMonth.get(Calendar.DAY_OF_MONTH)
                            )
                    )
                    previousMonth.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            // add days of current month
            for(i in first..last) {
                list.add(
                        Triple(
                                get(Calendar.YEAR),
                                get(Calendar.MONTH),
                                i
                        )
                )
            }

            // add days of next month
            if(list.size < maxItems) {
                val nextMonth = (calendar.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                    set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
                }

                while(list.size < maxItems) {
                    list.add(
                            Triple(
                                    nextMonth.get(Calendar.YEAR),
                                    nextMonth.get(Calendar.MONTH),
                                    nextMonth.get(Calendar.DAY_OF_MONTH)
                            )
                    )
                    nextMonth.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            list
        }

    }

    fun hasToday(): Int {
        val today = Calendar.getInstance()

        return when {
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) -> {
                today.get(Calendar.DAY_OF_MONTH)
            }
            else -> -1
        }
    }

    fun getIndexOfWeekendOnSet(list: ArrayList<Int>): Int {
        return list.indexOf(calendar.weekData.weekendOnset)
    }

    fun getIndexOfWeekendCease(list: ArrayList<Int>): Int {
        return list.indexOf(calendar.weekData.weekendCease)
    }
}
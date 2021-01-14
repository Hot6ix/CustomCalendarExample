package com.example.customcalendar

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DateUtilsTest {

    companion object {
        const val TAG = "DateUtilsTest"
    }

    private val dispatcher = newSingleThreadContext("Thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        dispatcher.close()
    }

    /*
        Reference: https://developer.android.com/reference/android/icu/util/Calendar
        In the reference, some countries use Sunday for the first day of week and some of other countries use Monday.
        US: Sunday, France: Monday, Iran: Saturday
     */

    @Test
    fun testManualGetDaysOfWeek() = runBlockingTest {
        launch {
            // set first day of week to Sunday manually
            with(Calendar.getInstance()) {
                firstDayOfWeek = Calendar.SUNDAY

                val list = DateUtils(this).getDaysOfWeek()
                Log.d(TAG, list.joinToString())
                assertTrue(list.isNotEmpty() && list.size == 7)
                assertTrue(list[0] == Calendar.SUNDAY && list.last() == Calendar.SATURDAY)
            }

            // set first day of week to Monday manually
            with(Calendar.getInstance()) {
                firstDayOfWeek = Calendar.MONDAY

                val list = DateUtils(this).getDaysOfWeek()
                Log.d(TAG, list.joinToString())
                assertTrue(list.isNotEmpty() && list.size == 7)
                assertTrue(list[0] == Calendar.MONDAY && list.last() == Calendar.SUNDAY)
            }

            // set first day of week to Saturday manually
            with(Calendar.getInstance()) {
                firstDayOfWeek = Calendar.SATURDAY

                val list = DateUtils(this).getDaysOfWeek()
                Log.d(TAG, list.joinToString())
                assertTrue(list.isNotEmpty() && list.size == 7)
                assertTrue(list[0] == Calendar.SATURDAY && list.last() == Calendar.FRIDAY)
            }
        }
    }

    @Test
    fun testLocaleBasedGetDaysOfWeek() = runBlockingTest {
        // use calendar with locale US where first day of week is Sunday
        with(Calendar.getInstance(Locale.US)) {
            val list = DateUtils(this).getDaysOfWeek()
            Log.d(TAG, list.joinToString())
            assertTrue(list.isNotEmpty() && list.size == 7)
            assertTrue(list[0] == Calendar.SUNDAY && list.last() == Calendar.SATURDAY)
        }

        // use calendar with locale France where first day of week is Monday
        with(Calendar.getInstance(Locale.FRANCE)) {
            val list = DateUtils(this).getDaysOfWeek()
            Log.d(TAG, list.joinToString())
            assertTrue(list.isNotEmpty() && list.size == 7)
            assertTrue(list[0] == Calendar.MONDAY && list.last() == Calendar.SUNDAY)
        }

        // use calendar with locale Iran where first day of week is Saturday
        with(Calendar.getInstance(Locale("fa_IR", "IR"))) {
            val list = DateUtils(this).getDaysOfWeek()
            Log.d(TAG, list.joinToString())
            assertTrue(list.isNotEmpty() && list.size == 7)
            assertTrue(list[0] == Calendar.SATURDAY && list.last() == Calendar.FRIDAY)
        }
    }

    @Test
    fun testGetDaysOfMonth() {
        // 1 Jan 2021, Friday
        Log.d(TAG, "testGetDaysOfMonth() #1")
        with(Calendar.getInstance()) {
            set(Calendar.YEAR, 2021)
            set(Calendar.MONTH, Calendar.JANUARY)

            val map = DateUtils(this).getDaysOfMonthWithDayOfWeek()
            Log.d(TAG, map.toString())
            assertTrue(map[getActualMinimum(Calendar.DAY_OF_MONTH)] == Calendar.FRIDAY)
            assertTrue(map.size == getActualMaximum(Calendar.DAY_OF_MONTH))
        }

        // 1 Feb 2021, Monday
        Log.d(TAG, "testGetDaysOfMonth() #2")
        with(Calendar.getInstance()) {
            set(Calendar.YEAR, 2021)
            set(Calendar.MONTH, Calendar.FEBRUARY)

            val map = DateUtils(this).getDaysOfMonthWithDayOfWeek()
            Log.d(TAG, map.toString())
            assertTrue(map[getActualMinimum(Calendar.DAY_OF_MONTH)] == Calendar.MONDAY)
            assertTrue(map.size == getActualMaximum(Calendar.DAY_OF_MONTH))
        }
    }

    @Test
    fun testManualGetDaysOfMonthCalendar() = runBlockingTest {
        val line = 6

        launch {
            // 1 Jan 2021, Friday
            // Set first day of week to Sunday manually
            with(Calendar.getInstance()) {
                set(Calendar.YEAR, 2021)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
                firstDayOfWeek = Calendar.SUNDAY

                val list = DateUtils(this).getDaysOfMonthWithCalendarFormat()

                Log.d(TAG, "Parameter: Date/Time={${SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL).format(this)}}," +
                        " firstDayOfWeek={1, Sunday}")
                Log.d(TAG, list.joinToString())
                // Check if the first day of the month is right position
                assertTrue(list.indexOf(Pair(get(Calendar.MONTH), 1)) == 5)
                assertTrue(list.size == line * 7)
            }

            // 1 Jan 2021, Friday
            // Set first day of week to Monday manually
            with(Calendar.getInstance()) {
                set(Calendar.YEAR, 2021)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
                firstDayOfWeek = Calendar.MONDAY

                val list = DateUtils(this).getDaysOfMonthWithCalendarFormat()

                Log.d(TAG, "Parameter: Date/Time={${SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL).format(this)}}," +
                        " firstDayOfWeek={2, Monday}")
                Log.d(TAG, list.joinToString())
                // Check if the first day of the month is right position
                assertTrue(list.indexOf(Pair(get(Calendar.MONTH), 1)) == 4)
                assertTrue(list.size == line * 7)
            }

            // 1 Jan 2021, Friday
            // Set first day of week to Tuesday manually
            with(Calendar.getInstance()) {
                set(Calendar.YEAR, 2021)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))
                firstDayOfWeek = Calendar.TUESDAY

                val list = DateUtils(this).getDaysOfMonthWithCalendarFormat()

                Log.d(TAG, "Parameter: Date/Time={${SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL).format(this)}}," +
                        " firstDayOfWeek={3, Tuesday}")
                Log.d(TAG, list.joinToString())
                // Check if the first day of the month is right position
                assertTrue(list.indexOf(Pair(get(Calendar.MONTH), 1)) == 3)
                assertTrue(list.size == line * 7)
            }
        }
    }

    @Test
    fun testLocaleBasedGetDaysOfMonthCalendar() = runBlockingTest {
        val line = 6

        // 1 Jan 2021, Friday
        // Locale: US
        // Sunday is the first day of week
        with(Calendar.getInstance(Locale.US)) {
            set(Calendar.YEAR, 2021)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))

            val list = DateUtils(this).getDaysOfMonthWithCalendarFormat(line)

            Log.d(TAG, "Parameter: Date/Time={${SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL).format(this)}}," +
                    " Locale={${Locale.US.displayCountry}}")
            Log.d(TAG, list.joinToString())
            // Check if the first day of the month is right position
            assertTrue(list.indexOf(Pair(get(Calendar.MONTH), 1)) == 5)
            assertTrue(list.size == line * 7)
        }

        // 1 Jan 2021, Friday
        // Locale: France
        // Sunday is the first day of week
        with(Calendar.getInstance(Locale.FRANCE)) {
            set(Calendar.YEAR, 2021)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))

            val list = DateUtils(this).getDaysOfMonthWithCalendarFormat(line)

            Log.d(TAG, "Parameter: Date/Time={${SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL).format(this)}}," +
                    " Locale={${Locale.FRANCE.displayCountry}}")
            Log.d(TAG, list.joinToString())
            // Check if the first day of the month is right position
            assertTrue(list.indexOf(Pair(get(Calendar.MONTH), 1)) == 4)
            assertTrue(list.size == line * 7)
        }

        // 1 Jan 2021, Friday
        // Locale: Iran
        // Saturday is the first day of week
        with(Calendar.getInstance(Locale("fa_IR", "IR"))) {
            set(Calendar.YEAR, 2021)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))

            val list = DateUtils(this).getDaysOfMonthWithCalendarFormat(line)

            Log.d(TAG, "Parameter: Date/Time={${SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL).format(this)}}," +
                    " Locale={${Locale("fa_IR", "IR").displayCountry}}")
            Log.d(TAG, list.joinToString())
            // Check if the first day of the month is right position
            assertTrue(list.indexOf(Pair(get(Calendar.MONTH), 1)) == 6)
            assertTrue(list.size == line * 7)
        }

        // 1 May 2021, Friday
        // Locale: Iran
        // Saturday is the first day of week
        with(Calendar.getInstance(Locale("fa_IR", "IR"))) {
            set(Calendar.YEAR, 2021)
            set(Calendar.MONTH, Calendar.MAY)
            set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))

            val list = DateUtils(this).getDaysOfMonthWithCalendarFormat(line)

            Log.d(TAG, "Parameter: Date/Time={${SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL).format(this)}}," +
                    " Locale={${Locale("fa_IR", "IR").displayCountry}}")
            Log.d(TAG, list.joinToString())
            // Check if the first day of the month is right position
            assertTrue(list.indexOf(Pair(get(Calendar.MONTH), 1)) == 0)
            assertTrue(list.size == line * 7)
        }

        // 2021
        // Locale: US
        // Sunday is the first day of week
        for(i in 0..11) {
            with(Calendar.getInstance(Locale.US)) {
                set(Calendar.YEAR, 2021)
                set(Calendar.MONTH, i)
                set(Calendar.DAY_OF_MONTH, getActualMinimum(Calendar.DAY_OF_MONTH))

                val list = DateUtils(this).getDaysOfMonthWithCalendarFormat(line)

                Log.d(TAG, "Parameter: Date/Time={${SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL).format(this)}}," +
                        " Locale={${Locale.US.displayCountry}}")
                Log.d(TAG, "Month=${i}, firstDay=${getActualMinimum(Calendar.DAY_OF_MONTH)}," +
                        "lastDay=${getActualMaximum(Calendar.DAY_OF_MONTH)}")
                Log.d(TAG, "Days={${list.joinToString()}}")
                // Check if the first day of the month is right position
                assertTrue(list.indexOf(Pair(get(Calendar.MONTH), 1)) == get(Calendar.DAY_OF_WEEK) - 1)
                assertTrue(list.size == line * 7)
            }
        }
    }

    fun testHasToday() {

    }

    @Test
    fun testWeekend() {
        with(Calendar.getInstance(Locale.US)) {
            Log.d(TAG, "${weekData.weekendCease}, ${weekData.weekendOnset}")
        }
        with(Calendar.getInstance(Locale.FRANCE)) {
            Log.d(TAG, "${weekData.weekendCease}, ${weekData.weekendOnset}")
        }
        with(Calendar.getInstance(Locale("fa_IR", "IR"))) {
            Log.d(TAG, "${weekData.weekendCease}, ${weekData.weekendOnset}")
        }
        with(Calendar.getInstance(Locale.CHINA)) {
            Log.d(TAG, "${weekData.weekendCease}, ${weekData.weekendOnset}")
        }
        with(Calendar.getInstance(Locale.KOREA)) {
            Log.d(TAG, "${weekData.weekendCease}, ${weekData.weekendOnset}")
        }
        with(Calendar.getInstance(Locale("ar_SA", "SA"))) {
            Log.d(TAG, "${weekData.weekendCease}, ${weekData.weekendOnset}")
        }
    }
}
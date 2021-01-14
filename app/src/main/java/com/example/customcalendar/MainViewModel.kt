package com.example.customcalendar

import android.icu.util.Calendar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val calendar: MutableLiveData<Calendar> by lazy {
        MutableLiveData<Calendar>().also {
            it.value = Calendar.getInstance()
        }
    }

    fun getCalendar(): Calendar? {
        return calendar.value
    }

    fun setCalendar(calendar: Calendar?): Boolean {
        if(calendar == null) return false

        this.calendar.value = calendar
        return true
    }
}
package com.example.customcalendar

import android.icu.text.DateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.customcalendar.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerViewAdapter = CalendarViewAdapter(applicationContext).apply {
            val array = arrayOf(
                    Pair(2021011L, "1"),
                    Pair(2021011L, "2"),
                    Pair(2021011L, "3"),
                    Pair(2021011L, "4"),
                    Pair(2021012L, "1"),
            )
            updateContents(array)
        }

        binding.calendar.apply {
            adapter = recyclerViewAdapter
            itemAnimator = CalendarItemAnimator()
            layoutManager = StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(false)
            recycledViewPool.setMaxRecycledViews(CalendarViewAdapter.TYPE_DAY_OF_WEEK, 0)
            recycledViewPool.setMaxRecycledViews(CalendarViewAdapter.TYPE_DAY_OF_MONTH, 0)
        }

        val tracker = SelectionTracker.Builder(
            "selection_id",
            binding.calendar,
            StableIdKeyProvider(binding.calendar),
            RecyclerViewDetailsLookup(binding.calendar),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

        recyclerViewAdapter.attachTracker(tracker)

        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                super.onItemStateChanged(key, selected)
                Log.d("tagggg", "$key, $selected")
            }
        })

        viewModel.calendar.observe(this, {
            supportActionBar?.title = DateFormat.getPatternInstance(DateFormat.YEAR_MONTH).format(it)
            recyclerViewAdapter.updateCalendar(it)
        })

        // Click events
        binding.add.setOnClickListener {
            viewModel.setCalendar(viewModel.calendar.value?.apply { add(Calendar.MONTH, 1) })
        }

        binding.subtract.setOnClickListener {
            viewModel.setCalendar(viewModel.calendar.value?.apply { add(Calendar.MONTH, -1) })
        }

        binding.localeUs.setOnClickListener {
            val c = Calendar.getInstance(Locale.US).apply {
                timeInMillis = viewModel.getCalendar()?.timeInMillis ?: 0
            }
            viewModel.setCalendar(c)
        }
        binding.localeFrance.setOnClickListener {
            viewModel.setCalendar(Calendar.getInstance(Locale.FRANCE))
        }
        binding.localeIran.setOnClickListener {
            viewModel.setCalendar(Calendar.getInstance(Locale("fa_IR", "IR")))
        }
        binding.showLayout.setOnCheckedChangeListener { buttonView, isChecked ->
            val set = ConstraintSet().apply {
                clone(binding.mainLayout)
            }

            if(isChecked) {
                set.connect(binding.bottomLayout.id, ConstraintSet.TOP, binding.calendar.id, ConstraintSet.BOTTOM)
            }
            else {
                set.clear(binding.bottomLayout.id, ConstraintSet.TOP)
            }

            val transition = AutoTransition().apply {
                duration = 250
                interpolator = AccelerateDecelerateInterpolator()
            }

            TransitionManager.beginDelayedTransition(binding.mainLayout, transition)
            set.applyTo(binding.mainLayout)

            (binding.calendar.adapter as CalendarViewAdapter?)?.notifyAllItemsChanged()
        }
    }
}
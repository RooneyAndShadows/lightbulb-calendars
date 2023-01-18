package com.github.rooneyandshadows.lightbulb.calendars.month.adapter

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.rooneyandshadows.lightbulb.calendars.month.MonthCalendarView
import com.github.rooneyandshadows.lightbulb.calendars.month.adapter.MonthsAdapter.*
import com.github.rooneyandshadows.java.commons.date.DateUtils
import com.github.rooneyandshadows.java.commons.date.DateUtilsOffsetDate
import java.util.*

internal class YearPageView : RecyclerView {
    private lateinit var calendarView: MonthCalendarView
    private var year = 0

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        isSaveEnabled = true
    }

    constructor(year: Int, calendarView: MonthCalendarView) : super(calendarView.context) {
        this.year = year
        this.calendarView = calendarView
        itemAnimator = null
        adapter = MonthsAdapter(calendarView, generateMonthsForYear())
        overScrollMode = OVER_SCROLL_NEVER
        layoutManager = GridLayoutManager(context, 4, VERTICAL, false)
    }

    @Override
    override fun getAdapter(): MonthsAdapter? {
        return super.getAdapter() as MonthsAdapter?
    }

    fun select(month: Int) {
        adapter!!.selectMonth(month)
    }

    fun clearSelection() {
        adapter!!.clearSelection()
    }

    private fun generateMonthsForYear(): MutableList<MonthItem> {
        val items: MutableList<MonthItem> = mutableListOf()
        for (date in DateUtilsOffsetDate.getAllMonthsForYear(year)) {
            val currentMonth = MonthEntry.fromDate(date)
            val calendarMonth = calendarView.selection
            val isSelected = currentMonth.compare(calendarMonth)
            val isEnabled = calendarView.isMonthEnabled(currentMonth)
            items.apply {
                add(MonthItem(currentMonth, isSelected, isEnabled))
            }
        }
        return items
    }
}
package com.github.rooneyandshadows.lightbulb.calendars.month.adapter

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.rooneyandshadows.lightbulb.calendars.month.MonthCalendarView
import com.github.rooneyandshadows.lightbulb.calendars.month.adapter.MonthsAdapter.*
import com.github.rooneyandshadows.java.commons.date.DateUtils
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
        for (date in DateUtils.getAllMonthsForYear(year)) {
            val currentYear = DateUtils.extractYearFromDate(date)
            val currentMonth = DateUtils.extractMonthOfYearFromDate(date)
            val currentMonthAsArray = intArrayOf(currentYear, currentMonth)
            val calendarSelectionAsArray = calendarView.selectionAsArray
            val enabled = calendarView.isMonthEnabled(currentMonthAsArray[0], currentMonthAsArray[1])
            val selected = Arrays.equals(calendarSelectionAsArray, currentMonthAsArray)
            items.add(MonthItem(currentYear, currentMonth, selected, enabled))
        }
        return items
    }
}
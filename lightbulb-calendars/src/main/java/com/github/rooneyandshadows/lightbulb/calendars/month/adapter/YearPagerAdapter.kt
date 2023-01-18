package com.github.rooneyandshadows.lightbulb.calendars.month.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.github.rooneyandshadows.lightbulb.calendars.month.MonthCalendarView
import java.time.OffsetDateTime
import java.util.*

@Suppress("unused")
internal class YearPagerAdapter(private val calendarView: MonthCalendarView, var minYear: Int, var maxYear: Int) :
    PagerAdapter() {
    private var selection: MonthEntry? = null
    private val years = ArrayList<Int>()
    private val gridViews: MutableMap<Int, YearPageView> = mutableMapOf()
    val disabledMonths: MutableList<MonthEntry> = mutableListOf()
    val enabledMonths: MutableList<MonthEntry> = mutableListOf()

    init {
        setBounds(minYear, maxYear)
    }

    val selectedMonthAsArray: IntArray?
        get() = selection?.toArray()
    val selectedMonthAsDate: OffsetDateTime?
        get() = selection?.toDate()
    val selectedMonth: MonthEntry?
        get() = selection


    @Override
    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as ViewGroup)
        view.removeAllViews()
        gridViews.remove(position)
    }

    @Override
    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val year = years[position]
        val grid = YearPageView(year, calendarView)
        gridViews[position] = grid
        collection.addView(grid)
        return grid
    }

    @Override
    override fun getCount(): Int {
        return years.size
    }

    @Override
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    @Override
    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    fun setBounds(min: Int, max: Int) {
        if (min > max) {
            maxYear = min
            minYear = max
        } else {
            minYear = min
            maxYear = max
        }
        initYearModels()
    }

    private fun initYearModels() {
        years.clear()
        for (year in minYear..maxYear) years.add(year)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        if (selection == null) return
        val yearView = getPageByYear(selection!!.year)
        yearView?.clearSelection()
        selection = null
    }

    fun isMonthEnabled(month: MonthEntry): Boolean {
        return isMonthEnabled(month.year, month.month)
    }

    fun isMonthEnabled(year: Int, month: Int): Boolean {
        if (enabledMonths.isNotEmpty())
            return enabledMonths.any { return it.compare(year, month) }
        if (disabledMonths.isNotEmpty())
            return disabledMonths.any { return it.compare(year, month) }
        return true
    }

    fun select(newSelection: MonthEntry) {
        val month = newSelection.getWithinYearBounds(minYear, maxYear)
        if (!isMonthEnabled(month)) return
        clearSelection()
        selection = month
        getPageByYear(selection!!.year)?.apply {
            select(selection!!.month)
        }
    }

    fun select(year: Int, month: Int) {
        select(MonthEntry(year, month))
    }

    fun setEnabledMonths(enabled: List<MonthEntry>) {
        enabledMonths.apply {
            clear()
            addAll(enabled)
            if (isNotEmpty()) {
                minYear = first().year
                maxYear = last().year
                var clearCurrentSelection = true
                forEach { enabledMonth ->
                    val currentYear = enabledMonth.year
                    if (enabledMonth.compare(selection))
                        clearCurrentSelection = false
                    if (currentYear < minYear) minYear = currentYear
                    if (currentYear > maxYear) maxYear = currentYear
                }
                if (clearCurrentSelection) clearSelection()
            }
        }
        initYearModels()
    }

    fun setDisabledMonths(disabled: List<MonthEntry>) {
        disabledMonths.apply {
            clear()
            addAll(disabled)
            if (any { it.compare(selection) })
                clearSelection()
        }
        initYearModels()
    }

    private fun getPageByYear(year: Int): YearPageView? {
        val yearIndex = getPagePositionByYear(year)
        return if (yearIndex == -1) null else gridViews[yearIndex]
    }

    fun getYearByPosition(position: Int): Int {
        return years[position]
    }

    fun getPagePositionByYear(targetYear: Int): Int {
        var index = -1
        for (i in years.indices) {
            val currentYear = years[i]
            if (currentYear == targetYear) index = i
        }
        return index
    }
}
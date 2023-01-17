package com.github.rooneyandshadowss.lightbulb.calendars.month.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.github.rooneyandshadowss.lightbulb.calendars.month.MonthCalendarView
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
        yearView!!.clearSelection()
        selection = null
    }

    fun isMonthEnabled(year: Int, month: Int): Boolean {
        val targetMonth = MonthEntry(year, month)
        if (enabledMonths.isNotEmpty())
            return enabledMonths.any { return it.compare(targetMonth) }
        if (disabledMonths.isNotEmpty())
            return disabledMonths.any { return it.compare(targetMonth) }
        return true
    }

    fun select(newYear: Int, newMonth: Int) {
        var year = newYear
        var month = newMonth
        if (year < minYear) year = minYear
        if (year > maxYear) year = maxYear
        if (month < 1) month = 1
        if (month > 12) month = 12
        val newSelection = MonthEntry(year, month)
        if (!isMonthEnabled(year, month)) return
        clearSelection()
        selection = newSelection
        val page = getPageByYear(selection!!.year)
        page?.select(selection!!.month)
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
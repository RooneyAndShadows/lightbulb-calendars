package com.github.rooneyandshadows.lightbulb.calendars.month

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.*
import com.github.rooneyandshadows.lightbulb.calendars.month.adapter.MonthEntry
import com.github.rooneyandshadows.lightbulb.calendars.month.adapter.YearPagerAdapter
import com.github.rooneyandshadows.java.commons.date.DateUtilsOffsetDate
import com.github.rooneyandshadows.lightbulb.calendars.R
import com.github.rooneyandshadows.lightbulb.calendars.R.styleable.*
import com.github.rooneyandshadows.lightbulb.commons.utils.ParcelUtils
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils
import java.time.OffsetDateTime
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Suppress("SameParameterValue", "MemberVisibilityCanBePrivate", "unused")
class MonthCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    private lateinit var headerYearTextView: TextView
    private lateinit var buttonYearPrev: AppCompatImageButton
    private lateinit var buttonYearNext: AppCompatImageButton
    private lateinit var pagerView: ViewPager
    private lateinit var adapter: YearPagerAdapter
    private var colorBackground: Int = -1
    private var colorHeaderArrows: Int = -1
    private var initialMinYear = 1970
    private var initialMaxYear = 2100
    private var dataBindingSelectionChangeListener: SelectionChangeListener? = null
    private val selectionListeners = ArrayList<SelectionChangeListener>()
    val selectionAsArray: IntArray?
        get() = adapter.selectedMonthAsArray
    val selectionAsDate: OffsetDateTime?
        get() = adapter.selectedMonthAsDate
    val selection: MonthEntry?
        get() = adapter.selectedMonth
    val minYear: Int
        get() = adapter.minYear
    val maxYear: Int
        get() = adapter.maxYear
    val enabledMonths: List<MonthEntry>
        get() = adapter.enabledMonths
    val disabledMonths: List<MonthEntry>
        get() = adapter.disabledMonths
    var currentShownYear = 0
        private set
    var monthTextColor: Int? = null
        private set
    var monthTextSelectedColor: Int? = null
        private set
    var backgroundColorSelected = 0
        private set
    var calendarItemSize = 0
        private set
    var tileSize = 0
        private set


    init {
        isSaveEnabled = true
        readAttributes(context, attrs)
        initView()
    }

    @Override
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredTileWidth: Int
        val desiredTileHeight: Int
        val measuredHeaderHeight = defaultHeaderHeight
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val horPadding = paddingLeft + paddingRight
        val verPadding = paddingTop + paddingBottom
        desiredTileWidth = when (widthSpecMode) {
            MeasureSpec.EXACTLY -> (widthSpecSize - horPadding) / 4
            MeasureSpec.AT_MOST -> min(calendarItemSize, (widthSpecSize - horPadding) / 4)
            MeasureSpec.UNSPECIFIED -> calendarItemSize
            else -> calendarItemSize
        }
        desiredTileHeight = when (heightSpecMode) {
            MeasureSpec.EXACTLY -> (heightSpecSize - verPadding - measuredHeaderHeight) / 3
            MeasureSpec.AT_MOST -> min(calendarItemSize, (heightSpecSize - verPadding - measuredHeaderHeight) / 3)
            MeasureSpec.UNSPECIFIED -> calendarItemSize
            else -> calendarItemSize
        }
        tileSize = max(desiredTileWidth, desiredTileHeight)
        val measuredWidth = tileSize * 4
        val measuredHeight = tileSize * 3 + measuredHeaderHeight
        //Put padding back in from when we took it away
        //measuredWidth += getPaddingLeft() + getPaddingRight();
        //measuredHeight += getPaddingTop() + getPaddingBottom();
        //Contract fulfilled, setting out measurements
        setMeasuredDimension( //We clamp inline because we want to use un-clamped versions on the children
            clampSize(measuredWidth + horPadding, widthMeasureSpec),
            clampSize(measuredHeight + verPadding, heightMeasureSpec)
        )
        //val count = childCount
        val header = getChildAt(0)
        val pager = getChildAt(1)
        val widthSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY)
        val heightHeaderSpec = MeasureSpec.makeMeasureSpec(measuredHeaderHeight, MeasureSpec.EXACTLY)
        val heightPagerSpec = MeasureSpec.makeMeasureSpec(tileSize * 3, MeasureSpec.EXACTLY)
        header.measure(widthSpec, heightHeaderSpec)
        pager.measure(widthSpec, heightPagerSpec)
    }

    @Override
    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    @Override
    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    @Override
    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val myState = SavedState(superState)
        myState.selectedMonth = selection
        myState.minYear = minYear
        myState.maxYear = maxYear
        myState.shownYear = currentShownYear
        myState.enabledMonths = enabledMonths
        myState.disabledMonths = disabledMonths
        return myState
    }

    @Override
    public override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        val minYear = savedState.minYear
        val maxYear = savedState.maxYear
        val previouslyEnabledMonths = savedState.enabledMonths
        val previouslyDisabledMonths = savedState.disabledMonths
        val previouslySelectedMonth = savedState.selectedMonth
        setCalendarBounds(minYear, maxYear)
        setEnabledMonths(previouslyEnabledMonths)
        setDisabledMonths(previouslyDisabledMonths)
        previouslySelectedMonth?.apply {
            setSelectedMonth(year, month)
        }
        goToYear(savedState.shownYear, false)
    }

    private fun readAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MonthCalendarView, 0, 0)
        try {
            val temp = TextView(context)
            val defMonthTextSelectedColor = ResourceUtils.getColorByAttribute(getContext(), R.attr.colorOnPrimary)
            val defMonthTextColor = temp.textColors.defaultColor
            val defBackgroundColorSelected = ResourceUtils.getColorByAttribute(getContext(), R.attr.colorPrimary)
            val defBackgroundColor = ResourceUtils.getColorById(getContext(), android.R.color.transparent)
            val defArrowsColor = ResourceUtils.getColorByAttribute(getContext(), android.R.attr.colorAccent)
            val defCalendarItemSize = ResourceUtils.getDimenPxById(context, R.dimen.month_calendar_item_size)
            monthTextSelectedColor = a.getColor(
                MonthCalendarView_mcv_selectedMonthTextColor,
                defMonthTextSelectedColor
            )
            monthTextColor = a.getColor(
                MonthCalendarView_mcv_monthTextColor,
                defMonthTextColor
            )
            backgroundColorSelected = a.getColor(
                MonthCalendarView_mcv_monthSelectionBackgroundColor,
                defBackgroundColorSelected
            )
            colorBackground = a.getColor(
                MonthCalendarView_mcv_backgroundColor,
                defBackgroundColor
            )
            colorHeaderArrows = a.getColor(
                MonthCalendarView_mcv_monthSelectionBackgroundColor,
                defArrowsColor
            )
            calendarItemSize = a.getDimensionPixelSize(
                MonthCalendarView_mcv_calendarItemSize,
                defCalendarItemSize
            )
            initialMinYear = a.getInteger(
                MonthCalendarView_mcv_minYear,
                initialMinYear
            )
            initialMaxYear = a.getInteger(
                MonthCalendarView_mcv_maxYear,
                initialMaxYear
            )
        } finally {
            a.recycle()
        }
    }

    private fun initView() {
        selectViews()
        customizeViews()
        setupPager()
        if (!isInEditMode) {
            setupEvents()
            updateHeader()
            handleInitialPosition()
        }
    }

    private fun setupPager() {
        adapter = YearPagerAdapter(this, initialMinYear, initialMaxYear)
        pagerView.adapter = adapter
    }

    fun setCalendarBounds(minYear: Int, maxYear: Int) {
        adapter.setBounds(minYear, maxYear)
        handleInitialPosition()
    }

    private fun selectViews() {
        orientation = VERTICAL
        inflate(context, R.layout.view_month_calendar, this)
        pagerView = findViewById(R.id.pager)
        buttonYearPrev = findViewById(R.id.buttonPrevYear)
        buttonYearNext = findViewById(R.id.buttonNextYear)
        headerYearTextView = findViewById(R.id.header_year_text_view)
    }

    private fun customizeViews() {
        setBackgroundColor(colorBackground)
        buttonYearPrev.drawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            colorHeaderArrows,
            BlendModeCompat.SRC_ATOP
        )
        buttonYearNext.drawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            colorHeaderArrows,
            BlendModeCompat.SRC_ATOP
        )
        //pagerView.setLayoutParams(new LinearLayout.LayoutParams(4 * calendarItemSize, 3 * calendarItemSize));
        pagerView.setPageTransformer(false) { page: View, position: Float ->
            val alphaValue = sqrt((1 - abs(position)).toDouble()).toFloat()
            page.alpha = alphaValue
        }
    }

    private fun setupEvents() {
        buttonYearPrev.setOnClickListener { goToPreviousPage(true) }
        buttonYearNext.setOnClickListener { goToNextPage(true) }
        pagerView.addOnPageChangeListener(object : OnPageChangeListener {
            @Override
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            @Override
            override fun onPageSelected(position: Int) {
                currentShownYear = adapter.getYearByPosition(pagerView.currentItem)
                updateHeader()
            }

            @Override
            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    private fun updateHeader() {
        headerYearTextView.text = currentShownYear.toString()
    }

    private fun handleInitialPosition() {
        val currentYear = DateUtilsOffsetDate.extractYearFromDate(DateUtilsOffsetDate.nowLocal())
        if (currentYear < adapter.minYear || currentYear > adapter.maxYear)
            goToYear(adapter.maxYear, false)
        else goToYear(currentYear, false)
    }

    fun removeSelectionChangeListener(listener: SelectionChangeListener) {
        selectionListeners.remove(listener)
    }

    fun addSelectionChangeListener(listener: SelectionChangeListener) {
        selectionListeners.add(listener)
    }

    /**
     * Method is used to select month.
     *
     * @param month desired month to select
     */
    fun setSelectedMonth(month: MonthEntry) {
        selectMonthInternally(month, true)
    }

    /**
     * Method is used to select month.
     *
     * @param year  desired year to select
     * @param month desired month to select
     */
    fun setSelectedMonth(year: Int, month: Int) {
        selectMonthInternally(MonthEntry(year, month), true)
    }

    /**
     * Method is used to select month and scroll to it's position.
     *
     * @param month        desired month to select
     * @param smoothScroll scroll animation enabled
     */
    @JvmOverloads
    fun setSelectedMonthAndScrollToYear(month: MonthEntry, smoothScroll: Boolean = true) {
        selectMonthInternally(month, true)
        goToYear(month.year, smoothScroll)
    }

    /**
     * Method is used to select month and scroll to it's position.
     *
     * @param year         desired year to select
     * @param month        desired month to select
     * @param smoothScroll scroll animation enabled
     */
    @JvmOverloads
    fun setSelectedMonthAndScrollToYear(year: Int, month: Int, smoothScroll: Boolean = true) {
        selectMonthInternally(MonthEntry(year, month), true)
        goToYear(year, smoothScroll)
    }

    private fun selectMonthInternally(monthEntry: MonthEntry, triggerChangeEvents: Boolean) {
        val newSelection = monthEntry.getWithinYearBounds(minYear, maxYear)
        if (!isMonthEnabled(monthEntry) || newSelection.compare(selection)) return
        val oldSelection = adapter.selectedMonth
        adapter.select(newSelection)
        if (!triggerChangeEvents) return
        dispatchSelectionChangeListeners(newSelection, oldSelection)
    }

    fun clearSelection() {
        val oldSelection = adapter.selectedMonth
        adapter.clearSelection()
        dispatchSelectionChangeListeners(null, oldSelection)
    }

    fun setDisabledMonths(disabledMonths: List<MonthEntry>) {
        adapter.setDisabledMonths(disabledMonths)
        handleInitialPosition()
    }

    fun setEnabledMonths(enabledMonths: List<MonthEntry>) {
        adapter.setEnabledMonths(enabledMonths)
        handleInitialPosition()
    }

    fun isMonthEnabled(month: MonthEntry): Boolean {
        return isMonthEnabled(month.year, month.month)
    }

    fun isMonthEnabled(year: Int, month: Int): Boolean {
        return adapter.isMonthEnabled(year, month)
    }

    @JvmOverloads
    fun goToPreviousPage(smoothScroll: Boolean = true) {
        pagerView.setCurrentItem(pagerView.currentItem - 1, smoothScroll)
        updateHeader()
    }

    @JvmOverloads
    fun goToNextPage(smoothScroll: Boolean = true) {
        pagerView.setCurrentItem(pagerView.currentItem + 1, smoothScroll)
        updateHeader()
    }

    @JvmOverloads
    fun goToYear(year: Int, smoothScroll: Boolean = true) {
        if (year > adapter.maxYear || year < adapter.minYear) return
        pagerView.setCurrentItem(adapter.getPagePositionByYear(year), smoothScroll)
        currentShownYear = year
        updateHeader()
    }

    private fun handleSelection(newSelection: MonthEntry?) {
        if (newSelection == null) {
            adapter.clearSelection()
            return
        }
        if (!isMonthEnabled(newSelection.year, newSelection.month)) return
        adapter.select(newSelection.year, newSelection.month)
        goToYear(newSelection.year, false)
    }

    private fun dispatchSelectionChangeListeners(newSelection: MonthEntry?, oldSelection: MonthEntry?) {
        dataBindingSelectionChangeListener?.onSelectionChanged(this, newSelection, oldSelection)
        selectionListeners.forEach {
            it.onSelectionChanged(this, newSelection, oldSelection)
        }
    }

    private class SavedState : BaseSavedState {
        var enabledMonths: List<MonthEntry> = mutableListOf()
        var disabledMonths: List<MonthEntry> = mutableListOf()
        var selectedMonth: MonthEntry? = null
        var minYear: Int = -1
        var maxYear: Int = -1
        var shownYear: Int = 0
        var headerArrowsColor: Int = -1
        var backgroundColor: Int = -1
        private var adapterFilterType = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            enabledMonths = ParcelUtils.readTypedList(parcel, MonthEntry::class.java) as MutableList<MonthEntry>
            disabledMonths = ParcelUtils.readTypedList(parcel, MonthEntry::class.java) as MutableList<MonthEntry>
            selectedMonth = ParcelUtils.readParcelable(parcel, MonthEntry::class.java)
            minYear = parcel.readInt()
            maxYear = parcel.readInt()
            adapterFilterType = parcel.readInt()
            shownYear = parcel.readInt()
            headerArrowsColor = parcel.readInt()
            backgroundColor = parcel.readInt()

        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            ParcelUtils.writeTypedList(out, enabledMonths)
            ParcelUtils.writeTypedList(out, disabledMonths)
            ParcelUtils.writeParcelable(out, selectedMonth)
            out.writeInt(minYear)
            out.writeInt(maxYear)
            out.writeInt(adapterFilterType)
            out.writeInt(shownYear)
            out.writeInt(headerArrowsColor)
            out.writeInt(backgroundColor)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    interface SelectionChangeListener {
        fun onSelectionChanged(monthCalendarView: MonthCalendarView, newSelection: MonthEntry?, oldSelection: MonthEntry?)
    }

    companion object {
        private val defaultHeaderHeight: Int = ResourceUtils.dpToPx(40)
        private fun clampSize(size: Int, spec: Int): Int {
            val specMode = MeasureSpec.getMode(spec)
            val specSize = MeasureSpec.getSize(spec)
            return when (specMode) {
                MeasureSpec.EXACTLY -> specSize
                MeasureSpec.AT_MOST -> min(size, specSize)
                MeasureSpec.UNSPECIFIED -> size
                else -> size
            }
        }

        @BindingAdapter("monthCalendarSelection")
        fun updatePickerSelectionBinding(view: MonthCalendarView, selectedDate: OffsetDateTime?) {
            var newSelection: MonthEntry? = null
            selectedDate?.apply {
                val year = DateUtilsOffsetDate.extractYearFromDate(this)
                val month = DateUtilsOffsetDate.extractMonthOfYearFromDate(this)
                newSelection = MonthEntry(year, month)
            }
            view.handleSelection(newSelection)
        }

        @InverseBindingAdapter(attribute = "monthCalendarSelection", event = "monthCalendarSelectionChanged")
        fun getText(view: MonthCalendarView): OffsetDateTime? {
            return view.selectionAsDate
        }

        @BindingAdapter("monthCalendarSelectionChanged")
        fun setListeners(view: MonthCalendarView, attrChange: InverseBindingListener) {
            view.dataBindingSelectionChangeListener = object : SelectionChangeListener {
                @Override
                override fun onSelectionChanged(
                    monthCalendarView: MonthCalendarView,
                    newSelection: MonthEntry?,
                    oldSelection: MonthEntry?
                ) {
                    attrChange.onChange()
                }
            }
        }
    }
}
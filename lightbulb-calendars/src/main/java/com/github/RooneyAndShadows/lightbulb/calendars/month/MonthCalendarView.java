package com.github.RooneyAndShadows.lightbulb.calendars.month;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.RooneyAndShadows.commons.date.DateUtils;
import com.github.RooneyAndShadows.lightbulb.commons.utils.ResourceUtils;
import com.rands.lightbulb.calendars.R;

import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.viewpager.widget.ViewPager;

@SuppressWarnings("unused")
public class MonthCalendarView extends LinearLayout {
    private LinearLayout root;
    private TextView headerYearTextView;
    private AppCompatImageButton buttonYearPrev;
    private AppCompatImageButton buttonYearNext;
    private ViewPager pagerView;
    private int currentShownYear;
    private Integer backgroundColor;
    private Integer headerArrowsColor;
    private Integer monthTextColor;
    private Integer monthTextSelectedColor;
    private int backgroundColorSelected;
    private int calendarItemSize;
    private int tileSize;
    private int initialMinYear = 1970;
    private int initialMaxYear = 2100;
    private static final int defaultHeaderHeight = ResourceUtils.dpToPx(40);
    private YearPagerAdapter adapter;
    private InternalSelectionChangeListener dataBindingSelectionChangeListener;
    private final ArrayList<SelectionChangeListener> selectionListeners = new ArrayList<>();

    public MonthCalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        readAttributes(context, attrs);
        initView();
    }

    public int getTileSize() {
        return tileSize;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int specHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        final int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        int desiredTileWidth;
        int desiredTileHeight;


        int measuredHeaderHeight = defaultHeaderHeight;
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int horPadding = getPaddingLeft() + getPaddingRight();
        int verPadding = getPaddingTop() + getPaddingBottom();
        switch (widthSpecMode) {
            case MeasureSpec.EXACTLY: {
                desiredTileWidth = (widthSpecSize - horPadding) / 4;
                break;
            }
            case MeasureSpec.AT_MOST: {
                desiredTileWidth = Math.min(calendarItemSize, (widthSpecSize - horPadding) / 4);
                break;
            }
            case MeasureSpec.UNSPECIFIED:
            default: {
                desiredTileWidth = calendarItemSize;
                break;
            }
        }
        switch (heightSpecMode) {
            case MeasureSpec.EXACTLY: {
                desiredTileHeight = (heightSpecSize - verPadding - measuredHeaderHeight) / 3;
                break;
            }
            case MeasureSpec.AT_MOST: {
                desiredTileHeight = Math.min(calendarItemSize, (heightSpecSize - verPadding - measuredHeaderHeight) / 3);
                break;
            }
            case MeasureSpec.UNSPECIFIED:
            default: {
                desiredTileHeight = calendarItemSize;
                break;
            }
        }
        tileSize = Math.max(desiredTileWidth, desiredTileHeight);
        int measuredWidth = (tileSize * 4);
        int measuredHeight = (tileSize * 3) + measuredHeaderHeight;
        //Put padding back in from when we took it away
        //measuredWidth += getPaddingLeft() + getPaddingRight();
        //measuredHeight += getPaddingTop() + getPaddingBottom();
        //Contract fulfilled, setting out measurements
        setMeasuredDimension(
                //We clamp inline because we want to use un-clamped versions on the children
                clampSize(measuredWidth + horPadding, widthMeasureSpec),
                clampSize(measuredHeight + verPadding, heightMeasureSpec)
        );

        int count = getChildCount();
        View header = getChildAt(0);
        View pager = getChildAt(1);
        int widthSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
        int heightHeaderSpec = MeasureSpec.makeMeasureSpec(measuredHeaderHeight, MeasureSpec.EXACTLY);
        int heightPagerSpec = MeasureSpec.makeMeasureSpec(tileSize * 3, MeasureSpec.EXACTLY);
        header.measure(widthSpec, heightHeaderSpec);
        pager.measure(widthSpec, heightPagerSpec);
    }

    private static int clampSize(int size, int spec) {
        int specMode = MeasureSpec.getMode(spec);
        int specSize = MeasureSpec.getSize(spec);
        switch (specMode) {
            case MeasureSpec.EXACTLY: {
                return specSize;
            }
            case MeasureSpec.AT_MOST: {
                return Math.min(size, specSize);
            }
            case MeasureSpec.UNSPECIFIED:
            default: {
                return size;
            }
        }
    }

    protected void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MonthCalendarView, 0, 0);
        try {
            TextView temp = new TextView(context);
            monthTextSelectedColor = a.getColor(R.styleable.MonthCalendarView_monthCalendarSelectedMonthTextColor, ResourceUtils.getColorByAttribute(getContext(), R.attr.colorOnPrimary));
            monthTextColor = a.getColor(R.styleable.MonthCalendarView_monthCalendarMonthTextColor, temp.getTextColors().getDefaultColor());
            backgroundColorSelected = a.getColor(R.styleable.MonthCalendarView_monthCalendarMonthSelectionBackgroundColor, ResourceUtils.getColorByAttribute(getContext(), R.attr.colorPrimary));
            backgroundColor = a.getColor(R.styleable.MonthCalendarView_monthCalendarBackgroundColor, ResourceUtils.getColorById(getContext(), android.R.color.transparent));
            headerArrowsColor = a.getColor(R.styleable.MonthCalendarView_monthCalendarMonthSelectionBackgroundColor, ResourceUtils.getColorByAttribute(getContext(), android.R.attr.colorAccent));
            initialMinYear = a.getInteger(R.styleable.MonthCalendarView_monthCalendarMinYear, initialMinYear);
            initialMaxYear = a.getInteger(R.styleable.MonthCalendarView_monthCalendarMaxYear, initialMaxYear);
            calendarItemSize = a.getDimensionPixelSize(R.styleable.MonthCalendarView_monthCalendarItemSize, ResourceUtils.getDimenPxById(context, R.dimen.month_calendar_item_size));
        } finally {
            a.recycle();
        }
    }

    private void initView() {
        selectViews();
        customizeViews();
        setupPager(initialMinYear, initialMaxYear);
        if (!isInEditMode()) {
            setupEvents();
            updateHeader();
            handleInitialPosition();
        }
    }

    private void setupPager(int minYear, int maxYear) {
        adapter = new YearPagerAdapter(this, minYear, maxYear);
        pagerView.setAdapter(adapter);
    }

    public void setCalendarBounds(int minYear, int maxYear) {
        adapter.setBounds(minYear, maxYear);
        handleInitialPosition();
    }

    private void selectViews() {
        setOrientation(VERTICAL);
        root = (LinearLayout) inflate(getContext(), R.layout.view_month_calendar, this);
        pagerView = root.findViewById(R.id.pager);
        buttonYearPrev = root.findViewById(R.id.buttonPrevYear);
        buttonYearNext = root.findViewById(R.id.buttonNextYear);
        headerYearTextView = root.findViewById(R.id.header_year_text_view);
    }

    private void customizeViews() {
        if (backgroundColor != null)
            root.setBackgroundColor(backgroundColor);
        if (headerArrowsColor != null) {
            buttonYearPrev.getDrawable().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(headerArrowsColor, BlendModeCompat.SRC_ATOP));
            buttonYearNext.getDrawable().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(headerArrowsColor, BlendModeCompat.SRC_ATOP));
        }
        //pagerView.setLayoutParams(new LinearLayout.LayoutParams(4 * calendarItemSize, 3 * calendarItemSize));
        pagerView.setPageTransformer(false, (page, position) -> {
            position = (float) Math.sqrt(1 - Math.abs(position));
            page.setAlpha(position);
        });
    }

    private void setupEvents() {
        buttonYearPrev.setOnClickListener(view -> goToPreviousPage(true));
        buttonYearNext.setOnClickListener(view -> goToNextPage(true));
        pagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentShownYear = adapter.getYearByPosition(pagerView.getCurrentItem());
                updateHeader();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateHeader() {
        headerYearTextView.setText(String.valueOf(currentShownYear));
    }

    private void handleInitialPosition() {
        int currentYear = DateUtils.extractYearFromDate(DateUtils.now());
        if (currentYear < adapter.getMinYear() || currentYear > adapter.getMaxYear())
            goToYear(adapter.getMaxYear(), false);
        else
            goToYear(currentYear, false);
    }

    public void addSelectioChangeListener(SelectionChangeListener listener) {
        selectionListeners.add(listener);
    }

    public int[] getSelectionAsArray() {
        return adapter.getSelectedMonthAsArray();
    }

    public Date getSelectionAsDate() {
        return adapter.getSelectedMonth();
    }

    /**
     * Method is used to select month.
     *
     * @param year  desired year to select
     * @param month desired month to select
     */
    public void setSelectedMonth(int year, int month) {
        selectMonthInternally(year, month, true);
    }

    /**
     * Method is used to select month and scroll to it's position.
     *
     * @param year         desired year to select
     * @param month        desired month to select
     * @param smoothScroll scroll animation enabled
     */
    public void setSelectedMonthAndScrollToYear(int year, int month, boolean smoothScroll) {
        selectMonthInternally(year, month, true);
        goToYear(year, smoothScroll);
    }

    /**
     * Method is used to select month and scroll to it's position.
     *
     * @param year  desired year to select
     * @param month desired month to select
     */
    public void setSelectedMonthAndScrollToYear(int year, int month) {
        setSelectedMonthAndScrollToYear(year, month, false);
    }

    private void selectMonthInternally(int year, int month, boolean triggerChangeEvents) {
        if (!isMonthEnabled(year, month) || adapter.getSelection().equals(DateUtils.date(year, month)))
            return;
        adapter.select(year, month);
        if (!triggerChangeEvents)
            return;
        if (dataBindingSelectionChangeListener != null)
            dataBindingSelectionChangeListener.onSelectionChanged(adapter.getSelection());
        for (SelectionChangeListener listener : selectionListeners)
            listener.onSelectionChanged(adapter.getSelection().getAsArray());
    }

    public void clearSelection() {
        adapter.clearSelection();
        if (dataBindingSelectionChangeListener != null)
            dataBindingSelectionChangeListener.onSelectionChanged(adapter.getSelection());
        for (SelectionChangeListener listener : selectionListeners)
            listener.onSelectionChanged(adapter.getSelection().getAsArray());
    }

    public void setDisabledMonths(ArrayList<int[]> newDisabledMonths) {
        adapter.setDisabledMonths(newDisabledMonths);
        handleInitialPosition();
    }

    public void setEnabledMonths(ArrayList<int[]> enabledMonths) {
        adapter.setEnabledMonths(enabledMonths);
        handleInitialPosition();
    }

    public boolean isMonthEnabled(int year, int month) {
        return adapter.isMonthEnabled(year, month);
    }

    public void goToPreviousPage(boolean smoothScroll) {
        pagerView.setCurrentItem(pagerView.getCurrentItem() - 1, smoothScroll);
        updateHeader();
    }

    public void goToNextPage(boolean smoothScroll) {
        pagerView.setCurrentItem(pagerView.getCurrentItem() + 1, smoothScroll);
        updateHeader();
    }

    public void goToYear(int year, boolean smoothScroll) {
        if (year > adapter.getMaxYear() || year < adapter.getMinYear())
            return;
        pagerView.setCurrentItem(adapter.getPagePositionByYear(year), smoothScroll);
        currentShownYear = year;
        updateHeader();
    }

    public int getCurrentShownYear() {
        return currentShownYear;
    }

    public int getBackgroundColorSelected() {
        return backgroundColorSelected;
    }

    public int getCalendarItemSize() {
        return calendarItemSize;
    }

    public int getMinYear() {
        return adapter.getMinYear();
    }

    public int getMaxYear() {
        return adapter.getMaxYear();
    }

    public Integer getMonthTextColor() {
        return monthTextColor;
    }

    public Integer getMonthTextSelectedColor() {
        return monthTextSelectedColor;
    }

    @BindingAdapter("monthCalendarSelection")
    public static void updatePickerSelectionBinding(MonthCalendarView view, Date selectedDate) {
        int[] newSelection = null;
        if (selectedDate != null)
            newSelection = new int[]{DateUtils.extractYearFromDate(selectedDate), DateUtils.extractMonthOfYearFromDate(selectedDate)};
        view.handleSelection(newSelection);
    }

    @InverseBindingAdapter(attribute = "monthCalendarSelection", event = "monthCalendarSelectionChanged")
    public static Date getText(MonthCalendarView view) {
        if (view.adapter.getSelection().isEmpty())
            return null;
        return DateUtils.date(view.getSelectionAsArray()[0], view.getSelectionAsArray()[1], 1);
    }

    @BindingAdapter("monthCalendarSelectionChanged")
    public static void setListeners(MonthCalendarView view, final InverseBindingListener attrChange) {
        view.dataBindingSelectionChangeListener = selection1 -> attrChange.onChange();
    }

    private void handleSelection(int[] newSelection) {
        if (newSelection == null) {
            adapter.clearSelection();
            return;
        }
        if (!isMonthEnabled(newSelection[0], newSelection[1]))
            return;
        adapter.select(newSelection[0], newSelection[1]);
        goToYear(newSelection[0], false);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState myState = new SavedState(superState);
        myState.selectedMonth = DateUtils.getDateStringInDefaultFormat(adapter.getSelection().get());
        myState.minYear = adapter.getMinYear();
        myState.maxYear = adapter.getMaxYear();
        myState.shownYear = currentShownYear;
        if (adapter.getEnabledMonths() != null) {
            ArrayList<String> enabledMonths = new ArrayList<>();
            for (int[] enabledMonth : adapter.getEnabledMonths())
                enabledMonths.add(DateUtils.getDateStringInDefaultFormat(DateUtils.date(enabledMonth[0], enabledMonth[1])));
            myState.enabledMonths = enabledMonths;
        }
        if (adapter.getDisabledMonths() != null) {
            ArrayList<String> disabledMonths = new ArrayList<>();
            for (int[] disabledMonth : adapter.getDisabledMonths())
                disabledMonths.add(DateUtils.getDateStringInDefaultFormat(DateUtils.date(disabledMonth[0], disabledMonth[1])));
            myState.disabledMonths = disabledMonths;
        }
        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.disabledMonths == null && savedState.enabledMonths == null)
            adapter.setBounds(savedState.minYear, savedState.maxYear);
        if (savedState.disabledMonths != null) {
            ArrayList<int[]> previouslyDisabledMonths = new ArrayList<>();
            for (String disabledMonth : savedState.disabledMonths) {
                Date monthAsDate = DateUtils.getDateFromStringInDefaultFormat(disabledMonth);
                int year = DateUtils.extractYearFromDate(monthAsDate);
                int month = DateUtils.extractMonthOfYearFromDate(monthAsDate);
                previouslyDisabledMonths.add(new int[]{year, month});
            }
            setDisabledMonths(previouslyDisabledMonths);
        }
        if (savedState.enabledMonths != null) {
            ArrayList<int[]> previouslyEnabledMonths = new ArrayList<>();
            for (String enabledMonth : savedState.enabledMonths) {
                Date monthAsDate = DateUtils.getDateFromStringInDefaultFormat(enabledMonth);
                int year = DateUtils.extractYearFromDate(monthAsDate);
                int month = DateUtils.extractMonthOfYearFromDate(monthAsDate);
                previouslyEnabledMonths.add(new int[]{year, month});
            }
            setEnabledMonths(previouslyEnabledMonths);
        }
        Date previouslySelectedDate = DateUtils.getDateFromStringInDefaultFormat(savedState.selectedMonth);
        if (previouslySelectedDate != null)
            setSelectedMonth(DateUtils.extractYearFromDate(previouslySelectedDate), DateUtils.extractMonthOfYearFromDate(previouslySelectedDate));
        if (savedState.shownYear != null)
            goToYear(savedState.shownYear, false);

    }

    private static class SavedState extends BaseSavedState {
        private ArrayList<String> enabledMonths;
        private ArrayList<String> disabledMonths;
        private Integer minYear;
        private Integer maxYear;
        private String selectedMonth;
        private Integer shownYear;
        private int adapterFilterType;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            enabledMonths = in.createStringArrayList();
            disabledMonths = in.createStringArrayList();
            minYear = in.readInt();
            maxYear = in.readInt();
            adapterFilterType = in.readInt();
            selectedMonth = in.readString();
            shownYear = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeStringList(enabledMonths);
            out.writeStringList(disabledMonths);
            out.writeInt(minYear);
            out.writeInt(maxYear);
            out.writeInt(adapterFilterType);
            out.writeString(selectedMonth);
            out.writeInt(shownYear);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private interface InternalSelectionChangeListener {
        void onSelectionChanged(YearPagerAdapter.Selection selection);
    }

    public interface SelectionChangeListener {
        void onSelectionChanged(int[] selection);
    }
}

package com.github.rooneyandshadows.lightbulb.calendars.month;

import android.content.Context;
import android.util.AttributeSet;


import com.github.rooneyandshadows.commons.date.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.github.rooneyandshadows.lightbulb.calendars.month.MonthsAdapter.*;

class YearPageView extends RecyclerView {
    private MonthCalendarView calendarView;
    private int year;

    public YearPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
    }

    public YearPageView(int year, MonthCalendarView calendarView) {
        super(calendarView.getContext());
        this.year = year;
        this.calendarView = calendarView;
        setItemAnimator(null);
        setAdapter(new MonthsAdapter(calendarView, generateMonthsForYear()));
        setOverScrollMode(OVER_SCROLL_NEVER);
        setLayoutManager(new GridLayoutManager(getContext(), 4, VERTICAL, false));
    }

    public void select(int month) {
        getAdapter().selectMonth(month);
    }

    public void clearSelection() {
        getAdapter().clearSelection();
    }

    @Nullable
    @Override
    public MonthsAdapter getAdapter() {
        return (MonthsAdapter) super.getAdapter();
    }

    private ArrayList<MonthItem> generateMonthsForYear() {
        ArrayList<MonthItem> items = new ArrayList<>();
        for (Date date : DateUtils.getAllMonthsForYear(year)) {
            int currentYear = DateUtils.extractYearFromDate(date);
            int currentMonth = DateUtils.extractMonthOfYearFromDate(date);
            int[] currentMonthAsArray = new int[]{currentYear, currentMonth};
            int[] calendarSelectionAsArray = calendarView.getSelectionAsArray();
            boolean enabled = calendarView.isMonthEnabled(currentMonthAsArray[0], currentMonthAsArray[1]);
            boolean selected = Arrays.equals(calendarSelectionAsArray, currentMonthAsArray);
            items.add(new MonthItem(currentYear, currentMonth, selected, enabled));
        }
        return items;
    }
}

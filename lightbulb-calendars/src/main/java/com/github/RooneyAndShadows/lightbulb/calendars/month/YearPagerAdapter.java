package com.github.rooneyandshadows.lightbulb.calendars.month;

import android.view.View;
import android.view.ViewGroup;


import com.github.rooneyandshadows.java.commons.date.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class YearPagerAdapter extends PagerAdapter {
    private int minYear;
    private int maxYear;
    private final ArrayList<Integer> years = new ArrayList<>();
    private final Selection selection = new Selection();
    private final MonthCalendarView calendarView;
    private ArrayList<int[]> disabledMonths;
    private ArrayList<int[]> enabledMonths;
    private final HashMap<Integer, YearPageView> gridViews = new HashMap<>();

    public YearPagerAdapter(MonthCalendarView calendarView, int minYear, int maxYear) {
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.calendarView = calendarView;
        setBounds(minYear, maxYear);
    }

    public int getMinYear() {
        return minYear;
    }

    public int getMaxYear() {
        return maxYear;
    }

    public int[] getSelectedMonthAsArray() {
        return selection.getAsArray();
    }

    public Date getSelectedMonth() {
        return selection.get();
    }

    public Selection getSelection() {
        return selection;
    }

    public ArrayList<int[]> getDisabledMonths() {
        return disabledMonths;
    }

    public ArrayList<int[]> getEnabledMonths() {
        return enabledMonths;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((ViewGroup) view);
        ((ViewGroup) view).removeAllViews();
        gridViews.remove(position);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        Integer year = years.get(position);
        YearPageView grid = new YearPageView(year, calendarView);
        gridViews.put(position, grid);
        collection.addView(grid);
        return grid;
    }

    @Override
    public int getCount() {
        return years.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public void setBounds(int min, int max) {
        if (min > max) {
            this.maxYear = min;
            this.minYear = max;
        } else {
            this.minYear = min;
            this.maxYear = max;
        }
        initYearModels();
    }

    private void initYearModels() {
        years.clear();
        for (int year = minYear; year <= maxYear; year++)
            years.add(year);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        if (!selection.isEmpty()) {
            YearPageView yearView = getPageByYear(selection.getYear());
            if (yearView != null)
                yearView.clearSelection();
            selection.clear();
        }
    }

    public boolean isMonthEnabled(int year, int month) {
        int[] target = new int[]{year, month};
        if (enabledMonths != null) {
            for (int[] enabledMonth : enabledMonths) {
                if (Arrays.equals(target, enabledMonth))
                    return true;
            }
            return false;
        }
        if (disabledMonths != null) {
            for (int[] disabledMonth : disabledMonths) {
                if (Arrays.equals(target, disabledMonth))
                    return false;
            }
        }
        return true;
    }

    public void select(int year, int month) {
        if (year < minYear)
            year = minYear;
        if (year > maxYear)
            year = maxYear;
        if (month < 1)
            month = 1;
        if (month > 12)
            month = 12;
        int[] newSelection = new int[]{year, month};
        if (!isMonthEnabled(year, month))
            return;
        clearSelection();
        selection.set(newSelection);
        YearPageView page = getPageByYear(selection.getYear());
        if (page != null)
            page.select(selection.getMonth());
    }

    public void setEnabledMonths(ArrayList<int[]> enabled) {
        enabledMonths = enabled;
        if (enabledMonths != null) {
            minYear = DateUtils.extractYearFromDate(DateUtils.now());
            maxYear = minYear;
            if (enabledMonths.size() > 0) {
                minYear = enabled.get(0)[0];
                maxYear = enabled.get(0)[0];
            }
            boolean clearCurrentSelection = true;
            for (int[] month : enabledMonths) {
                int currentYear = month[0];
                if (Arrays.equals(month, selection.getAsArray()))
                    clearCurrentSelection = false;
                if (currentYear < minYear)
                    minYear = currentYear;
                if (currentYear > maxYear)
                    maxYear = currentYear;
            }
            if (clearCurrentSelection)
                clearSelection();
        }
        initYearModels();
    }

    public void setDisabledMonths(ArrayList<int[]> disabled) {
        disabledMonths = disabled;
        if (disabledMonths != null)
            for (int[] disabledMonth : disabledMonths)
                if (Arrays.equals(disabledMonth, selection.getAsArray()))
                    clearSelection();
        initYearModels();
    }

    private YearPageView getPageByYear(int year) {
        int yearIndex = getPagePositionByYear(year);
        if (yearIndex == -1)
            return null;
        YearPageView targetPage = gridViews.get(yearIndex);
        if (targetPage != null) // if page is visible on screen
            return targetPage;
        else return null;
    }


    public int getYearByPosition(int position) {
        return years.get(position);
    }

    public int getPagePositionByYear(int targetYear) {
        int index = -1;
        for (int i = 0; i < years.size(); i++) {
            Integer currentYear = years.get(i);
            if (currentYear == targetYear)
                index = i;
        }
        return index;
    }

    static class Selection {
        Date selection;

        public Integer getYear() {
            if (selection == null)
                return null;
            return DateUtils.extractYearFromDate(selection);
        }

        public Integer getMonth() {
            if (selection == null)
                return null;
            return DateUtils.extractMonthOfYearFromDate(selection);
        }

        public boolean equals(Date date) {
            return DateUtils.isDateEqual(selection, date);
        }

        public boolean isEmpty() {
            return this.selection == null;
        }

        public Date get() {
            return selection;
        }

        public int[] getAsArray() {
            if (selection == null)
                return null;
            return new int[]{DateUtils.extractYearFromDate(selection), DateUtils.extractMonthOfYearFromDate(selection)};
        }

        public void set(Date date) {
            this.selection = date;
        }

        public void set(int[] month) {
            this.selection = DateUtils.date(month[0], month[1]);
        }

        public void set(int year, int month) {
            this.selection = DateUtils.date(year, month);
        }

        public void clear() {
            selection = null;
        }
    }

}
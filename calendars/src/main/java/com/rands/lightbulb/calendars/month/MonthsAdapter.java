package com.rands.lightbulb.calendars.month;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rands.lightbulb.calendars.R;
import com.rands.lightbulb.commons.utils.ResourceUtils;
import com.rands.java.commons.date.DateUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.recyclerview.widget.RecyclerView;

public class MonthsAdapter extends RecyclerView.Adapter<MonthsAdapter.MonthVH> {
    private ArrayList<MonthItem> items;
    private MonthCalendarView calendarView;

    public MonthsAdapter(MonthCalendarView calendarView, ArrayList<MonthItem> items) {
        this.items = items;
        this.calendarView = calendarView;
    }

    @NonNull
    @Override
    public MonthVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_month_item, parent, false);
        return new MonthVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthVH holder, int position) {
        MonthItem item = items.get(position);
        holder.bindItem(item, calendarView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int findMonthPosition(int month) {
        int position = -1;
        for (int pos = 0; pos < items.size(); pos++) {
            MonthItem item = items.get(pos);
            if (item.getCurrentMonth() == month) {
                position = pos;
                break;
            }
        }
        return position;
    }

    public void selectMonth(int month) {
        int positionOfMonth = findMonthPosition(month);
        if (positionOfMonth == -1)
            return;
        clearSelection();
        items.get(positionOfMonth).setSelected(true);
        notifyItemChanged(positionOfMonth);
    }

    public void clearSelection() {
        for (int i = 0; i < items.size(); i++) {
            MonthItem item = items.get(i);
            if (!item.isSelected())
                continue;
            item.setSelected(false);
            notifyItemChanged(i);
        }
    }

    public static class MonthVH extends RecyclerView.ViewHolder {
        protected TextView monthView;

        MonthVH(TextView view) {
            super(view);
            this.monthView = (TextView) view;


        }

        public void bindItem(MonthItem item, MonthCalendarView calendarView) {
            Context context = monthView.getContext();
            monthView.setLayoutParams(new ViewGroup.LayoutParams(calendarView.getTileSize(), calendarView.getTileSize()));
            monthView.setOnClickListener(v -> calendarView.setSelectedMonth(item.currentYear, item.currentMonth));
            monthView.setEnabled(item.enabled);
            monthView.setText(item.getMonthName());
            if (item.isSelected()) {
                Drawable background = ResourceUtils.getDrawable(context, R.drawable.calendar_selected_item_background);
                background.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(calendarView.getBackgroundColorSelected(), BlendModeCompat.SRC_ATOP));
                monthView.setBackground(background);
            } else {
                monthView.setBackground(null);
            }
        }
    }

    public static class MonthItem {
        private final int currentYear;
        private final int currentMonth;
        private boolean selected;
        private final boolean enabled;
        private final String monthName;

        public MonthItem(int currentYear, int currentMonth, boolean selected, boolean enabled) {
            this.currentYear = currentYear;
            this.currentMonth = currentMonth;
            this.selected = selected;
            this.enabled = enabled;
            this.monthName = DateUtils.getDateString("MMM", DateUtils.date(currentYear, currentMonth));
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public int getCurrentYear() {
            return currentYear;
        }

        public int getCurrentMonth() {
            return currentMonth;
        }

        public boolean isSelected() {
            return selected;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getMonthName() {
            return monthName;
        }
    }
}
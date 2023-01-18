package com.github.rooneyandshadows.lightbulb.calendars.month.adapter

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView.*
import com.github.rooneyandshadows.lightbulb.calendars.month.MonthCalendarView
import com.github.rooneyandshadows.lightbulb.calendars.month.adapter.MonthsAdapter.MonthVH
import com.github.rooneyandshadows.lightbulb.calendars.R
import com.github.rooneyandshadows.lightbulb.commons.utils.ParcelUtils
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils

internal class MonthsAdapter(
    private val calendarView: MonthCalendarView,
    private val items: MutableList<MonthItem>,
) : Adapter<MonthVH>() {

    @Override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.calendar_month_item, parent, false) as TextView
        return MonthVH(v)
    }

    @Override
    override fun onBindViewHolder(holder: MonthVH, position: Int) {
        val item = items[position]
        holder.bindItem(item, calendarView)
    }

    @Override
    override fun getItemCount(): Int {
        return items.size
    }

    private fun findMonthPosition(month: Int): Int {
        var position = -1
        for (pos in items.indices) {
            val item = items[pos]
            if (item.monthEntry.month == month) {
                position = pos
                break
            }
        }
        return position
    }

    fun selectMonth(month: Int) {
        val positionOfMonth = findMonthPosition(month)
        if (positionOfMonth == -1) return
        clearSelection()
        items[positionOfMonth].isSelected = true
        notifyItemChanged(positionOfMonth)
    }

    fun clearSelection() {
        for (i in items.indices) {
            val item = items[i]
            if (!item.isSelected) continue
            item.isSelected = false
            notifyItemChanged(i)
        }
    }

    class MonthVH internal constructor(private val monthView: TextView) : ViewHolder(monthView) {

        fun bindItem(item: MonthItem, calendarView: MonthCalendarView) {
            monthView.apply {
                layoutParams = ViewGroup.LayoutParams(calendarView.tileSize, calendarView.tileSize)
                setOnClickListener { calendarView.setSelectedMonth(item.monthEntry) }
                isEnabled = item.isEnabled
                text = item.monthEntry.monthName
                background = if (item.isSelected) {
                    ResourceUtils.getDrawable(context, R.drawable.calendar_selected_item_background)!!.apply {
                        colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            calendarView.backgroundColorSelected,
                            BlendModeCompat.SRC_ATOP
                        )
                    }
                } else null
            }
        }
    }

    class MonthItem(
        val monthEntry: MonthEntry,
        var isSelected: Boolean,
        val isEnabled: Boolean,
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            ParcelUtils.readParcelable(parcel, MonthEntry::class.java)!!,
            ParcelUtils.readBoolean(parcel)!!,
            ParcelUtils.readBoolean(parcel)!!
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            ParcelUtils.apply {
                writeParcelable(parcel, monthEntry)
                writeBoolean(parcel, isSelected)
                writeBoolean(parcel, isEnabled)
            }
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<MonthItem> {
            override fun createFromParcel(parcel: Parcel): MonthItem {
                return MonthItem(parcel)
            }

            override fun newArray(size: Int): Array<MonthItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
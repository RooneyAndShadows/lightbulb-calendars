package com.github.RooneyAndShadows.lightbulb.calendars.month.adapter

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView.*
import com.github.RooneyAndShadows.lightbulb.calendars.month.MonthCalendarView
import com.github.RooneyAndShadows.lightbulb.calendars.month.adapter.MonthsAdapter.MonthVH
import com.github.rooneyandshadows.java.commons.date.DateUtils
import com.github.rooneyandshadows.lightbulb.calendars.R
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils

internal class MonthsAdapter(
    private val calendarView: MonthCalendarView,
    private val items: MutableList<MonthItem>
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
            if (item.currentMonth == month) {
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

    class MonthVH internal constructor(view: TextView) : ViewHolder(view) {
        private var monthView: TextView

        init {
            monthView = view
        }

        fun bindItem(item: MonthItem, calendarView: MonthCalendarView) {
            val context = monthView.context
            monthView.layoutParams = ViewGroup.LayoutParams(calendarView.tileSize, calendarView.tileSize)
            monthView.setOnClickListener { calendarView.setSelectedMonth(item.currentYear, item.currentMonth) }
            monthView.isEnabled = item.isEnabled
            monthView.text = item.monthName
            if (item.isSelected) {
                val background: Drawable = ResourceUtils.getDrawable(context, R.drawable.calendar_selected_item_background)!!
                background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    calendarView.backgroundColorSelected,
                    BlendModeCompat.SRC_ATOP
                )
                monthView.background = background
            } else {
                monthView.background = null
            }
        }
    }

    class MonthItem(
        val currentYear: Int,
        val currentMonth: Int,
        var isSelected: Boolean,
        val isEnabled: Boolean
    ) : Parcelable {
        val monthName: String = DateUtils.getDateString(
            "MMM", DateUtils.date(
                currentYear, currentMonth
            )
        )

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(currentYear)
            parcel.writeInt(currentMonth)
            parcel.writeByte(if (isSelected) 1 else 0)
            parcel.writeByte(if (isEnabled) 1 else 0)
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
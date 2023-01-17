package com.github.rooneyandshadowss.lightbulb.calendars.month.adapter

import android.os.Parcel
import android.os.Parcelable
import com.github.rooneyandshadows.java.commons.date.DateUtilsOffsetDate
import java.time.OffsetDateTime

class MonthEntry(year: Int, month: Int) : Parcelable {
    var year: Int = year
        private set
    var month: Int = month
        private set

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

    fun compare(target: MonthEntry?): Boolean {
        if (target == null) return false
        return year == target.year && month == target.month
    }

    fun toArray(): IntArray {
        return intArrayOf(year, month)
    }

    fun toDate(): OffsetDateTime {
        return DateUtilsOffsetDate.date(year, month)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(year)
        parcel.writeInt(month)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MonthEntry> {
        override fun createFromParcel(parcel: Parcel): MonthEntry {
            return MonthEntry(parcel)
        }

        override fun newArray(size: Int): Array<MonthEntry?> {
            return arrayOfNulls(size)
        }

        @JvmStatic
        fun fromDateString(dateString: String): MonthEntry {
            val date = DateUtilsOffsetDate.getDateFromString(DateUtilsOffsetDate.defaultFormatWithTimeZone, dateString)
            val year = DateUtilsOffsetDate.extractYearFromDate(date)
            val month = DateUtilsOffsetDate.extractMonthOfYearFromDate(date)
            return MonthEntry(year, month)
        }
    }
}
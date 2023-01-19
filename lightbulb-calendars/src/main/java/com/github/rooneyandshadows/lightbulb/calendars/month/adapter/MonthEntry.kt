package com.github.rooneyandshadows.lightbulb.calendars.month.adapter

import android.os.Parcel
import android.os.Parcelable
import com.github.rooneyandshadows.java.commons.date.DateUtils
import com.github.rooneyandshadows.java.commons.date.DateUtilsOffsetDate
import java.time.OffsetDateTime
import kotlin.math.max
import kotlin.math.min

class MonthEntry(val year: Int, val month: Int) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

    val monthName: String
        get() = DateUtilsOffsetDate.getDateString("MMM", toDate())

    fun compare(year: Int, month: Int): Boolean {
        return year == this.year && month == this.month
    }

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

    fun getWithinYearBounds(minYear: Int, maxYear: Int): MonthEntry {
        return CREATOR.getWithinYearBounds(year, month, minYear, maxYear)
    }

    fun getMonthString(format: String = "MMMM YYYY"): String {
        return DateUtilsOffsetDate.getDateString(format, toDate())
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
        fun fromDate(date: OffsetDateTime): MonthEntry {
            val year = DateUtilsOffsetDate.extractYearFromDate(date)
            val month = DateUtilsOffsetDate.extractMonthOfYearFromDate(date)
            return MonthEntry(year, month)
        }

        @JvmStatic
        fun getWithinYearBounds(targetYear: Int, targetMonth: Int, minYear: Int, maxYear: Int): MonthEntry {
            val year = min(maxYear, max(minYear, targetYear))
            val month = min(12, max(1, targetMonth))
            return MonthEntry(year, month)
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
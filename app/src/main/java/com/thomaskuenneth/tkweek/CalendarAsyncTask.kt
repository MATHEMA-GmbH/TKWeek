/*
 * CalendarAsyncTask.kt
 *
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import com.thomaskuenneth.tkweek.util.DateUtilities
import com.thomaskuenneth.tkweek.preference.PickBusinessDaysPreference
import com.thomaskuenneth.tkweek.databinding.DaysBetweenDatesActivityBinding
import java.util.*

private const val YEARS = "years"
private const val MONTHS = "months"
private const val WEEKENDS = "weekends"
private const val WEEKS = "weeks"
private const val BUSINESS_DAYS = "business_days"
private const val DAYS = "days"

class CalendarAsyncTask(
    private val context: Context,
    private val binding: DaysBetweenDatesActivityBinding
) : AsyncTask<Calendar, Void, Bundle>() {

    override fun doInBackground(vararg params: Calendar): Bundle {
        val c1 = params[0]
        DateUtilities.clearTimeRelatedFields(c1)
        val c2 = params[1]
        DateUtilities.clearTimeRelatedFields(c2)
        var days = 0
        var businessDays = 0
        var sat = false
        var weekends = 0
        var weeks = 0
        var lastWeek = c1[Calendar.WEEK_OF_YEAR]
        var months = 0
        var lastMonth = c1[Calendar.MONTH]
        var years = 0
        var lastYear = c1[Calendar.YEAR]
        var `val`: Int
        val prefs: SharedPreferences = context.getSharedPreferences(
            PickBusinessDaysPreference.getTag(), Context.MODE_PRIVATE
        )
        if (binding.checkboxIncludeFirstDate.isChecked) {
            days += 1
            val weekday = c1[Calendar.DAY_OF_WEEK]
            if (prefs.getBoolean(
                    weekday.toString(),
                    PickBusinessDaysPreference.getDefault(weekday)
                )
            ) {
                businessDays += 1
            }
        }
        while (c2.after(c1)) {
            val weekday = c1[Calendar.DAY_OF_WEEK]
            if (prefs.getBoolean(
                    weekday.toString(),
                    PickBusinessDaysPreference.getDefault(weekday)
                )
            ) {
                businessDays += 1
            }
            if (weekday == Calendar.SATURDAY) {
                sat = true
            } else if (weekday == Calendar.SUNDAY) {
                if (sat) {
                    weekends += 1
                }
                sat = false
            }
            days += 1
            if (c1[Calendar.WEEK_OF_YEAR].also { `val` = it } != lastWeek) {
                weeks += 1
                lastWeek = `val`
            }
            if (c1[Calendar.MONTH].also { `val` = it } != lastMonth) {
                months += 1
                lastMonth = `val`
            }
            if (c1[Calendar.YEAR].also { `val` = it } != lastYear) {
                years += 1
                lastYear = `val`
            }
            c1.add(Calendar.DAY_OF_YEAR, 1)
        }
        val b = Bundle()
        b.putInt(DAYS, days)
        b.putInt(BUSINESS_DAYS, businessDays)
        b.putInt(WEEKENDS, weekends)
        b.putInt(WEEKS, weeks)
        b.putInt(MONTHS, months)
        b.putInt(YEARS, years)
        return b
    }

    override fun onPostExecute(b: Bundle) {
        binding.firstDatePick.isEnabled = true
        binding.firstDateToday.isEnabled = true
        binding.secondDatePick.isEnabled = true
        binding.secondDateToday.isEnabled = true
        binding.daysBetweenDatesProgressbar.visibility = View.INVISIBLE
        binding.daysBetweenDatesTotal.visibility = View.VISIBLE
        binding.daysBetweenDatesTotal.text = context.getString(
            R.string.days_total,
            b.getInt(DAYS), b.getInt(BUSINESS_DAYS)
        )
        binding.daysBetweenDatesWeekends.text = context.getString(
            R.string.days_between_dates_weekends, b.getInt(WEEKENDS)
        )
        binding.daysBetweenDatesWeeks.text = context.getString(
            R.string.days_between_dates_weeks, b.getInt(WEEKS)
        )
        binding.daysBetweenDatesMonths.text = context.getString(
            R.string.days_between_dates_months, b.getInt(MONTHS)
        )
        binding.daysBetweenDatesYears.text = context.getString(
            R.string.days_between_dates_yearss, b.getInt(YEARS)
        )
    }
}
/*
 * WeekFragment.kt
 *
 * TKWeek (c) Thomas Künneth 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.fragment

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.thomaskuenneth.tkweek.R
import com.thomaskuenneth.tkweek.activity.TKWeekActivity
import com.thomaskuenneth.tkweek.appwidget.WeekInfoMediumWidget
import com.thomaskuenneth.tkweek.appwidget.WeekInfoWidget
import com.thomaskuenneth.tkweek.databinding.WeekActivityBinding
import com.thomaskuenneth.tkweek.util.TKWeekUtils
import java.util.*

class WeekFragment : TKWeekBaseFragment<WeekActivityBinding>(),
    OnDateChangedListener, OnSeekBarChangeListener, View.OnClickListener {

    private val binding get() = backing!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        backing = WeekActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        TKWeekActivity.configureDatePicker(binding.dateWithinWeek)
        binding.weekSelection.setOnSeekBarChangeListener(this)
        binding.down.setOnClickListener(this)
        binding.up.setOnClickListener(this)
        prepareCalendar(cal, context, binding.labelWeekNumber, false)
        updatViewsFromCalendar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_today, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.today -> {
                cal.time = Date()
                updatViewsFromCalendar()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun preferencesFinished(resultCode: Int, data: Intent?) {
        super.preferencesFinished(resultCode, data)
        prepareCalendar(cal, context, binding.labelWeekNumber, false)
        updatViewsFromCalendar()
        updateWeekInfoWidgets(requireContext())
    }

    override fun onDateChanged(
        view: DatePicker?, year: Int, monthOfYear: Int,
        dayOfMonth: Int
    ) {
        cal[Calendar.YEAR] = year
        cal[Calendar.MONTH] = monthOfYear
        cal[Calendar.DAY_OF_MONTH] = dayOfMonth
        updateViews()
    }

    override fun onProgressChanged(
        seekBar: SeekBar?, progress: Int,
        fromUser: Boolean
    ) {
        if (fromUser) {
            val dif = progress - (cal[Calendar.WEEK_OF_YEAR] - 1)
            if (dif != 0) {
                cal.add(Calendar.DAY_OF_MONTH, 7 * dif)
                updatViewsFromCalendar()
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        // keine Aktion nötig
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        // keine Aktion nötig
    }

    override fun onClick(v: View) {
        var current = cal[Calendar.DAY_OF_MONTH]
        if (v === binding.down) {
            current -= 7
        } else if (v === binding.up) {
            current += 7
        }
        cal[Calendar.DAY_OF_MONTH] = current
        updatViewsFromCalendar()
    }

    private fun updateWeekInfoWidgets(context: Context) {
        val m = AppWidgetManager.getInstance(context)
        if (m != null) {
            // 4x1-Version
            var appWidgetIds = m.getAppWidgetIds(
                ComponentName(
                    context,
                    WeekInfoWidget::class.java
                )
            )
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                WeekInfoWidget.updateWidgets(context, m, appWidgetIds)
            }
            // 3x1-Version
            appWidgetIds = m.getAppWidgetIds(
                ComponentName(
                    context,
                    WeekInfoMediumWidget::class.java
                )
            )
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                WeekInfoMediumWidget.updateWidgets(context, m, appWidgetIds)
            }
        }
    }

    private fun updatViewsFromCalendar() {
        binding.dateWithinWeek.init(
            cal[Calendar.YEAR], cal[Calendar.MONTH],
            cal[Calendar.DAY_OF_MONTH], this
        )
        updateViews()
    }

    private fun updateViews() {
        // Wochentag ausgeben
        binding.day.text = TKWeekActivity.FORMAT_DAY_OF_WEEK.format(cal.time)
        // Nummer der aktuellen Woche ausgeben
        val weekOfYear = cal[Calendar.WEEK_OF_YEAR]
        binding.weekNumber.text =
            TKWeekUtils.integerToString(weekOfYear)
        val temp = cal.clone() as Calendar
        // die SeekBar anpassen (Maximum Anzahl Wochen im Jahr -1)
        binding.weekSelection.max = temp.getActualMaximum(Calendar.WEEK_OF_YEAR) - 1
        binding.weekSelection.progress = weekOfYear - 1
        // Bis zum Wochenanfangs zurück gehen
        while (temp[Calendar.DAY_OF_WEEK] != temp.firstDayOfWeek) {
            temp.add(Calendar.DAY_OF_MONTH, -1)
        }
        val start = temp.time
        // Datum des letzten Tags der Woche ermitteln
        temp.add(Calendar.DAY_OF_MONTH, 6)
        val end = temp.time
        // Label erster und letzter Tag der Woche
        binding.labelFirstAndLastDayOfWeek.text = getString(
            R.string.first_and_last_day_of_week,
            TKWeekActivity.FORMAT_DAY_OF_WEEK.format(start),
            TKWeekActivity.FORMAT_DAY_OF_WEEK.format(end)
        )
        // ersten und letzten Tag der Woche ausgeben
        val text = getString(
            R.string.string1_dash_string2,
            TKWeekActivity.FORMAT_DEFAULT.format(start),
            TKWeekActivity.FORMAT_DEFAULT.format(end)
        )
        binding.firstAndLastDayOfWeek.text = text
    }

    companion object {

        private const val WOCHENANFANG = "wochenanfang"
        private const val USE_ISO_WEEKS = "use_iso_weeks"

        private val cal = Calendar.getInstance()

        @JvmStatic
        fun prepareCalendar(cal: Calendar, context: Context?) {
            prepareCalendar(cal, context, null, false)
        }

        @JvmStatic
        fun prepareCalendar(
            cal: Calendar, context: Context?,
            label_week_number: TextView?, appendColon: Boolean
        ) {
            val prefs = PreferenceManager
                .getDefaultSharedPreferences(context)
            val useISO = prefs.getBoolean(USE_ISO_WEEKS, false)
            if (useISO) {
                cal.minimalDaysInFirstWeek = 4
                cal.firstDayOfWeek = Calendar.MONDAY
                label_week_number?.setText(R.string.week_number_iso)
            } else {
                val c = Calendar.getInstance()
                val s = prefs.getString(WOCHENANFANG, "-1")
                var start = -1
                try {
                    start = s!!.toInt()
                } catch (e: NumberFormatException) {
                    // kein Logging nötig
                }
                if (start != -1) {
                    c.firstDayOfWeek = start
                }
                cal.minimalDaysInFirstWeek = c.minimalDaysInFirstWeek
                cal.firstDayOfWeek = c.firstDayOfWeek
                label_week_number?.setText(R.string.week_number)
            }
            cal[Calendar.DAY_OF_MONTH] = cal[Calendar.DAY_OF_MONTH]
            if (label_week_number != null && appendColon) {
                label_week_number.append(":")
            }
        }
    }
}
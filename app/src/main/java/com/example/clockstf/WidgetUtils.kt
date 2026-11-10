package com.example.clockstf

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object WidgetUtils {
    // Shared Preferences keys
    const val PREFS_NAME = "ClockStfPrefs"
    const val KEY_UPPERCASE = "uppercase"
    const val KEY_TEXT_SHADOW = "text_shadow"
    const val KEY_BACKGROUND = "background"
    const val KEY_SHOW_DATE = "show_date"
    const val KEY_DATE_ABOVE = "date_above"
    const val KEY_SHORT_WEEKDAY = "short_weekday"
    const val KEY_FULL_WEEKDAY = "full_weekday"
    const val KEY_SHORT_MONTH = "short_month"
    const val KEY_FULL_MONTH = "full_month"
    const val KEY_CLOCK_COLOR = "clock_color"
    const val KEY_DATE_COLOR = "date_color"
    const val KEY_FONT_SIZE = "font_size"
    const val KEY_USE_DEFAULT_CLOCK_COLOR = "use_default_clock_color"
    const val KEY_USE_DEFAULT_DATE_COLOR = "use_default_date_color"

    // Default values
    const val DEFAULT_UPPERCASE = false
    const val DEFAULT_TEXT_SHADOW = false
    const val DEFAULT_BACKGROUND = false
    const val DEFAULT_SHOW_DATE = true
    const val DEFAULT_DATE_ABOVE = false
    const val DEFAULT_SHORT_WEEKDAY = true
    const val DEFAULT_FULL_WEEKDAY = false
    const val DEFAULT_SHORT_MONTH = true
    const val DEFAULT_FULL_MONTH = false
    const val DEFAULT_CLOCK_COLOR = Color.WHITE
    const val DEFAULT_DATE_COLOR = Color.WHITE
    const val DEFAULT_FONT_SIZE = 70
    const val DEFAULT_USE_DEFAULT_CLOCK_COLOR = true
    const val DEFAULT_USE_DEFAULT_DATE_COLOR = true

    // Clock package
    const val CLOCK_PACKAGE = "com.android.deskclock"

    // Get SharedPreferences
    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Format time as HH:mm
    fun formatTime(context: Context, calendar: Calendar = Calendar.getInstance()): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(calendar.time)
    }

    // Format date based on preferences
    fun formatDate(context: Context, calendar: Calendar = Calendar.getInstance()): String {
        val prefs = getPrefs(context)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-6
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) // 0-11

        val weekdaysShort = context.resources.getStringArray(R.array.weekdays_short)
        val weekdaysFull = context.resources.getStringArray(R.array.weekdays_full)
        val monthsShort = context.resources.getStringArray(R.array.months_short)
        val monthsFull = context.resources.getStringArray(R.array.months_full)

        val useShortWeekday = prefs.getBoolean(KEY_SHORT_WEEKDAY, DEFAULT_SHORT_WEEKDAY)
        val useFullWeekday = prefs.getBoolean(KEY_FULL_WEEKDAY, DEFAULT_FULL_WEEKDAY)
        val useShortMonth = prefs.getBoolean(KEY_SHORT_MONTH, DEFAULT_SHORT_MONTH)
        val useFullMonth = prefs.getBoolean(KEY_FULL_MONTH, DEFAULT_FULL_MONTH)

        val weekday = if (useFullWeekday) weekdaysFull[dayOfWeek] else weekdaysShort[dayOfWeek]
        val monthStr = if (useFullMonth) monthsFull[month] else monthsShort[month]

        val dateStr = "$weekday $dayOfMonth $monthStr"

        val uppercase = prefs.getBoolean(KEY_UPPERCASE, DEFAULT_UPPERCASE)
        return if (uppercase) dateStr.uppercase(Locale.getDefault()) else dateStr
    }

    // Get clock color
    fun getClockColor(context: Context): Int {
        val prefs = getPrefs(context)
        val useDefault = prefs.getBoolean(KEY_USE_DEFAULT_CLOCK_COLOR, DEFAULT_USE_DEFAULT_CLOCK_COLOR)
        return if (useDefault) DEFAULT_CLOCK_COLOR else prefs.getInt(KEY_CLOCK_COLOR, DEFAULT_CLOCK_COLOR)
    }

    // Get date color
    fun getDateColor(context: Context): Int {
        val prefs = getPrefs(context)
        val useDefault = prefs.getBoolean(KEY_USE_DEFAULT_DATE_COLOR, DEFAULT_USE_DEFAULT_DATE_COLOR)
        return if (useDefault) DEFAULT_DATE_COLOR else prefs.getInt(KEY_DATE_COLOR, DEFAULT_DATE_COLOR)
    }

    // Get font size
    fun getFontSize(context: Context): Int {
        val prefs = getPrefs(context)
        return prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE)
    }

    // Save color with alpha
    fun saveColorWithAlpha(prefs: SharedPreferences, key: String, color: Int) {
        prefs.edit().putInt(key, color).apply()
    }

    // Parse color from hex string (e.g., "CBFF70E3")
    fun parseColorFromHex(hex: String): Int {
        return try {
            if (hex.length == 8) {
                Color.parseColor("#$hex")
            } else if (hex.length == 6) {
                Color.parseColor("#FF$hex")
            } else {
                Color.WHITE
            }
        } catch (e: Exception) {
            Color.WHITE
        }
    }

    // Convert color to hex string (e.g., "CBFF70E3")
    fun colorToHex(color: Int): String {
        return String.format("%08X", color)
    }

    // Reset all preferences to defaults
    fun resetAllPreferences(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().clear().apply()
    }
}
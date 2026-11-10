package com.example.clockstf

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import java.util.Calendar

class ClockStfWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_QUICKBOOT_POWERON ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ClockStfWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {}

    override fun onDisabled(context: Context) {}

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = WidgetUtils.getPrefs(context)

            val showDate = prefs.getBoolean(WidgetUtils.KEY_SHOW_DATE, WidgetUtils.DEFAULT_SHOW_DATE)
            val dateAbove = prefs.getBoolean(WidgetUtils.KEY_DATE_ABOVE, WidgetUtils.DEFAULT_DATE_ABOVE)
            val background = prefs.getBoolean(WidgetUtils.KEY_BACKGROUND, WidgetUtils.DEFAULT_BACKGROUND)
            val textShadow = prefs.getBoolean(WidgetUtils.KEY_TEXT_SHADOW, WidgetUtils.DEFAULT_TEXT_SHADOW)

            val clockColor = WidgetUtils.getClockColor(context)
            val dateColor = WidgetUtils.getDateColor(context)

            val baseFontSize = WidgetUtils.getFontSize(context)

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            val defaultWidth = 200
            val defaultHeight = 110
            val scaleX = minWidth.toFloat() / defaultWidth
            val scaleY = minHeight.toFloat() / defaultHeight
            val scale = minOf(scaleX, scaleY) * 0.8f

            val fontSize = (baseFontSize * scale).toInt().coerceAtLeast(20).coerceAtMost(200)

            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            if (background) {
                views.setInt(R.id.widget_container, "setBackgroundColor", Color.parseColor("#80000000"))
            } else {
                views.setInt(R.id.widget_container, "setBackgroundColor", Color.TRANSPARENT)
            }

            val calendar = Calendar.getInstance()
            val timeStr = WidgetUtils.formatTime(context, calendar)
            val dateStr = WidgetUtils.formatDate(context, calendar)

            val dateFontSize = (fontSize * 0.35).toInt().coerceAtLeast(12)
            val shadowRadius = if (textShadow) 4f else 0f
            val shadowColor = if (textShadow) Color.BLACK else Color.TRANSPARENT

            views.setFloat(R.id.time_text, "setTextSize", fontSize.toFloat())
            views.setFloat(R.id.time_text, "setShadowLayer", shadowRadius, 0f, 0f, shadowColor)
            views.setInt(R.id.time_text, "setTextColor", clockColor)
            views.setTextViewText(R.id.time_text, timeStr)

            views.setFloat(R.id.date_text, "setTextSize", dateFontSize.toFloat())
            views.setFloat(R.id.date_text, "setShadowLayer", shadowRadius, 0f, 0f, shadowColor)
            views.setInt(R.id.date_text, "setTextColor", dateColor)
            views.setTextViewText(R.id.date_text, dateStr)
            views.setViewVisibility(R.id.date_text, if (showDate) View.VISIBLE else View.GONE)

            views.setFloat(R.id.time_above_text, "setTextSize", fontSize.toFloat())
            views.setFloat(R.id.time_above_text, "setShadowLayer", shadowRadius, 0f, 0f, shadowColor)
            views.setInt(R.id.time_above_text, "setTextColor", clockColor)
            views.setTextViewText(R.id.time_above_text, timeStr)

            views.setFloat(R.id.date_above_text, "setTextSize", dateFontSize.toFloat())
            views.setFloat(R.id.date_above_text, "setShadowLayer", shadowRadius, 0f, 0f, shadowColor)
            views.setInt(R.id.date_above_text, "setTextColor", dateColor)
            views.setTextViewText(R.id.date_above_text, dateStr)

            if (dateAbove) {
                views.setViewVisibility(R.id.date_above_container, View.VISIBLE)
                views.setViewVisibility(R.id.time_above_container, View.GONE)
            } else {
                views.setViewVisibility(R.id.date_above_container, View.GONE)
                views.setViewVisibility(R.id.time_above_container, View.VISIBLE)
            }

            val clockIntent = Intent()
            clockIntent.component = ComponentName(
                WidgetUtils.CLOCK_PACKAGE,
                "com.android.deskclock.DeskClock"
            )
            clockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val fallbackIntent = Intent(Intent.ACTION_MAIN)
            fallbackIntent.addCategory(Intent.CATEGORY_APP_CLOCK)
            fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val pendingIntent = try {
                PendingIntent.getActivity(context, 0, clockIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    })
            } catch (e: Exception) {
                PendingIntent.getActivity(context, 0, fallbackIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    })
            }

            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            views.setOnClickPendingIntent(R.id.date_above_container, pendingIntent)
            views.setOnClickPendingIntent(R.id.time_above_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ClockStfWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
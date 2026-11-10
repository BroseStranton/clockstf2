package com.example.clockstf

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.AlarmClock
import android.util.TypedValue
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
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
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

        private fun styleText(views: RemoteViews, viewId: Int, sizeSp: Float, color: Int, text: String) {
            views.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, sizeSp)
            views.setInt(viewId, "setTextColor", color)
            views.setTextViewText(viewId, text)
        }

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
            val dateFontSize = (fontSize * 0.35).toInt().coerceAtLeast(12)

            // L'ombre du texte ne peut pas être réglée par code dans un widget :
            // on choisit entre deux layouts identiques (avec / sans ombre).
            val layoutId = if (textShadow) R.layout.widget_layout_shadow else R.layout.widget_layout
            val views = RemoteViews(context.packageName, layoutId)

            val bgColor = if (background) Color.parseColor("#80000000") else Color.TRANSPARENT
            views.setInt(R.id.widget_container, "setBackgroundColor", bgColor)

            val calendar = Calendar.getInstance()
            val timeStr = WidgetUtils.formatTime(context, calendar)
            val dateStr = WidgetUtils.formatDate(context, calendar)

            // Mode « heure au-dessus de la date »
            styleText(views, R.id.time_text, fontSize.toFloat(), clockColor, timeStr)
            styleText(views, R.id.date_text, dateFontSize.toFloat(), dateColor, dateStr)
            views.setViewVisibility(R.id.date_text, if (showDate) View.VISIBLE else View.GONE)

            // Mode « date au-dessus de l'heure »
            styleText(views, R.id.time_above_text, fontSize.toFloat(), clockColor, timeStr)
            styleText(views, R.id.date_above_text, dateFontSize.toFloat(), dateColor, dateStr)
            views.setViewVisibility(R.id.date_above_text, if (showDate) View.VISIBLE else View.GONE)

            if (dateAbove) {
                views.setViewVisibility(R.id.date_above_container, View.VISIBLE)
                views.setViewVisibility(R.id.time_above_container, View.GONE)
            } else {
                views.setViewVisibility(R.id.date_above_container, View.GONE)
                views.setViewVisibility(R.id.time_above_container, View.VISIBLE)
            }

            // Un clic ouvre l'application Horloge du téléphone (quel que soit le fabricant)
            val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            clockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val pendingIntent = PendingIntent.getActivity(
                context, 0, clockIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

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

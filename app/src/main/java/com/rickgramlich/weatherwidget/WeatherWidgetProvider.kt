package com.rickgramlich.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rickgramlich.weatherwidget.data.WeatherPrefs
import com.rickgramlich.weatherwidget.ui.WidgetLayoutBuilder
import java.util.concurrent.TimeUnit

class WeatherWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val WORK_NAME = "weather_refresh"
        private const val ACTION_REFRESH = "com.rickgramlich.weatherwidget.ACTION_REFRESH"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, WeatherWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)

            val prefs = WeatherPrefs(context)
            val data = prefs.loadWeatherData()
            val views = WidgetLayoutBuilder.build(context, data)

            // Current weather section → refresh
            val refreshIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPending = PendingIntent.getBroadcast(
                context, 0, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.current_section, refreshPending)

            // Forecast section → open weather app
            val appPending = buildAppIntent(context, prefs)
            views.setOnClickPendingIntent(R.id.forecast_section, appPending)

            for (id in ids) {
                manager.updateAppWidget(id, views)
            }
        }

        private fun buildAppIntent(context: Context, prefs: WeatherPrefs): PendingIntent {
            val weatherAppPackage = prefs.weatherAppPackage
            val launchIntent = if (weatherAppPackage != null) {
                context.packageManager.getLaunchIntentForPackage(weatherAppPackage)
            } else {
                null
            }

            val intent = launchIntent ?: Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            return PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun reschedulePeriodicRefresh(context: Context) {
            val prefs = WeatherPrefs(context)
            val intervalMinutes = prefs.refreshIntervalMinutes.toLong()

            val request = PeriodicWorkRequestBuilder<WeatherWidgetWorker>(intervalMinutes, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                request
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH) {
            enqueueOneTimeRefresh(context)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = WeatherPrefs(context)
        val data = prefs.loadWeatherData()
        val views = WidgetLayoutBuilder.build(context, data)

        // Current weather section → refresh
        val refreshIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        val refreshPending = PendingIntent.getBroadcast(
            context, 0, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.current_section, refreshPending)

        // Forecast section → open weather app
        val appPending = buildAppIntent(context, prefs)
        views.setOnClickPendingIntent(R.id.forecast_section, appPending)

        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, views)
        }

        enqueueOneTimeRefresh(context)
    }

    override fun onEnabled(context: Context) {
        reschedulePeriodicRefresh(context)
    }

    override fun onDisabled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun enqueueOneTimeRefresh(context: Context) {
        val request = OneTimeWorkRequestBuilder<WeatherWidgetWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}

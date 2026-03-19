package com.rickgramlich.weatherwidget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rickgramlich.weatherwidget.data.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val data = WeatherRepository.refreshWeather(applicationContext)
            if (data != null) {
                WeatherWidgetProvider.updateAllWidgets(applicationContext)
                Result.success()
            } else {
                Result.retry()
            }
        }
    }
}

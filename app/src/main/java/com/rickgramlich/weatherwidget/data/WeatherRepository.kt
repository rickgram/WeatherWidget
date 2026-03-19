package com.rickgramlich.weatherwidget.data

import android.content.Context
import com.rickgramlich.weatherwidget.location.LocationProvider

object WeatherRepository {

    fun refreshWeather(context: Context): WeatherData? {
        val prefs = WeatherPrefs(context)
        val location = LocationProvider(context).getLocation()

        if (location == null) {
            return prefs.loadWeatherData()
        }

        return try {
            val data = OpenMeteoApi.fetchWeather(location.first, location.second)
            prefs.saveWeatherData(data)
            data
        } catch (e: Exception) {
            prefs.loadWeatherData()
        }
    }
}

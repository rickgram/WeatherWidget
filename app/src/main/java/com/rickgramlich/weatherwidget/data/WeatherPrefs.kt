package com.rickgramlich.weatherwidget.data

import android.content.Context
import android.content.SharedPreferences

class WeatherPrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    var lastLat: Float
        get() = prefs.getFloat("last_lat", 0f)
        set(value) = prefs.edit().putFloat("last_lat", value).apply()

    var lastLon: Float
        get() = prefs.getFloat("last_lon", 0f)
        set(value) = prefs.edit().putFloat("last_lon", value).apply()

    var lastUpdated: Long
        get() = prefs.getLong("last_updated", 0L)
        set(value) = prefs.edit().putLong("last_updated", value).apply()

    var backgroundOpacity: Int
        get() = prefs.getInt("bg_opacity", 80)
        set(value) = prefs.edit().putInt("bg_opacity", value.coerceIn(0, 100)).apply()

    var weatherAppPackage: String?
        get() = prefs.getString("weather_app_package", null)
        set(value) = prefs.edit().putString("weather_app_package", value).apply()

    var weatherAppName: String?
        get() = prefs.getString("weather_app_name", null)
        set(value) = prefs.edit().putString("weather_app_name", value).apply()

    var refreshIntervalMinutes: Int
        get() = prefs.getInt("refresh_interval_minutes", 30)
        set(value) = prefs.edit().putInt("refresh_interval_minutes", value).apply()

    var selectedFont: Int
        get() = prefs.getInt("selected_font", FONT_SYSTEM)
        set(value) = prefs.edit().putInt("selected_font", value).apply()

    var usePixelIcons: Boolean
        get() = prefs.getBoolean("use_pixel_icons", false)
        set(value) = prefs.edit().putBoolean("use_pixel_icons", value).apply()

    companion object {
        const val FONT_SYSTEM = 0
        const val FONT_JETBRAINS_MONO = 1
        const val FONT_INSTRUMENT_SERIF = 2
        const val FONT_PERMANENT_MARKER = 3
        const val FONT_GOOGLE_SANS = 4
        const val FONT_DS_DIGITAL = 5
        const val FONT_OXANIUM = 6
        const val FONT_DOTO = 7
    }

    fun hasLocation(): Boolean = lastLat != 0f || lastLon != 0f

    fun saveWeatherData(data: WeatherData) {
        prefs.edit().apply {
            putFloat("current_temp", data.current.temperature.toFloat())
            putInt("current_code", data.current.weatherCode)
            putBoolean("current_is_night", data.current.isNight)
            putLong("last_updated", data.lastUpdated)

            data.forecast.forEachIndexed { i, day ->
                putString("day_label_$i", day.dayLabel)
                putFloat("day_high_$i", day.high.toFloat())
                putFloat("day_low_$i", day.low.toFloat())
                putInt("day_code_$i", day.weatherCode)
            }

            apply()
        }
    }

    fun loadWeatherData(): WeatherData? {
        if (lastUpdated == 0L) return null

        val current = CurrentWeather(
            temperature = prefs.getFloat("current_temp", 0f).toDouble(),
            weatherCode = prefs.getInt("current_code", 0),
            isNight = prefs.getBoolean("current_is_night", false)
        )

        val forecast = (0..3).map { i ->
            DayForecast(
                dayLabel = prefs.getString("day_label_$i", "--") ?: "--",
                high = prefs.getFloat("day_high_$i", 0f).toDouble(),
                low = prefs.getFloat("day_low_$i", 0f).toDouble(),
                weatherCode = prefs.getInt("day_code_$i", 0)
            )
        }

        return WeatherData(
            current = current,
            forecast = forecast,
            lastUpdated = lastUpdated
        )
    }
}

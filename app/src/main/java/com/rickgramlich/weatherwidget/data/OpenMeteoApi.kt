package com.rickgramlich.weatherwidget.data

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object OpenMeteoApi {

    fun fetchWeather(lat: Double, lon: Double): WeatherData {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,weather_code" +
                "&daily=temperature_2m_max,temperature_2m_min,weather_code,sunrise,sunset" +
                "&timezone=auto&forecast_days=5&temperature_unit=fahrenheit" +
                "&models=best_match"
        )

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            reader.close()
            return parseResponse(response)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseResponse(json: String): WeatherData {
        val root = JSONObject(json)

        val currentObj = root.getJSONObject("current")
        val currentTime = currentObj.getString("time")

        val dailyObj = root.getJSONObject("daily")
        val sunrises = dailyObj.getJSONArray("sunrise")
        val sunsets = dailyObj.getJSONArray("sunset")

        val now = LocalDateTime.parse(currentTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val todaySunrise = LocalDateTime.parse(sunrises.getString(0), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val todaySunset = LocalDateTime.parse(sunsets.getString(0), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val isNight = now.isBefore(todaySunrise) || now.isAfter(todaySunset)

        val current = CurrentWeather(
            temperature = currentObj.getDouble("temperature_2m"),
            weatherCode = currentObj.getInt("weather_code"),
            isNight = isNight
        )

        val dates = dailyObj.getJSONArray("time")
        val highs = dailyObj.getJSONArray("temperature_2m_max")
        val lows = dailyObj.getJSONArray("temperature_2m_min")
        val codes = dailyObj.getJSONArray("weather_code")

        // Start from index 0 (today) through index 3
        val today = LocalDate.parse(dates.getString(0))
        val forecast = (0..3).map { i ->
            val date = LocalDate.parse(dates.getString(i))
            val label = if (date == today) "Today" else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            DayForecast(
                dayLabel = label,
                high = highs.getDouble(i),
                low = lows.getDouble(i),
                weatherCode = codes.getInt(i)
            )
        }

        return WeatherData(
            current = current,
            forecast = forecast,
            lastUpdated = System.currentTimeMillis()
        )
    }
}

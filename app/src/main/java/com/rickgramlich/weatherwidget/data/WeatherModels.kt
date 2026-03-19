package com.rickgramlich.weatherwidget.data

data class CurrentWeather(
    val temperature: Double,
    val weatherCode: Int,
    val isNight: Boolean
)

data class DayForecast(
    val dayLabel: String,
    val high: Double,
    val low: Double,
    val weatherCode: Int
)

data class WeatherData(
    val current: CurrentWeather,
    val forecast: List<DayForecast>,
    val lastUpdated: Long
)

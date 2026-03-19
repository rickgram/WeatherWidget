package com.rickgramlich.weatherwidget.util

import com.rickgramlich.weatherwidget.R

object WeatherCodeMapper {

    fun getIconResource(weatherCode: Int, isNight: Boolean = false): Int {
        return when (weatherCode) {
            0 -> if (isNight) R.drawable.ic_weather_clear_night else R.drawable.ic_weather_clear
            1, 2 -> if (isNight) R.drawable.ic_weather_partly_cloudy_night else R.drawable.ic_weather_partly_cloudy
            3 -> R.drawable.ic_weather_cloudy
            45, 48 -> R.drawable.ic_weather_fog
            51, 53, 55, 56, 57 -> if (isNight) R.drawable.ic_weather_drizzle_night else R.drawable.ic_weather_drizzle
            61, 63, 65, 66, 67, 80, 81, 82 -> if (isNight) R.drawable.ic_weather_rain_night else R.drawable.ic_weather_rain
            71, 73, 75, 77, 85, 86 -> R.drawable.ic_weather_snow
            95, 96, 99 -> R.drawable.ic_weather_thunderstorm
            else -> if (isNight) R.drawable.ic_weather_partly_cloudy_night else R.drawable.ic_weather_partly_cloudy
        }
    }
}

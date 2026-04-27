package com.rickgramlich.weatherwidget.util

import com.rickgramlich.weatherwidget.R

object WeatherCodeMapper {

    fun getIconResource(weatherCode: Int, isNight: Boolean = false, usePixel: Boolean = false): Int {
        if (usePixel) return getPixelIcon(weatherCode, isNight)
        return getDefaultIcon(weatherCode, isNight)
    }

    private fun getDefaultIcon(weatherCode: Int, isNight: Boolean): Int {
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

    private fun getPixelIcon(weatherCode: Int, isNight: Boolean): Int {
        return when (weatherCode) {
            0 -> if (isNight) R.drawable.ic_weather_pixel_clear_night else R.drawable.ic_weather_pixel_clear
            1, 2 -> if (isNight) R.drawable.ic_weather_pixel_partly_cloudy_night else R.drawable.ic_weather_pixel_partly_cloudy
            3 -> R.drawable.ic_weather_pixel_cloudy
            45, 48 -> R.drawable.ic_weather_pixel_fog
            51, 53, 55, 56, 57 -> if (isNight) R.drawable.ic_weather_pixel_drizzle_night else R.drawable.ic_weather_pixel_drizzle
            61, 63, 65, 66, 67, 80, 81, 82 -> if (isNight) R.drawable.ic_weather_pixel_rain_night else R.drawable.ic_weather_pixel_rain
            71, 73, 75, 77, 85, 86 -> R.drawable.ic_weather_pixel_snow
            95, 96, 99 -> R.drawable.ic_weather_pixel_thunderstorm
            else -> if (isNight) R.drawable.ic_weather_pixel_partly_cloudy_night else R.drawable.ic_weather_pixel_partly_cloudy
        }
    }
}

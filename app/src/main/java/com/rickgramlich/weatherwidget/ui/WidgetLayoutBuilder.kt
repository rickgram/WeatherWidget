package com.rickgramlich.weatherwidget.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.widget.RemoteViews
import com.rickgramlich.weatherwidget.R
import com.rickgramlich.weatherwidget.data.WeatherData
import com.rickgramlich.weatherwidget.data.WeatherPrefs
import com.rickgramlich.weatherwidget.util.WeatherCodeMapper
import kotlin.math.roundToInt

object WidgetLayoutBuilder {

    fun build(context: Context, data: WeatherData?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_weather)
        val prefs = WeatherPrefs(context)

        // Set background opacity
        val opacity = prefs.backgroundOpacity
        val alpha = (opacity * 255 / 100)
        val bgColor = (alpha shl 24) or 0x000000
        views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)

        if (data == null) {
            views.setTextViewText(R.id.current_temp, "--")
            return views
        }

        // Current weather
        views.setTextViewText(R.id.current_temp, "${data.current.temperature.roundToInt()}")
        views.setImageViewResource(
            R.id.current_icon,
            WeatherCodeMapper.getIconResource(data.current.weatherCode, data.current.isNight)
        )

        // Forecast days (always daytime icons for forecast)
        val dayIds = listOf(
            Triple(R.id.day1_label, R.id.day1_icon, Pair(R.id.day1_high, R.id.day1_low)),
            Triple(R.id.day2_label, R.id.day2_icon, Pair(R.id.day2_high, R.id.day2_low)),
            Triple(R.id.day3_label, R.id.day3_icon, Pair(R.id.day3_high, R.id.day3_low)),
            Triple(R.id.day4_label, R.id.day4_icon, Pair(R.id.day4_high, R.id.day4_low))
        )

        data.forecast.forEachIndexed { i, day ->
            if (i < dayIds.size) {
                val (labelId, iconId, tempIds) = dayIds[i]
                views.setTextViewText(labelId, day.dayLabel)
                views.setImageViewResource(iconId, WeatherCodeMapper.getIconResource(day.weatherCode))
                views.setTextViewText(tempIds.first, "${day.high.roundToInt()}")
                views.setTextViewText(tempIds.second, "${day.low.roundToInt()}")
            }
        }

        return views
    }
}

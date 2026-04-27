package com.rickgramlich.weatherwidget.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.rickgramlich.weatherwidget.R
import com.rickgramlich.weatherwidget.data.WeatherData
import com.rickgramlich.weatherwidget.data.WeatherPrefs
import com.rickgramlich.weatherwidget.util.WeatherCodeMapper
import kotlin.math.roundToInt

object WidgetLayoutBuilder {

    fun build(context: Context, data: WeatherData?): RemoteViews {
        val prefs = WeatherPrefs(context)
        val views = RemoteViews(context.packageName, R.layout.widget_weather)

        val opacity = prefs.backgroundOpacity
        val alpha = (opacity * 255 / 100)
        val bgColor = (alpha shl 24) or 0x000000
        views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)

        val typeface = getFontTypeface(context, prefs.selectedFont)
        val density = context.resources.displayMetrics.scaledDensity
        val usePixel = prefs.usePixelIcons

        if (data == null) {
            views.setImageViewBitmap(R.id.current_temp, textToBitmap("--", 28f, 0xFFFFFFFF.toInt(), typeface, true, density))
            return views
        }

        views.setImageViewBitmap(
            R.id.current_temp,
            textToBitmap("${data.current.temperature.roundToInt()}", 28f, 0xFFFFFFFF.toInt(), typeface, true, density)
        )
        views.setImageViewBitmap(
            R.id.current_icon,
            drawableToBitmap(context, WeatherCodeMapper.getIconResource(data.current.weatherCode, data.current.isNight, usePixel))
        )

        val dayIds = listOf(
            Triple(R.id.day1_label, R.id.day1_icon, Pair(R.id.day1_high, R.id.day1_low)),
            Triple(R.id.day2_label, R.id.day2_icon, Pair(R.id.day2_high, R.id.day2_low)),
            Triple(R.id.day3_label, R.id.day3_icon, Pair(R.id.day3_high, R.id.day3_low)),
            Triple(R.id.day4_label, R.id.day4_icon, Pair(R.id.day4_high, R.id.day4_low))
        )

        data.forecast.forEachIndexed { i, day ->
            if (i < dayIds.size) {
                val (labelId, iconId, tempIds) = dayIds[i]
                views.setImageViewBitmap(labelId, textToBitmap(day.dayLabel, 12f, 0xFFFFFFFF.toInt(), typeface, false, density))
                views.setImageViewBitmap(iconId, drawableToBitmap(context, WeatherCodeMapper.getIconResource(day.weatherCode, usePixel = usePixel)))
                views.setImageViewBitmap(tempIds.first, textToBitmap("${day.high.roundToInt()}", 12f, 0xFFFFFFFF.toInt(), typeface, false, density))
                views.setImageViewBitmap(tempIds.second, textToBitmap("${day.low.roundToInt()}", 11f, 0xFFAAAAAA.toInt(), typeface, false, density))
            }
        }

        return views
    }

    private fun textToBitmap(text: String, sizeSp: Float, color: Int, typeface: Typeface?, bold: Boolean, density: Float): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sizeSp * density
            this.color = color
            this.typeface = when {
                bold && typeface != null -> Typeface.create(typeface, Typeface.BOLD)
                bold -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                typeface != null -> typeface
                else -> Typeface.DEFAULT
            }
            textAlign = Paint.Align.CENTER
        }
        val baseline = -paint.ascent()
        val width = (paint.measureText(text) + 2).toInt().coerceAtLeast(1)
        val height = (baseline + paint.descent() + 2).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, width / 2f, baseline, paint)
        return bitmap
    }

    private fun getFontTypeface(context: Context, fontId: Int): Typeface? {
        return when (fontId) {
            WeatherPrefs.FONT_JETBRAINS_MONO -> ResourcesCompat.getFont(context, R.font.jetbrains_mono)
            WeatherPrefs.FONT_INSTRUMENT_SERIF -> ResourcesCompat.getFont(context, R.font.instrument_serif)
            WeatherPrefs.FONT_PERMANENT_MARKER -> ResourcesCompat.getFont(context, R.font.permanent_marker)
            WeatherPrefs.FONT_GOOGLE_SANS -> ResourcesCompat.getFont(context, R.font.google_sans)
            WeatherPrefs.FONT_DS_DIGITAL -> ResourcesCompat.getFont(context, R.font.ds_digital)
            WeatherPrefs.FONT_OXANIUM -> ResourcesCompat.getFont(context, R.font.oxanium)
            WeatherPrefs.FONT_DOTO -> ResourcesCompat.getFont(context, R.font.doto)
            else -> null
        }
    }

    private fun drawableToBitmap(context: Context, drawableRes: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableRes)!!
        val size = (48 * context.resources.displayMetrics.density).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }
}

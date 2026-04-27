package com.rickgramlich.weatherwidget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rickgramlich.weatherwidget.data.WeatherPrefs

class MainActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 100
    }

    private lateinit var prefs: WeatherPrefs
    private lateinit var weatherAppLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = WeatherPrefs(this)

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(0xFF000000.toInt())
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(64, 96, 64, 96)
        }

        val title = TextView(this).apply {
            text = "Weather Widget"
            textSize = 24f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }

        val message = TextView(this).apply {
            text = "\nLong-press your home screen to add the Weather Widget.\n"
            textSize = 16f
            setTextColor(0xFFAAAAAA.toInt())
            gravity = Gravity.CENTER
        }

        // --- Background Opacity ---
        val sectionBg = sectionHeader("Background Opacity")

        val opacityLabel = TextView(this).apply {
            text = "${prefs.backgroundOpacity}%"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 8)
        }

        val opacitySeekBar = SeekBar(this).apply {
            max = 100
            progress = prefs.backgroundOpacity
        }

        val opacityHint = TextView(this).apply {
            text = "0% = transparent  \u2022  100% = solid black"
            textSize = 12f
            setTextColor(0xFF666666.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 0)
        }

        opacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                opacityLabel.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefs.backgroundOpacity = seekBar?.progress ?: 80
                WeatherWidgetProvider.updateAllWidgets(this@MainActivity)
            }
        })

        // --- Weather App Selector ---
        val sectionApp = sectionHeader("Tap Widget Opens")

        weatherAppLabel = TextView(this).apply {
            text = getWeatherAppDisplayName()
            textSize = 16f
            setTextColor(0xFF4FC3F7.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            setOnClickListener { showAppPicker() }
        }

        val appHint = TextView(this).apply {
            text = "Tap above to change which app opens\nwhen tapping the forecast"
            textSize = 12f
            setTextColor(0xFF666666.toInt())
            gravity = Gravity.CENTER
        }

        // --- Refresh Interval ---
        val sectionRefresh = sectionHeader("Refresh Interval")

        val intervalOptions = listOf(15, 30, 60, 120, 240)
        val intervalLabels = listOf("15 minutes", "30 minutes", "1 hour", "2 hours", "4 hours")

        val refreshLabel = TextView(this).apply {
            val current = prefs.refreshIntervalMinutes
            val idx = intervalOptions.indexOf(current)
            text = if (idx >= 0) intervalLabels[idx] else "$current minutes"
            textSize = 16f
            setTextColor(0xFF4FC3F7.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Refresh Interval")
                    .setItems(intervalLabels.toTypedArray()) { _, which ->
                        val minutes = intervalOptions[which]
                        prefs.refreshIntervalMinutes = minutes
                        text = intervalLabels[which]
                        WeatherWidgetProvider.reschedulePeriodicRefresh(this@MainActivity)
                    }
                    .show()
            }
        }

        val refreshHint = TextView(this).apply {
            text = "How often the widget fetches new weather data\nTap the current weather on the widget to refresh manually"
            textSize = 12f
            setTextColor(0xFF666666.toInt())
            gravity = Gravity.CENTER
        }

        // --- Font Picker ---
        val sectionFont = sectionHeader("Font")

        val fontNames = listOf(
            "System", "JetBrains Mono", "Instrument Serif", "Permanent Marker",
            "Google Sans", "DS Digital", "Oxanium", "Doto"
        )

        val fontLabel = TextView(this).apply {
            text = fontNames[prefs.selectedFont]
            textSize = 16f
            setTextColor(0xFF4FC3F7.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Font")
                    .setItems(fontNames.toTypedArray()) { _, which ->
                        prefs.selectedFont = which
                        text = fontNames[which]
                        WeatherWidgetProvider.updateAllWidgets(this@MainActivity)
                    }
                    .show()
            }
        }

        val fontHint = TextView(this).apply {
            text = "Font used for all text in the widget"
            textSize = 12f
            setTextColor(0xFF666666.toInt())
            gravity = Gravity.CENTER
        }

        // --- Icon Pack ---
        val sectionIcons = sectionHeader("Icon Style")

        val iconPackLabel = TextView(this).apply {
            text = if (prefs.usePixelIcons) "Pixel" else "Default"
            textSize = 16f
            setTextColor(0xFF4FC3F7.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            setOnClickListener {
                val options = arrayOf("Default", "Pixel")
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Icon Style")
                    .setItems(options) { _, which ->
                        prefs.usePixelIcons = which == 1
                        text = options[which]
                        WeatherWidgetProvider.updateAllWidgets(this@MainActivity)
                    }
                    .show()
            }
        }

        val iconHint = TextView(this).apply {
            text = "Pixel icons pair well with DS Digital font"
            textSize = 12f
            setTextColor(0xFF666666.toInt())
            gravity = Gravity.CENTER
        }

        // Build layout
        layout.addView(title)
        layout.addView(message)
        layout.addView(divider())
        layout.addView(sectionBg)
        layout.addView(opacityLabel)
        layout.addView(opacitySeekBar)
        layout.addView(opacityHint)
        layout.addView(spacer(48))
        layout.addView(divider())
        layout.addView(sectionFont)
        layout.addView(fontLabel)
        layout.addView(fontHint)
        layout.addView(spacer(48))
        layout.addView(divider())
        layout.addView(sectionIcons)
        layout.addView(iconPackLabel)
        layout.addView(iconHint)
        layout.addView(spacer(48))
        layout.addView(divider())
        layout.addView(sectionApp)
        layout.addView(weatherAppLabel)
        layout.addView(appHint)
        layout.addView(spacer(48))
        layout.addView(divider())
        layout.addView(sectionRefresh)
        layout.addView(refreshLabel)
        layout.addView(refreshHint)

        scrollView.addView(layout)
        setContentView(scrollView)

        requestLocationPermission()
    }

    private fun sectionHeader(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(0xFF888888.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
            isAllCaps = true
        }
    }

    private fun divider(): android.view.View {
        return android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            ).apply { setMargins(0, 16, 0, 0) }
            setBackgroundColor(0xFF333333.toInt())
        }
    }

    private fun spacer(height: Int): android.view.View {
        return android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height
            )
        }
    }

    private fun getWeatherAppDisplayName(): String {
        val name = prefs.weatherAppName
        return if (name != null) name else "Widget Settings (no app selected)"
    }

    private fun showAppPicker() {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(mainIntent, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        val appNames = mutableListOf("None (open widget settings)")
        val appPackages = mutableListOf<String?>(null)

        for (app in apps) {
            appNames.add(app.loadLabel(pm).toString())
            appPackages.add(app.activityInfo.packageName)
        }

        AlertDialog.Builder(this)
            .setTitle("Select Weather App")
            .setItems(appNames.toTypedArray()) { _, which ->
                val pkg = appPackages[which]
                val name = if (which == 0) null else appNames[which]
                prefs.weatherAppPackage = pkg
                prefs.weatherAppName = name
                weatherAppLabel.text = getWeatherAppDisplayName()
                WeatherWidgetProvider.updateAllWidgets(this)
            }
            .show()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            triggerInitialFetch()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            triggerInitialFetch()
        }
    }

    private fun triggerInitialFetch() {
        val request = OneTimeWorkRequestBuilder<WeatherWidgetWorker>().build()
        WorkManager.getInstance(this).enqueue(request)
    }
}

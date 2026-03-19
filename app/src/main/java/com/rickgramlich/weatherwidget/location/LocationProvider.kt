package com.rickgramlich.weatherwidget.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.rickgramlich.weatherwidget.data.WeatherPrefs

class LocationProvider(private val context: Context) {

    fun getLocation(): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return getCachedLocation()
        }

        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location: Location? = Tasks.await(client.lastLocation)
            if (location != null) {
                val prefs = WeatherPrefs(context)
                prefs.lastLat = location.latitude.toFloat()
                prefs.lastLon = location.longitude.toFloat()
                Pair(location.latitude, location.longitude)
            } else {
                getCachedLocation()
            }
        } catch (e: Exception) {
            getCachedLocation()
        }
    }

    private fun getCachedLocation(): Pair<Double, Double>? {
        val prefs = WeatherPrefs(context)
        return if (prefs.hasLocation()) {
            Pair(prefs.lastLat.toDouble(), prefs.lastLon.toDouble())
        } else {
            null
        }
    }
}

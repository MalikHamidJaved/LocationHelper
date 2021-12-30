package nl.zoofy.consumer.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocationHelper(val listener: LocationUpdates) {
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE: Int = 9098
    }

    interface LocationUpdates {
        fun onLocationUpdated(location: Location)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun initLocation(context: Activity) {
        if (checkLocationPermission(context)) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (checkLocationSource(context)) {
                enableLocationUpdates(context)
            } else {
                openGPSActivity(context)
            }

        } else {
            requestLocationPermission(context)
        }
    }

    private fun openGPSActivity(context: Activity) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationUpdates(context: Activity) {
        fusedLocationClient.lastLocation.addOnCompleteListener(context) { task ->
            var location: Location? = task.result
            if (location == null) {
                setFusedLocation()
            } else {
                listener.onLocationUpdated(location)
            }
        }
    }

    private fun setFusedLocation() {

        LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }
    }

    private fun requestLocationPermission(context: Activity) {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkLocationSource(context: Context): Boolean {
        var locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


}
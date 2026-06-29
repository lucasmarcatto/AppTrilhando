package com.trilhando.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper(
    private val context: Context,
    private val onLocationReceived: (Double, Double) -> Unit,
    private val onError: (String) -> Unit
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        private const val TAG = "LocationHelper"
    }

    fun hasLocationPermission(): Boolean { //Verifica se as permissões de localização estão concedidas
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLocation() { //obtem a localização atual uma única vez
        if (!hasLocationPermission()) {
            onError("Permissão de localização não concedida")
            return
        }

        val locationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0) // não usa cache, pega a mais recente
            .build()

        fusedLocationClient.getCurrentLocation(locationRequest, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    onLocationReceived(lat, lon)
                } else {
                    onError("Localização indisponível")
                }
            }
            .addOnFailureListener { e ->
                onError("Erro ao obter localização: ${e.message}")
            }
    }
}
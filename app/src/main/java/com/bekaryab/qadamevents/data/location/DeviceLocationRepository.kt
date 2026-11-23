package com.bekaryab.qadamevents.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import com.bekaryab.qadamevents.domain.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class DeviceLocationRepository(
    context: Context
) : LocationRepository {

    private val appContext = context.applicationContext

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    override suspend fun getCurrentCity(): Result<String> {

        return try {

            // 1. Check location permission
            val permissionGranted = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                throw SecurityException("Location permission not granted")
            }

            // 2. Get current device location
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                com.google.android.gms.tasks.CancellationTokenSource().token
            ).await() ?: throw IllegalStateException(
                "Current location is unavailable"
            )

            // 3. Convert coordinates into a city name
            val city = withContext(Dispatchers.IO) {

                if (!Geocoder.isPresent()) {
                    throw IllegalStateException("Geocoder is unavailable")
                }

                @Suppress("DEPRECATION")
                val addresses = Geocoder(
                    appContext,
                    Locale.getDefault()
                ).getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )

                val address = addresses?.firstOrNull()
                    ?: throw IllegalStateException("Address not found")

                address.locality?.takeIf { it.isNotBlank() }
                    ?: address.subAdminArea?.takeIf { it.isNotBlank() }
                    ?: address.adminArea?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("City not found")
            }

            // 4. Return successful result
            Result.success(city)

        } catch (cancelled: CancellationException) {

            throw cancelled

        } catch (error: Exception) {

            Result.failure(error)
        }
    }
}
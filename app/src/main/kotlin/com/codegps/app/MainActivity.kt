package com.codegps.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codegps.app.location.LocationRepository
import com.codegps.app.ui.LocationPermissionStatus
import com.codegps.app.ui.LocationScreen
import com.codegps.app.ui.theme.CodeGpsTheme

/**
 * App entry point. Hosts a single Compose screen that requests location
 * permission and, once granted, displays live GPS readings.
 */
class MainActivity : ComponentActivity() {

    private val locationRepository by lazy { LocationRepository(applicationContext) }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            permissionStatus = if (grants.values.any { it }) {
                LocationPermissionStatus.GRANTED
            } else {
                LocationPermissionStatus.DENIED
            }
        }

    // Backing state read by the Compose tree below; set from the permission
    // launcher callback above and from onCreate's initial check.
    private var permissionStatus by mutableStateOf(LocationPermissionStatus.UNKNOWN)

    override fun onCreate(savedInstanceState: Bundle?) {
        // The HUD theme is always dark, regardless of the system light/dark
        // setting, so force light (i.e. white/cyan) status & navigation bar
        // icons rather than letting enableEdgeToEdge() infer them from the
        // system setting — on a light-system device that would otherwise
        // pick dark icons that vanish against our dark background.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        if (hasLocationPermission()) {
            permissionStatus = LocationPermissionStatus.GRANTED
        }

        setContent {
            val status = permissionStatus

            // collectAsStateWithLifecycle only collects the flow while this
            // activity is at least STARTED, so location updates are
            // automatically paused/resumed with the activity lifecycle and
            // the underlying callback is unregistered when collection stops
            // (see LocationRepository.observeLocationUpdates).
            val reading by if (status == LocationPermissionStatus.GRANTED) {
                locationRepository.observeLocationUpdates().collectAsStateWithLifecycle(initialValue = null)
            } else {
                remember { mutableStateOf(null) }
            }

            CodeGpsTheme {
                LocationScreen(
                    permissionStatus = status,
                    reading = reading,
                    onRequestPermission = {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            )
                        )
                    },
                )
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }
}

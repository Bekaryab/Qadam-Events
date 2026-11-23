
package com.bekaryab.qadamevents.ui.explore

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc

private val LocationNavy = Color(0xFF2E3768)
private val LocationMuted = Color(0xFF7B8AA0)

@Composable
fun ExploreLocationHeader(
    viewModel: ExploreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val locationState by viewModel.locationUiState.collectAsState()

    var permissionDenied by rememberSaveable {
        mutableStateOf(false)
    }

    // Android runtime permission launcher.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->

        permissionDenied = !granted

        if (granted) {
            viewModel.requestCurrentCity()
        } else {
            viewModel.clearLocationFilter()
        }
    }

    // Checks existing permission before requesting it.
    fun requestLocation() {

        permissionDenied = false

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            viewModel.requestCurrentCity()
        } else {
            permissionLauncher.launch(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    val locationLabel = when (val state = locationState) {
        ExploreLocationUiState.Idle ->
            "Use my location"

        ExploreLocationUiState.Loading ->
            "Detecting location..."

        is ExploreLocationUiState.Success ->
            state.city

        is ExploreLocationUiState.Error ->
            "Location unavailable"
    }

    Column(
        modifier = modifier
    ) {

        Text(
            text = "Explore",
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            lineHeight = 32.sp,
            color = LocationNavy,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            // Location button.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        requestLocation()
                    }
            ) {

                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_place
                    ),
                    contentDescription = "Location",
                    tint = LocationNavy,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = locationLabel,
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = LocationNavy,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(4.dp))

                if (locationState is ExploreLocationUiState.Loading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = LocationNavy
                    )

                } else {

                    Icon(
                        painter = painterResource(
                            id = R.drawable.ic_expand_more
                        ),
                        contentDescription = "Detect location",
                        tint = LocationMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Clear city filter.
            if (locationState is ExploreLocationUiState.Success) {

                Text(
                    text = "Clear",
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = LocationMuted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            viewModel.clearLocationFilter()
                        }
                )
            }
        }

        // Permission and location errors.
        val errorMessage = when {
            permissionDenied ->
                "Location permission denied. Allow access to find nearby events."

            locationState is ExploreLocationUiState.Error ->
                (locationState as ExploreLocationUiState.Error).message

            else -> null
        }

        if (errorMessage != null) {

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = errorMessage,
                fontFamily = AlumniSansSc,
                fontSize = 11.sp,
                color = Color(0xFFB45353)
            )
        }
    }
}

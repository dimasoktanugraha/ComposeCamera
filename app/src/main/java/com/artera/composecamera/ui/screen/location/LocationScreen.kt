package com.artera.composecamera.ui.screen.location

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.artera.composecamera.di.Injection
import com.artera.composecamera.ui.ViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artera.composecamera.model.LocationDetails
import com.artera.composecamera.ui.component.ButtonPrimary
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.Builder.IMPLICIT_MIN_UPDATE_INTERVAL
import com.google.android.gms.tasks.Task

private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var locationCallback: LocationCallback
private lateinit var locationRequest: LocationRequest

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationScreen(
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideRepository())
    ),
    navigateBack: () -> Unit,
) {

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    PermissionsRequired(
        multiplePermissionsState = permissionState,
        permissionsNotGrantedContent = {
            navigateBack()
        },
        permissionsNotAvailableContent = {
            navigateBack()
        }
    ) {
        LocationView(
            requestPermission = {
                permissionState.launchMultiplePermissionRequest()
            }
        )
    }
}

@Composable
fun LocationView(
    requestPermission: () -> Unit,
) {
    val context = LocalContext.current
    var currentLocation by remember {
        mutableStateOf(LocationDetails(0.toDouble(), 0.toDouble()))
    }
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
        .apply {
            setWaitForAccurateLocation(true)
            setMinUpdateIntervalMillis(IMPLICIT_MIN_UPDATE_INTERVAL)
            setMaxUpdateDelayMillis(100000)
        }.build()
    locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            for (lo in p0.locations) {
                Toast.makeText(context, "locationCallback ${lo.latitude}/${lo.longitude}", Toast.LENGTH_SHORT).show()
                currentLocation = LocationDetails(lo.latitude, lo.longitude)
            }
        }
    }

    val settingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == RESULT_OK)
            Toast.makeText(context, "settingResultRequest OK", Toast.LENGTH_SHORT).show()
        else {
            Toast.makeText(context, "settingResultRequest DENIED", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        checkLocationSetting(
            context = context,
            onDisabled = { intentSenderRequest ->
                settingResultRequest.launch(intentSenderRequest)
            },
            onEnabled = {
                getLastLocation(
                    context,
                    locationData = {
                        currentLocation = it
                    },
                    noPermission = requestPermission
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Location")
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "lat : ${currentLocation.latitude} \nlong : ${currentLocation.longitude}",
                    fontSize = 20.sp,
                    color = Color.Black,
                )
                ButtonPrimary({
                    checkLocationSetting(
                        context = context,
                        onDisabled = { intentSenderRequest ->
                            settingResultRequest.launch(intentSenderRequest)
                        },
                        onEnabled = {
                            getLastLocation(
                                context,
                                locationData = {
                                    currentLocation = it
                                },
                                noPermission = requestPermission
                            )
                        }
                    )
                }, "checkLocationSetting")
                ButtonPrimary({
                    getLastLocation(
                        context,
                        locationData = {
                            currentLocation = it
                        },
                        noPermission = requestPermission
                    )
                }, "getLastLocation")
                ButtonPrimary({
                    getLocationUpdates(context)
                }, "getLocationUpdates")
            }
        }
    }
}

fun getLastLocation(
    context: Context,
    locationData: (LocationDetails) -> Unit,
    noPermission: () -> Unit
) {
    if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, context) &&
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, context)
    ){
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                locationData(LocationDetails(location.latitude, location.longitude))
            } else {
                Toast.makeText(context, "Location is not found. Try Again", Toast.LENGTH_SHORT).show()
            }
        }
    }else{
        noPermission()
    }
}

fun getLocationUpdates(context: Context) {
    try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    } catch (exception: SecurityException) {
        Toast.makeText(context, "getLocationUpdates : ${exception.message}", Toast.LENGTH_SHORT).show()
    }
}

fun checkLocationSetting(
    context: Context,
    onDisabled: (IntentSenderRequest) -> Unit,
    onEnabled: () -> Unit
) {
    val client: SettingsClient = LocationServices.getSettingsClient(context)
    val builder: LocationSettingsRequest.Builder = LocationSettingsRequest
        .Builder()
        .addLocationRequest(locationRequest)

    val gpsSettingTask: Task<LocationSettingsResponse> =
        client.checkLocationSettings(builder.build())

    gpsSettingTask.addOnSuccessListener { onEnabled() }
    gpsSettingTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                val intentSenderRequest = IntentSenderRequest
                    .Builder(exception.resolution)
                    .build()
                onDisabled(intentSenderRequest)
            } catch (sendEx: IntentSender.SendIntentException) {
                // ignore here
            }
        }
    }

}

private fun checkPermission(permission: String, context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}
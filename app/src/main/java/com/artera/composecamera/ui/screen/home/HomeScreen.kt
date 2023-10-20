package com.artera.composecamera.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artera.composecamera.di.Injection
import com.artera.composecamera.ui.ViewModelFactory
import com.artera.composecamera.ui.component.ButtonPrimary
import com.artera.composecamera.ui.screen.camera.CameraViewModel
import com.artera.composecamera.ui.theme.ComposeCameraTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideRepository())
    ),
    navigateToCamera: () -> Unit,
    navigateToLocation: () -> Unit,
    navigateToPayment: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Camera")
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ButtonPrimary(navigateToCamera, "Camera")
                ButtonPrimary(navigateToLocation, "Location")
                ButtonPrimary(navigateToPayment, "Payment")
            }
        }
    }
}
package com.artera.composecamera.ui.screen.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artera.composecamera.di.Injection
import com.artera.composecamera.ui.ViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artera.composecamera.ui.common.PurchaseHelper

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PaymentScreen(
    purchaseHelper: PurchaseHelper,
    modifier: Modifier = Modifier,
    viewModel: PaymentViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideRepository())
    ),
    navigateBack: () -> Unit,
) {

    val buyEnabled by purchaseHelper.buyEnabled.collectAsState(false)
    val consumeEnabled by purchaseHelper.consumeEnabled.collectAsState(false)
    val productName by purchaseHelper.productName.collectAsState("")
    val statusText by purchaseHelper.statusText.collectAsState("")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Payment")
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
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    productName,
                    Modifier.padding(20.dp),
                    fontSize = 30.sp)

                Text(statusText)

                Row(Modifier.padding(20.dp)) {

                    Button(
                        onClick = { purchaseHelper.makePurchase() },
                        Modifier.padding(20.dp),
                        enabled = buyEnabled
                    ) {
                        Text("Purchase")
                    }

                    Button(
                        onClick = { purchaseHelper.consumePurchase() },
                        Modifier.padding(20.dp),
                        enabled = consumeEnabled
                    ) {
                        Text("Consume")
                    }
                }
            }
        }
    }
}
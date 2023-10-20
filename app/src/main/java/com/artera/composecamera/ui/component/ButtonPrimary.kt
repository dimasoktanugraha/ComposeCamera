package com.artera.composecamera.ui.component

import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artera.composecamera.ui.theme.ComposeCameraTheme

@Composable
fun ButtonPrimary(onButtonClicked: () -> Unit, text: String) { //higher-order function
    Button(
        onClick = onButtonClicked,
        modifier = Modifier.width(100.dp)
    ) { //lambda expression
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ComposeCameraTheme {
        ButtonPrimary(onButtonClicked = {}, "Button")
    }
}
package com.mattdangelo.flashpad.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBar(
    isSosActive: Boolean,
    onToggleSos: () -> Unit
) {
    TopAppBar(
        title = { Text("Flashpad") },
        actions = {
            IconButton(onClick = onToggleSos) {
                SOSIcon(tint = if (isSosActive) Color.Red else Color.White)
            }
        }
    )
}
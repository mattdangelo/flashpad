package com.mattdangelo.flashpad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mattdangelo.flashpad.ui.theme.FlashpadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        setContent {
            FlashpadTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Pad()
                }
            }
        }
    }
}

@Composable
fun Pad(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        Spacer(modifier = Modifier.statusBarsPadding().fillMaxWidth().height(0.dp))
        Box(modifier = modifier.fillMaxSize().padding(16.dp).clip(RoundedCornerShape(10.dp)).background(Color.DarkGray))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FlashpadTheme {
        Pad()
    }
}
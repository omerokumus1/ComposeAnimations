package com.omerokumus.composeanimations

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A reusable FPS counter that displays the current frames per second
 * and logs it to Logcat under the tag "ShaderPerformance".
 */
@Composable
fun FpsCounter(modifier: Modifier = Modifier) {
    var fps by remember { mutableIntStateOf(0) }
    var frameCount by remember { mutableIntStateOf(0) }
    var prevTime by remember { mutableLongStateOf(System.nanoTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTime ->
                frameCount++
                val elapsed = frameTime - prevTime
                if (elapsed >= 1_000_000_000L) {
                    fps = frameCount
                    Log.d("ShaderPerformance", "Current FPS: $fps")
                    frameCount = 0
                    prevTime = frameTime
                }
            }
        }
    }

    Text(
        text = "FPS: $fps",
        color = Color.Green,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

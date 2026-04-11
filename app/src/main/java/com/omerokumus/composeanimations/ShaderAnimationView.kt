package com.omerokumus.composeanimations

import android.graphics.RuntimeShader
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.dp

/**
 * AGSL Shader Source for the Shader Animation.
 * 
 * This shader creates a dynamic, multi-layered line/ring effect using nested loops.
 * 
 * Uniforms:
 * iResolution The dimensions of the drawing area (width, height).
 * iTime The elapsed time in seconds, used to drive the animation.
 */
private const val SHADER_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    half4 main(float2 fragCoord) {
        // Normalize coordinates to a range of [-1, 1] on the shorter axis,
        // maintaining aspect ratio.
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        
        // Slow down time for a smoother, more elegant animation.
        float t = iTime * 0.05;
        
        // Base width for the animated lines.
        float lineWidth = 0.002;
        
        // Optimization: Pre-calculate values that are constant within the loops
        // to reduce per-pixel processing cost.
        float l = length(uv);
        float m = mod(uv.x + uv.y, 0.2);

        float3 col = float3(0.0);
        
        // Outermost loop defines the three color channels (R, G, B).
        for (int j = 0; j < 3; j++) {
            float tj = t - 0.01 * float(j);
            
            // Innermost loop creates multiple overlapping layers of lines.
            for (int i = 0; i < 5; i++) {
                // Calculate the distance field based on time, UV coordinates, and noise (mod).
                // fract() creates the repeating/pulsing behavior.
                float val = fract(tj + float(i) * 0.01) * 5.0 - l + m;
                
                // Add intensity to the current color channel based on proximity to the distance field.
                // lineWidth * i*i determines the brightness/thickness of each layer.
                col[j] += lineWidth * float(i * i) / abs(val);
            }
        }

        // Output the final color with full opacity.
        return half4(col, 1.0);
    }
"""

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

/**
 * Performance Considerations for Shader Animations in Compose:
 *
 * 1. Shader Complexity:
 *    This shader uses nested loops (3x5 = 15 iterations per pixel). At a 1080p resolution,
 *    this results in ~31 million iterations per frame. On mobile hardware, keeping
 *    loop counts low is critical for maintaining 60+ FPS.
 *
 * 2. Pre-calculations:
 *    Values like 'length(uv)' and 'mod(uv.x + uv.y, 0.2)' are constant within the loops.
 *    Moving them outside the loops (as seen in the optimized SHADER_SRC) significantly
 *    reduces the number of ALU operations performed by the GPU per pixel.
 *
 * 3. Emulator vs. Physical Device:
 *    Android Emulators often use software rendering or translated OpenGL calls, which
 *    can be significantly slower than physical hardware. Lag observed on an emulator
 *    may not reflect the actual performance on a device.
 *
 * 4. Build Variants (Debug vs. Release):
 *    Jetpack Compose has significant overhead in Debug mode (recomposition tracking,
 *    layout validation, etc.). Performance testing should ALWAYS be conducted in
 *    Release mode with 'isMinifyEnabled = true'.
 *
 *    How to run in Release mode:
 *    - Android Studio: Open the 'Build Variants' tool window (usually bottom-left),
 *      and change the 'Active Build Variant' for the ':app' module to 'release'.
 *    - CLI: Run './gradlew assembleRelease' to build the APK, then install it
 *      using 'adb install app/build/outputs/apk/release/app-release.apk'.
 *    Note: You may need to configure a signing config in build.gradle.kts for 
 *    release builds to install correctly on some devices.
 *
 * 5. Drawing Strategy:
 *    Using 'Modifier.drawWithCache' ensures that the 'ShaderBrush' and other drawing
 *    objects are reused across frames, avoiding expensive allocations during the
 *    draw phase.
 */
@Composable
fun ShaderAnimationView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderAnimationContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun ShaderAnimationContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ShaderTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(SHADER_SRC) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time)
                val brush = ShaderBrush(shader)
                onDrawBehind {
                    drawRect(brush)
                }
            }
    ) {
        FpsCounter(Modifier.align(Alignment.TopEnd).padding(16.dp).padding(top = 32.dp))
    }
}

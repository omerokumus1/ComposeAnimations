package com.omerokumus.composeanimations

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.dp

/**
 * AGSL Shader Source for Warp Speed.
 * 
 * This shader creates an elongated "warp drive" effect using stretched elliptical 
 * distance fields and horizontal coordinate offsets.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the "speed" of the rings.
 */
private const val WARP_SPEED_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.07;
        float lw = 0.002;

        float3 col = float3(0.0);
        
        // Optimization: Pre-calculate the stretched distance metric
        // Elongated horizontal rings like warp drive
        float dist = length(uv * float2(0.4, 1.0));
        float offsetX = mod(uv.x, 0.3);

        for (int j = 0; j < 3; j++) {
            float tj = t - 0.008 * float(j);
            for (int i = 0; i < 6; i++) {
                // Procedural distance field for warp effect
                float val = fract(tj + float(i) * 0.015) * 4.0 - dist + offsetX;
                col[j] += lw * float(i * i) / abs(val);
            }
        }
        
        // Apply a cool blue-cyan futuristic tint
        col *= float3(0.6, 0.9, 1.3);
        
        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Warp Speed:
 *
 * 1. Geometry Complexity:
 *    Uses a non-uniform scale on the length calculation `uv * float2(0.4, 1.0)`.
 *    This is efficient but creates the characteristic oval warp shape.
 *
 * 2. Loop Complexity:
 *    3x6 = 18 iterations. Similar to Diamond Rings, but with an added 
 *    coordinate-based offset `mod(uv.x, 0.3)` to create horizontal variation.
 */
@Composable
fun WarpSpeedView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        WarpSpeedContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun WarpSpeedContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "WarpTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(WARP_SPEED_SRC) }

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

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
 * AGSL Shader Source for Diamond Rings.
 * 
 * This shader is a variation of the original ring shader but uses a 
 * "diamond distance" metric (L1 norm) instead of standard Euclidean distance (L2 norm).
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the ring expansion.
 */
private const val DIAMOND_RINGS_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.05;
        float lw = 0.002;

        float3 col = float3(0.0);
        
        // Calculate diamond distance once outside the inner loop
        float dist = abs(uv.x) + abs(uv.y);
        float m = mod(uv.x * uv.y, 0.15);

        for (int j = 0; j < 3; j++) {
            float tj = t - 0.01 * float(j);
            for (int i = 0; i < 6; i++) {
                // Procedural distance field for diamond-shaped rings
                float val = fract(tj + float(i) * 0.012) * 5.0 - dist + m;
                col[j] += lw * float(i * i) / abs(val);
            }
        }
        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Diamond Rings:
 *
 * 1. Geometric Optimization:
 *    The 'diamond distance' (abs(x) + abs(y)) is significantly cheaper to 
 *    calculate than circular distance (sqrt(x*x + y*y)).
 *
 * 2. Loop Complexity:
 *    3x6 = 18 iterations per pixel. Slightly more demanding than the 
 *    standard ring shader, but the simplified distance math offsets this.
 */
@Composable
fun DiamondRingsView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        DiamondRingsContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun DiamondRingsContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "DiamondTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(DIAMOND_RINGS_SRC) }

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

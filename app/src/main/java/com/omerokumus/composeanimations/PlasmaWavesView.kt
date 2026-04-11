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
 * AGSL Shader Source for Plasma Waves.
 * 
 * This shader creates a fluid, colorful "plasma" effect by combining multiple 
 * sine waves based on coordinates and time.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive wave motion.
 */
private const val PLASMA_WAVES_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.8;

        // Combine four different sine-based patterns
        float v1 = sin(uv.x * 10.0 + t);
        float v2 = sin(uv.y * 10.0 + t * 0.7);
        float v3 = sin((uv.x + uv.y) * 10.0 + t * 1.3);
        float v4 = sin(length(uv) * 10.0 - t * 2.0);
        
        // Average the patterns to create the base plasma value
        float value = (v1 + v2 + v3 + v4) / 4.0;

        // Map the base value to an RGB color space using phase-shifted sine waves
        float r = sin(value * 3.14159 + 0.0) * 0.5 + 0.5;
        float g = sin(value * 3.14159 + 2.094) * 0.5 + 0.5;
        float b = sin(value * 3.14159 + 4.189) * 0.5 + 0.5;

        return half4(r, g, b, 1.0);
    }
"""

/**
 * Performance Considerations for Plasma Waves:
 *
 * 1. Computational Cost:
 *    Unlike the ring shader, this shader does not use loops, making it much 
 *    cheaper per pixel. It primarily uses sine and length functions.
 *
 * 2. Optimization:
 *    The math is straightforward, but on older hardware, the trigonometric 
 *    functions (sin) can still be taxing if overused. 
 *
 * 3. General Rules apply:
 *    - Use Release builds for accurate testing.
 *    - Modifier.drawWithCache is used to avoid object allocation.
 */
@Composable
fun PlasmaWavesView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PlasmaWavesContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun PlasmaWavesContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "PlasmaTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(PLASMA_WAVES_SRC) }

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

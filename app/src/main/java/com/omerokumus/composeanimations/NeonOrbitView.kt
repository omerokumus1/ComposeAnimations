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
 * AGSL Shader Source for Neon Orbit.
 * 
 * This shader is a variation of the ring shader that introduces:
 * 1. Rotating angular offsets based on polar coordinates.
 * 2. Asymmetric hue shifting between color channels.
 * 3. Boosted saturation for a vibrant neon look.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the orbit rotation and expansion.
 */
private const val NEON_ORBIT_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.06;
        float lw = 0.002;
        
        // Use atan(y, x) for polar angle
        float a = atan(uv.y, uv.x);

        float3 col = float3(0.0);
        
        // Pre-calculate values outside the inner loop
        float l = length(uv);
        float m = mod(uv.x + uv.y, 0.2);

        for (int j = 0; j < 3; j++) {
            float fj = float(j);
            // Angular shift creates the "orbiting" effect per color channel
            float angularShift = sin(a * 3.0 + iTime * 0.5 + fj) * 0.08;
            float tj = t - 0.01 * fj;
            
            for (int i = 0; i < 5; i++) {
                float val = fract(tj + float(i) * 0.01) * 5.0 - l + angularShift + m;
                col[j] += lw * float(i * i) / abs(val);
            }
        }
        
        // Boost saturation for neon feel
        col *= float3(1.1, 0.8, 1.2);
        
        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Neon Orbit:
 *
 * 1. Loop Workload:
 *    3x5 = 15 iterations per pixel. Moderate complexity.
 *
 * 2. Polar Coordinates:
 *    Using 'atan' per pixel is slightly more expensive than simple distance 
 *    calculations, but essential for the rotating orbit effect.
 *
 * 3. Optimization:
 *    Moving 'length(uv)' and 'mod' outside the inner loop reduces redundant 
 *    math operations.
 */
@Composable
fun NeonOrbitView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        NeonOrbitContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun NeonOrbitContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbitTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(NEON_ORBIT_SRC) }

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

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
 * AGSL Shader Source for Hologram.
 * 
 * This shader creates a futuristic holographic effect using:
 * 1. Horizontal scan lines.
 * 2. Multi-source interference patterns.
 * 3. Rainbow interference colors (HSV).
 * 4. Random digital glitch lines.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the pattern and glitches.
 */
private const val HOLOGRAM_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    float hashVal(float2 p) {
        return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
    }

    float3 hsv2rgb(float3 c) {
        float3 p = abs(fract(float3(c.x) + float3(0.0, 2.0/3.0, 1.0/3.0)) * 6.0 - 3.0);
        return c.z * mix(float3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
    }

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.5;

        // 1. Scan lines: moving horizontal pattern
        float scan = sin(uv.y * 80.0 + t * 5.0) * 0.5 + 0.5;
        scan = pow(scan, 0.3) * 0.15 + 0.85;

        // 2. Interference pattern: overlapping wave sources
        float pattern = 0.0;
        pattern += sin(uv.x * 30.0 + t * 2.0) * 0.5;
        pattern += sin(uv.y * 25.0 - t * 1.5) * 0.5;
        pattern += sin((uv.x + uv.y) * 20.0 + t) * 0.3;
        pattern += sin(length(uv) * 15.0 - t * 3.0) * 0.4;

        // 3. Rainbow color from pattern + angle
        float hue = fract(pattern * 0.15 + uv.x * 0.3 + uv.y * 0.2 + t * 0.1);
        float3 col = hsv2rgb(float3(hue, 0.6, 0.9));

        // 4. Shimmer effect: view-dependent highlight
        float shimmer = sin(uv.x * 50.0 + uv.y * 30.0 + t * 4.0) * 0.5 + 0.5;
        col = mix(col, float3(1.0), shimmer * 0.15);

        col *= scan;

        // 5. Digital glitch lines: random bright horizontal strips
        float glitch = step(0.98, hashVal(float2(floor(uv.y * 40.0), floor(t * 10.0))));
        col += float3(0.3, 0.6, 1.0) * glitch * 0.5;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Hologram:
 *
 * 1. Pattern Summing:
 *    The shader calculates 4 overlapping sine patterns. This is extremely 
 *    efficient per pixel and performs well across all hardware.
 *
 * 2. Color Conversion:
 *    Uses 'hsv2rgb' for dynamic hue cycling. While adding some ALU cost, 
 *    it remains well within the 60 FPS budget.
 *
 * 3. Branching:
 *    The glitch logic uses 'step' to avoid 'if' statements, ensuring high 
 *    parallelism on the GPU.
 */
@Composable
fun HologramView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        HologramContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun HologramContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "HologramTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(HOLOGRAM_SRC) }

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

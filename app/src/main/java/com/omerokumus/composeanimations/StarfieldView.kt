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
 * AGSL Shader Source for Starfield.
 * 
 * This shader creates a layered, scrolling starfield effect with procedural stars
 * and twinkling logic.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the scrolling and twinkling.
 */
private const val STARFIELD_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    float hashVal(float2 p) {
        return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
    }

    float2 hash2(float2 p) {
        return fract(sin(float2(dot(p, float2(127.1, 311.7)),
                                dot(p, float2(269.5, 183.3)))) * 43758.5453);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / iResolution.xy;
        float3 col = float3(0.0);

        // Iterate through 3 layers of stars to create depth (parallax effect)
        for (int layer = 0; layer < 3; layer++) {
            float fl = float(layer);
            float scale = 50.0 + fl * 80.0;
            float speed = 0.03 + fl * 0.02;
            float brightness = 1.0 - fl * 0.25;

            float2 st = uv * scale;
            // Scroll the stars vertically based on time and layer speed
            st.y += iTime * speed * scale;
            
            float2 cell = floor(st);
            float2 f = fract(st);

            // Procedural star generation using hashing
            float h = hashVal(cell);
            if (h > 0.95) {
                float2 center = hash2(cell);
                float d = length(f - center);
                // Dynamic twinkling effect
                float twinkle = sin(iTime * 3.0 + h * 100.0) * 0.3 + 0.7;
                float star = smoothstep(0.1, 0.0, d) * twinkle * brightness;
                col += float3(star);
            }
        }

        // Add a subtle deep blue background tint
        col += float3(0.01, 0.01, 0.03);
        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Starfield:
 *
 * 1. Branching (if-statement):
 *    Shaders perform best without branching. However, the 'if (h > 0.95)' check
 *    is used here to skip expensive calculations for empty space. On modern
 *    GPUs, this is generally efficient for sparse effects like stars.
 *
 * 2. Loop Complexity:
 *    3 iterations per pixel. This is quite lightweight compared to complex
 *    fractal shaders, making it suitable for high-resolution screens.
 *
 * 3. Twinkling Logic:
 *    The use of 'sin' and 'hash' functions per layer adds some ALU cost,
 *    but it remains well within the performance budget for 60/120 FPS.
 */
@Composable
fun StarfieldView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        StarfieldContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun StarfieldContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "StarfieldTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(STARFIELD_SRC) }

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

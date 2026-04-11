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
 * AGSL Shader Source for Matrix Rain.
 * 
 * This shader creates the iconic digital rain effect using grid-based hashing,
 * procedural character flicker, and trailing brightness logic.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the rain fall and flicker.
 */
private const val MATRIX_RAIN_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    float hashVal(float2 p) {
        return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / iResolution.xy;
        
        // Define the grid columns and rows based on resolution
        float columns = 40.0;
        float2 grid = float2(columns, columns * iResolution.y / iResolution.x);

        // Identify the current grid cell and pixel coordinates within it
        float2 cell = floor(uv * grid);
        float2 f = fract(uv * grid);

        // Randomize speed and offset per column
        float columnSpeed = 2.0 + hashVal(float2(cell.x, 0.0)) * 4.0;
        float columnOffset = hashVal(float2(cell.x, 1.0)) * 100.0;

        // Calculate the rain fall position with looping trail
        float rain = fract(-iTime * columnSpeed * 0.1 + cell.y * 0.05 + columnOffset);
        float trail = pow(rain, 3.0);

        // Procedural flicker for the "characters"
        float charFlicker = step(0.3, hashVal(cell + floor(iTime * 8.0)));
        float brightness = trail * charFlicker;

        // Highlight the "head" of the rain drop
        float head = step(0.95, rain);
        
        // Classic matrix green color palette
        float3 col = float3(0.0, brightness * 0.8, brightness * 0.2);
        // Add white head highlight
        col += float3(head * 0.8, head, head * 0.8);

        // Mask the pixel to create "character" shapes within the grid cells
        float charShape = smoothstep(0.1, 0.2, f.x) * smoothstep(0.9, 0.8, f.x)
                        * smoothstep(0.05, 0.15, f.y) * smoothstep(0.95, 0.85, f.y);
        col *= charShape;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Matrix Rain:
 *
 * 1. Grid Logic:
 *    The shader calculates grid cells on the fly. This is extremely efficient 
 *    as it avoids texture lookups.
 *
 * 2. Hashing:
 *    Uses a standard sine-based hash. While 'sin' is slightly more expensive 
 *    than bitwise hashing, it's consistent across AGSL implementations.
 *
 * 3. Minimal Branching:
 *    Uses 'step' and 'smoothstep' instead of 'if' statements to maintain 
 *    high GPU throughput.
 */
@Composable
fun MatrixRainView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        MatrixRainContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun MatrixRainContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "MatrixTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(MATRIX_RAIN_SRC) }

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

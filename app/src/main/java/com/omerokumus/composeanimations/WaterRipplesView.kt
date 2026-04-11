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
 * AGSL Shader Source for Water Ripples.
 * 
 * This shader simulates multiple overlapping wave sources to create a fluid 
 * water ripple effect.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the wave propagation.
 */
private const val WATER_RIPPLES_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.6;

        // Define three moving emitters for the ripples
        float2 e1 = float2(sin(t * 0.7) * 0.5, cos(t * 0.5) * 0.5);
        float2 e2 = float2(cos(t * 0.4) * 0.8, sin(t * 0.6) * 0.3);
        float2 e3 = float2(sin(t * 0.3) * 0.3, cos(t * 0.8) * 0.7);

        float wave = 0.0;
        float d1 = length(uv - e1);
        float d2 = length(uv - e2);
        float d3 = length(uv - e3);
        
        // Sum of three sine waves with distance-based attenuation
        wave += sin(d1 * 20.0 - iTime * 4.0) / (1.0 + d1 * 5.0);
        wave += sin(d2 * 20.0 - iTime * 3.5) / (1.0 + d2 * 5.0);
        wave += sin(d3 * 20.0 - iTime * 4.5) / (1.0 + d3 * 5.0);

        float3 baseColor = float3(0.0, 0.1, 0.3); // Deep blue base
        float3 col = baseColor + wave * float3(0.1, 0.3, 0.2); // Greenish ripple tint
        
        // Add "specular" highlights on the peaks of the waves
        col += pow(max(wave, 0.0), 8.0) * float3(0.8, 0.9, 1.0);

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Water Ripples:
 *
 * 1. Computational Profile:
 *    Very efficient. It uses only a few 'sin' and 'length' calls without 
 *    any loops or heavy branching.
 *
 * 2. Visual Quality:
 *    The 'pow' function for specular highlights is cheap but very effective 
 *    for creating a high-fidelity look.
 */
@Composable
fun WaterRipplesView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        WaterRipplesContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun WaterRipplesContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaterTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(WATER_RIPPLES_SRC) }

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

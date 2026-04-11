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
 * AGSL Shader Source for Tunnel effect.
 * 
 * This shader creates an infinite perspective tunnel by transforming 
 * Cartesian coordinates (x, y) into polar coordinates (radius, angle).
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the tunnel speed.
 */
private const val TUNNEL_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float r = length(uv);
        float a = atan(uv.y, uv.x);

        // Transformation to perspective coordinates
        // U represents depth, V represents angle
        float tunnelU = 1.0 / (r + 0.001);
        float tunnelV = a / 3.14159265;

        // Move through the tunnel based on time
        tunnelU += iTime * 2.0;

        // Create a checkered pattern using floor and mod
        float pattern = mod(floor(tunnelU * 4.0) + floor(tunnelV * 8.0), 2.0);
        float3 col = mix(float3(0.05, 0.0, 0.1), float3(0.2, 0.1, 0.4), pattern);

        // Add a central glow effect
        float glow = 1.0 / (r * 8.0 + 1.0);
        col += float3(0.2, 0.5, 1.0) * glow;

        // Fade out the center to hide the singularity
        float fade = smoothstep(0.0, 0.3, r);
        col *= fade;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Tunnel:
 *
 * 1. Geometry Transformation:
 *    The coordinate transformation from Cartesian to Polar is computationally 
 *    light but visually very effective for creating depth.
 *
 * 2. Singularity Handling:
 *    The +0.001 in '1.0 / (r + 0.001)' prevents division by zero at the 
 *    center of the screen.
 *
 * 3. General Rules:
 *    - Modifier.drawWithCache is used to ensure the ShaderBrush is only 
 *      re-created when absolutely necessary.
 */
@Composable
fun TunnelView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        TunnelContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun TunnelContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "TunnelTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(TUNNEL_SRC) }

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

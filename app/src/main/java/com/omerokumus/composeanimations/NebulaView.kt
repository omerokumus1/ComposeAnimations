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
 * AGSL Shader Source for Nebula.
 * 
 * This shader creates a deep-space nebula effect combining:
 * 1. Domain-warped fBm for organic gas clouds.
 * 2. Multi-layered color blending (Purple, Blue, Pink, Orange).
 * 3. A bright central core glow.
 * 4. Procedural background stars with twinkling logic.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive gas movement and twinkling.
 */
private const val NEBULA_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    float hashVal(float2 p) {
        return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
    }

    float valueNoise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        f = f * f * (3.0 - 2.0 * f);
        float a = hashVal(i);
        float b = hashVal(i + float2(1.0, 0.0));
        float c = hashVal(i + float2(0.0, 1.0));
        float d = hashVal(i + float2(1.0, 1.0));
        return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
    }

    float fbm(float2 p) {
        float v = 0.0;
        float a = 0.5;
        for (int i = 0; i < 6; i++) {
            v += a * valueNoise(p);
            p *= 2.0;
            a *= 0.5;
        }
        return v;
    }

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.15;

        // Domain-warped fBm for organic nebula shapes
        float2 p = uv * 1.5;
        float n1 = fbm(p + float2(t, t * 0.7));
        float n2 = fbm(p + n1 * 1.5 + float2(t * 0.3, -t * 0.2));
        float n3 = fbm(p + n2 * 1.5 + float2(-t * 0.2, t * 0.4));

        // Color layers: purple, blue, pink nebula gas
        float3 purple = float3(0.3, 0.1, 0.5) * smoothstep(0.2, 0.8, n1);
        float3 blue = float3(0.1, 0.2, 0.6) * smoothstep(0.3, 0.9, n2);
        float3 pink = float3(0.6, 0.15, 0.4) * smoothstep(0.3, 0.85, n3);
        float3 orange = float3(0.5, 0.2, 0.05) * smoothstep(0.5, 0.95, n1 * n2);

        float3 col = float3(0.02, 0.01, 0.04);
        col += purple + blue + pink + orange;

        // Bright core glow
        float core = exp(-length(uv + float2(sin(t), cos(t * 0.7)) * 0.3) * 2.5);
        col += float3(0.4, 0.2, 0.5) * core;

        // Procedural stars with twinkling
        float stars = hashVal(floor(uv * 200.0 + 0.5));
        float starBright = step(0.97, stars);
        float twinkle = sin(iTime * 2.0 + stars * 100.0) * 0.3 + 0.7;
        col += float3(0.9, 0.9, 1.0) * starBright * twinkle;

        // Dim background stars
        float dimStars = hashVal(floor(uv * 80.0 + 10.0));
        col += float3(0.5, 0.5, 0.7) * step(0.96, dimStars) * 0.3;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Nebula:
 *
 * 1. Computational Profile (Very High):
 *    This shader uses triple-nested fBm calls (domain warping). 
 *    3 x 6 = 18 iterations of value noise per pixel. This is extremely 
 *    demanding for mobile GPUs.
 *
 * 2. Visual Fidelity vs. Performance:
 *    The complexity is necessary for the "gas" look. If lagging on device:
 *    - Reduce fBm octaves from 6 to 4.
 *    - Simplify star generation logic.
 *
 * 3. General Best Practices:
 *    - Performance must be measured in Release mode.
 *    - Modifier.drawWithCache prevents redundant object creation.
 */
@Composable
fun NebulaView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        NebulaContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun NebulaContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "NebulaTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(NEBULA_SRC) }

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

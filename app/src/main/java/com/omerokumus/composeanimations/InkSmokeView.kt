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
 * AGSL Shader Source for Ink Smoke.
 * 
 * This shader simulates the flow of ink in water using deep domain warping:
 * 1. Layered fBm noise for organic shapes.
 * 2. Multiple levels of warping (noise offsetting noise) to create "tendrils".
 * 3. A moody, deep-sea color palette (Burgundy, Teal, Deep Blue).
 * 4. Wispy highlights based on noise density.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the smoke movement.
 */
private const val INK_SMOKE_SRC = """
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
        float t = iTime * 0.2;

        // Multiple layers of domain-warped noise for smoke tendrils
        float2 p = uv * 1.8;

        // Level 1: main smoke body
        float2 q = float2(fbm(p + float2(t * 0.4, t * 0.3)),
                       fbm(p + float2(t * 0.2, -t * 0.4)));
        
        // Level 2: secondary warping for fine details
        float2 r2 = float2(fbm(p + q * 4.0 + float2(1.7, 9.2) + t * 0.15),
                        fbm(p + q * 4.0 + float2(8.3, 2.8) - t * 0.1));
        
        // Final noise result
        float f = fbm(p + r2 * 2.0);

        // Color palette: deep ink colors
        float3 ink1 = float3(0.05, 0.0, 0.1);     // deep purple-black
        float3 ink2 = float3(0.1, 0.2, 0.5);       // deep blue
        float3 ink3 = float3(0.4, 0.1, 0.3);       // burgundy
        float3 ink4 = float3(0.0, 0.3, 0.4);       // teal

        // Layered blending for moody ink look
        float3 col = mix(ink1, ink2, clamp(f * 2.0, 0.0, 1.0));
        col = mix(col, ink3, clamp(q.x * 1.5, 0.0, 1.0));
        col = mix(col, ink4, clamp(r2.y * 0.8, 0.0, 1.0));

        // Add wispy highlights on the densest parts of the smoke
        float wisp = pow(clamp(f * 1.5, 0.0, 1.0), 3.0);
        col += float3(0.3, 0.2, 0.4) * wisp;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Ink Smoke:
 *
 * 1. High Complexity (fBm Overload):
 *    This shader is extremely heavy. It calls 'fbm' 5 times per pixel.
 *    6 iterations x 5 = 30 value noise calls per pixel per frame.
 *
 * 2. GPU Load:
 *    On high-resolution displays, this will be the ultimate stress test 
 *    for the mobile GPU. 
 *
 * 3. Optimization Strategy:
 *    If the FPS drops below 60 on your device:
 *    - Reduce the fbm loop from 6 to 4 in the 'fbm' function.
 *    - Reduce the number of warping levels.
 */
@Composable
fun InkSmokeView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        InkSmokeContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun InkSmokeContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "SmokeTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(INK_SMOKE_SRC) }

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

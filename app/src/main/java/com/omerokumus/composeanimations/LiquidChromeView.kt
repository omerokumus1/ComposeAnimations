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
 * AGSL Shader Source for Liquid Chrome.
 * 
 * This shader creates a metallic, fluid-like surface using domain warping 
 * and sharp value-noise based reflections.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the fluid motion.
 */
private const val LIQUID_CHROME_SRC = """
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

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.3;

        // Domain warping for metallic fluid motion
        float2 p = uv * 2.0;
        float n1 = valueNoise(p + float2(t, t * 0.6));
        float n2 = valueNoise(p + n1 * 1.5 + float2(-t * 0.4, t * 0.3));
        float n3 = valueNoise(p * 1.5 + n2 * 1.2 + float2(t * 0.2, -t * 0.5));

        // Chrome-like reflections: sharp highlights and dark valleys
        float chrome = n3 * 0.5 + 0.5;
        chrome = pow(chrome, 0.6);

        // Dark metallic color palette
        float3 silver = float3(0.2, 0.2, 0.25);
        float3 highlight = float3(0.5, 0.5, 0.6);
        float3 shadow = float3(0.02, 0.01, 0.05);
        float3 tint = float3(0.15, 0.2, 0.4);

        float3 col = mix(shadow, silver, chrome);
        col = mix(col, highlight, smoothstep(0.8, 0.98, chrome));
        col += tint * smoothstep(0.3, 0.6, n1) * 0.15;

        // Intense specular highlights
        float spec = pow(max(chrome, 0.0), 12.0);
        col += float3(0.6, 0.6, 0.8) * spec * 0.3;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Liquid Chrome:
 *
 * 1. Computational Profile:
 *    Moderate to High. Uses triple-nested noise logic (domain warping) 
 *    per pixel to create organic metal folds.
 *
 * 2. High Power Functions:
 *    The 'pow(chrome, 12.0)' for specular highlights is very effective 
 *    but requires a GPU with good precision for smooth gradients.
 */
@Composable
fun LiquidChromeView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        LiquidChromeContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun LiquidChromeContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ChromeTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(LIQUID_CHROME_SRC) }

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

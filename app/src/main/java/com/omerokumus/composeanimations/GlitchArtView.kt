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
 * AGSL Shader Source for Glitch Art.
 * 
 * This shader simulates digital video corruption using:
 * 1. Random block-based horizontal offsets.
 * 2. RGB channel splitting (chromatic aberration).
 * 3. Animated geometric background patterns.
 * 4. High-frequency scan lines and random noise strips.
 * 5. Vignette for depth.
 */
private const val GLITCH_ART_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    float hashVal(float2 p) {
        return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / iResolution.xy;
        float t = iTime;

        // 1. Random block glitch offset
        float blockY = floor(uv.y * 20.0);
        float glitchSeed = floor(t * 3.0);
        float blockRand = hashVal(float2(blockY, glitchSeed));
        float isGlitched = step(0.85, blockRand);
        float offset = (hashVal(float2(blockY + 1.0, glitchSeed)) - 0.5) * 0.15 * isGlitched;

        // 2. RGB channel split coordinates
        float2 uvR = uv + float2(offset + sin(t * 10.0) * 0.005, 0.0);
        float2 uvG = uv;
        float2 uvB = uv - float2(offset + sin(t * 10.0) * 0.005, 0.0);

        // 3. Base pattern: animated geometric shapes
        float2 grid = fract(uv * 8.0 + float2(t * 0.2, t * 0.1));
        float basePattern = step(0.4, grid.x) * step(0.4, grid.y);
        basePattern += sin(uv.x * 30.0 + t * 5.0) * sin(uv.y * 20.0 - t * 3.0) * 0.3;

        float2 gridR = fract(uvR * 8.0 + float2(t * 0.2, t * 0.1));
        float2 gridB = fract(uvB * 8.0 + float2(t * 0.2, t * 0.1));
        float patR = step(0.4, gridR.x) * step(0.4, gridR.y) + sin(uvR.x * 30.0 + t * 5.0) * 0.3;
        float patB = step(0.4, gridB.x) * step(0.4, gridB.y) + sin(uvB.x * 30.0 + t * 5.0) * 0.3;

        float3 col = float3(patR * 0.9, basePattern * 0.9, patB * 0.9);

        // 4. Scan lines
        float scan = sin(uv.y * 300.0) * 0.5 + 0.5;
        col *= 0.85 + scan * 0.15;

        // 5. Random bright noise lines
        float noiseLine = step(0.97, hashVal(float2(floor(uv.y * 200.0), floor(t * 20.0))));
        col += float3(0.5, 1.0, 0.8) * noiseLine * 0.4;

        // 6. Vignette
        float2 vc = uv - 0.5;
        col *= 1.0 - dot(vc, vc) * 0.8;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Glitch Art:
 *
 * 1. Pixel Sampling:
 *    The shader calculates coordinates for 3 separate channels (R, G, B) 
 *    but generates the pattern mathematically rather than sampling textures. 
 *    This is extremely efficient.
 *
 * 2. Visual Artifacts:
 *    The 'isGlitched' and 'noiseLine' logic use 'step' functions to maintain 
 *    a clean, non-branching execution path on the GPU.
 */
@Composable
fun GlitchArtView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GlitchArtContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun GlitchArtContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlitchTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(GLITCH_ART_SRC) }

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

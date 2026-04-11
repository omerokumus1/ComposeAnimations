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
 * AGSL Shader Source for Fractal Clouds.
 * 
 * This shader uses domain-warping and Fractional Brownian Motion (fBm) to 
 * create an organic, moving cloud-like texture.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive cloud movement.
 */
private const val FRACTAL_CLOUDS_SRC = """
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
        float2 uv = fragCoord / iResolution.xy;
        uv *= 3.0; // Scale the noise
        uv += float2(iTime * 0.08, iTime * 0.04); // Move clouds over time

        // Domain warping: using noise to offset the noise coordinates
        float f1 = fbm(uv);
        float f2 = fbm(uv + f1 * 2.0 + float2(iTime * 0.02, iTime * 0.03));

        // Define sky and cloud colors
        float3 sky = float3(0.1, 0.15, 0.35);
        float3 cloud = float3(0.9, 0.9, 1.0);
        
        // Blend sky and cloud based on the second fBm result
        float3 col = mix(sky, cloud, clamp(f2, 0.0, 1.0));

        // Add a subtle sunset/tint highlight based on the first fBm result
        col += float3(0.1, 0.05, 0.0) * f1 * 0.5;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Fractal Clouds:
 *
 * 1. Computational Intensity (High):
 *    This shader calls 'fbm' twice. Each 'fbm' has a loop of 6 iterations, 
 *    resulting in 12 value noise calculations per pixel.
 *
 * 2. GPU Load:
 *    Because of the nested noise calls, this is one of the more demanding 
 *    shaders. It performs many floating-point operations per frame.
 *
 * 3. Mitigation:
 *    - Running in Release mode is essential.
 *    - If FPS is too low, the fBm loop count (currently 6) can be reduced 
 *      to 4 or 5 for a significant speed boost with minor visual loss.
 */
@Composable
fun FractalCloudsView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        FractalCloudsContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun FractalCloudsContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "CloudsTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(FRACTAL_CLOUDS_SRC) }

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

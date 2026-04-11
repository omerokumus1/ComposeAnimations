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
 * AGSL Shader Source for Ocean Waves.
 * 
 * This shader simulates an ocean surface with multiple wave layers, 
 * sky/water reflection, foam, and caustics.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive wave and foam movement.
 */
private const val OCEAN_WAVES_SRC = """
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
        float t = iTime * 0.5;

        // Sum multiple sine waves with different frequencies and amplitudes
        float wave = 0.0;
        float amp = 0.4;
        float freq = 2.0;
        for (int i = 0; i < 6; i++) {
            float fi = float(i);
            wave += amp * sin(uv.x * freq + t * (1.0 + fi * 0.2) + fi * 0.8);
            wave += amp * 0.5 * sin(uv.x * freq * 1.3 - t * 0.7 + fi * 1.2);
            freq *= 1.6;
            amp *= 0.55;
        }

        // Define the surface line and underwater area
        float surface = smoothstep(0.02, 0.0, abs(uv.y - wave * 0.3));
        float underWater = smoothstep(wave * 0.3, wave * 0.3 - 1.5, uv.y);

        // Sky and water base colors
        float3 skyColor = mix(float3(0.1, 0.1, 0.2), float3(0.02, 0.02, 0.08), uv.y * 0.5 + 0.5);
        float3 deepColor = float3(0.0, 0.05, 0.15);
        float3 shallowColor = float3(0.0, 0.2, 0.4);
        float3 waterColor = mix(shallowColor, deepColor, clamp(-uv.y + wave * 0.3, 0.0, 1.0));

        // Composition of sky and water
        float3 col = mix(skyColor, waterColor, underWater);
        
        // Add bright surface line
        col += float3(0.5, 0.8, 0.9) * surface;

        // Add procedural foam effect near the surface
        float foam = valueNoise(float2(uv.x * 8.0 + t * 2.0, wave * 10.0)) * surface * 2.0;
        col += float3(foam) * 0.5;

        // Add caustics effect underwater
        float caustics = valueNoise(float2(uv.x * 6.0 + t, uv.y * 6.0 + t * 0.5));
        col += float3(0.0, 0.1, 0.15) * caustics * underWater * 0.4;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Ocean Waves:
 *
 * 1. Loop Complexity:
 *    6 iterations for wave summing plus 2 value noise calls. This is a 
 *    moderate load shader.
 *
 * 2. Visual Layering:
 *    The combination of 'smoothstep' for the surface, 'mix' for colors, 
 *    and 'valueNoise' for foam/caustics creates a rich look at a 
 *    reasonable computational cost.
 */
@Composable
fun OceanWavesView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        OceanWavesContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun OceanWavesContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "OceanTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(OCEAN_WAVES_SRC) }

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

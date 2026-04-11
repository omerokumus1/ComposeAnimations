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
 * AGSL Shader Source for Fire.
 * 
 * This shader uses layered noise (fBm) and a vertical gradient mask to simulate 
 * rising flames.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the noise movement.
 */
private const val FIRE_SRC = """
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
        float2 uv = fragCoord / iResolution.xy;
        // Invert Y for rising effect
        float2 nUV = float2(uv.x, 1.0 - uv.y);

        float2 p = nUV * float2(4.0, 4.0);
        p.y -= iTime * 3.0; // Rising speed

        float n = 0.0;
        float amp = 0.5;
        float2 q = p;
        
        // Fractional Brownian Motion (fBm) loop for noise detail
        for (int i = 0; i < 5; i++) {
            n += amp * valueNoise(q);
            q *= 2.0;
            amp *= 0.5;
        }

        // Mask to fade out the fire at the top and bottom
        float mask = pow(1.0 - nUV.y, 1.5);
        float fire = n * mask * 2.0;
        fire = clamp(fire, 0.0, 1.0);

        // Color mapping from dark red to bright yellow
        float3 col;
        if (fire < 0.33) {
            col = mix(float3(0.0), float3(0.8, 0.1, 0.0), fire / 0.33);
        } else if (fire < 0.66) {
            col = mix(float3(0.8, 0.1, 0.0), float3(1.0, 0.5, 0.0), (fire - 0.33) / 0.33);
        } else {
            col = mix(float3(1.0, 0.5, 0.0), float3(1.0, 1.0, 0.3), (fire - 0.66) / 0.34);
        }

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Fire:
 *
 * 1. Loop Complexity:
 *    5 iterations of value noise per pixel. Value noise is more expensive 
 *    than simple sine waves but provides the organic look required for fire.
 *
 * 2. Color Mapping:
 *    Uses conditional branching (if/else) for color interpolation. On modern 
 *    mobile GPUs, this is handled well, but using 'mix' and 'step' could 
 *    be a future optimization if needed.
 */
@Composable
fun FireView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        FireContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun FireContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "FireTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(FIRE_SRC) }

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

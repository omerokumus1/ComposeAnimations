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
 * AGSL Shader Source for Neon Pulse.
 * 
 * This is a high-complexity shader that combines four distinct visual elements:
 * 1. Expanding Neon Rings: Pulsing circles with hue cycling.
 * 2. Central Orb: A glowing, breathing core.
 * 3. Rotating Neon Rays: Beams of light that rotate around the center.
 * 4. Floating Neon Particles: Small, orbiting points of light.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive all animation components.
 */
private const val NEON_PULSE_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    float hashVal(float2 p) {
        return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
    }

    float3 hsv2rgb(float3 c) {
        float3 p = abs(fract(float3(c.x) + float3(0.0, 2.0/3.0, 1.0/3.0)) * 6.0 - 3.0);
        return c.z * mix(float3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
    }

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.6;
        float r = length(uv);
        float a = atan(uv.y, uv.x);

        float3 col = float3(0.02, 0.01, 0.04); // Deep dark background

        // 1. Expanding neon rings
        for (int i = 0; i < 6; i++) {
            float fi = float(i);
            float ringTime = fract(t * 0.25 + fi / 6.0);
            float ringR = ringTime * 2.0;
            float fade = (1.0 - ringTime);
            fade *= fade;

            float dist = abs(r - ringR);
            float core = smoothstep(0.015, 0.002, dist) * fade;
            float glow = 0.006 / (dist + 0.006) * fade;

            float hueShift = fi / 6.0 + t * 0.1;
            float3 neon = hsv2rgb(float3(fract(hueShift), 0.9, 1.0));
            col += mix(neon, float3(1.0), core * 0.6) * (core + glow * 0.4);
        }

        // 2. Central orb glow
        float orb = exp(-r * 5.0);
        float orbPulse = 0.7 + 0.3 * sin(t * 3.0);
        float3 orbColor = hsv2rgb(float3(fract(t * 0.08), 0.7, 1.0));
        col += orbColor * orb * orbPulse * 0.6;

        // 3. Rotating neon rays
        for (int j = 0; j < 4; j++) {
            float fj = float(j);
            float rayAngle = a + t * (0.5 + fj * 0.15) + fj * 1.57;
            float ray = pow(max(cos(rayAngle * 3.0), 0.0), 20.0);
            ray *= exp(-r * 2.5);
            float3 rayCol = hsv2rgb(float3(fract(fj * 0.25 + t * 0.05), 0.8, 1.0));
            col += rayCol * ray * 0.25;
        }

        // 4. Floating neon particles
        for (int k = 0; k < 8; k++) {
            float fk = float(k);
            float pAngle = fk * 0.785 + t * (0.3 + fk * 0.05);
            float pR = 0.4 + sin(t * 0.7 + fk * 1.2) * 0.3;
            float2 pPos = float2(cos(pAngle), sin(pAngle)) * pR;
            float pDist = length(uv - pPos);
            float particle = 0.003 / (pDist * pDist + 0.003);
            float3 pCol = hsv2rgb(float3(fract(fk / 8.0 + t * 0.12), 0.9, 1.0));
            col += pCol * particle * 0.15;
        }

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Neon Pulse:
 *
 * 1. Loop Complexity (High):
 *    Total of 18 loop iterations (6 rings + 4 rays + 8 particles) per pixel.
 *    This is a heavy shader that requires a modern GPU to maintain 60 FPS 
 *    at high resolutions.
 *
 * 2. Math Intensity:
 *    Uses multiple 'atan2', 'pow', 'exp', and 'hsv2rgb' conversions. 
 *    These are computationally expensive.
 *
 * 3. Mitigation:
 *    - Ensure Release mode for best performance.
 *    - On extremely high-DPI screens, the load may scale significantly.
 */
@Composable
fun NeonPulseView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        NeonPulseContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun NeonPulseContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeonTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(NEON_PULSE_SRC) }

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

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
 * AGSL Shader Source for Supernova.
 * 
 * This shader simulates an explosive stellar event using:
 * 1. Pulsing core glow.
 * 2. Expanding shockwave rings.
 * 3. Radial energy rays.
 * 4. Procedural particle debris using hashing.
 * 
 * Uniforms:
 * @param iResolution The dimensions of the drawing area.
 * @param iTime The elapsed time used to drive the explosion dynamics.
 */
private const val SUPERNOVA_SRC = """
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
        float r = length(uv);
        float a = atan(uv.y, uv.x);
        float t = iTime * 0.4;

        // 1. Pulsing core
        float coreSize = 0.08 + sin(t * 3.0) * 0.02;
        float core = coreSize / (r + coreSize);
        core = core * core;

        float3 col = float3(0.0);

        // 2. Shock wave rings expanding outward
        for (int i = 0; i < 4; i++) {
            float fi = float(i);
            float ringPhase = fract(t * 0.3 + fi * 0.25);
            float waveR = ringPhase * 2.5;
            float waveFade = exp(-ringPhase * 3.0);
            float wave = exp(-abs(r - waveR) * 20.0) * waveFade;
            float3 waveCol = hsv2rgb(float3(fract(fi * 0.15 + 0.05), 0.8, 1.0));
            col += waveCol * wave;
        }

        // 3. Radial energy rays
        float rays = 0.0;
        for (int j = 0; j < 12; j++) {
            float fj = float(j);
            float rayAngle = fj * 0.5236 + t * 0.2 + sin(t + fj) * 0.3;
            float ray = pow(max(cos((a - rayAngle) * 6.0), 0.0), 40.0);
            ray *= exp(-r * 1.5) * (0.5 + 0.5 * sin(t * 2.0 + fj));
            rays += ray;
        }
        col += float3(1.0, 0.6, 0.2) * rays * 0.4;

        // 4. Hot white core with orange/yellow gradient
        float3 coreCol = mix(float3(1.0, 0.4, 0.1), float3(1.0, 1.0, 0.9), smoothstep(0.3, 0.0, r));
        col += coreCol * core;

        // 5. Particle debris
        for (int k = 0; k < 20; k++) {
            float fk = float(k);
            float pAngle = hashVal(float2(fk, 0.0)) * 6.283;
            float pSpeed = 0.3 + hashVal(float2(fk, 1.0)) * 0.7;
            float pR = fract(t * pSpeed * 0.2 + hashVal(float2(fk, 2.0))) * 1.8;
            float2 pPos = float2(cos(pAngle + t * 0.1), sin(pAngle + t * 0.1)) * pR;
            float pDist = length(uv - pPos);
            float p = 0.002 / (pDist * pDist + 0.002);
            float pFade = exp(-pR * 2.0);
            col += float3(1.0, 0.8, 0.4) * p * pFade * 0.08;
        }

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Supernova:
 *
 * 1. Loop Complexity (High):
 *    Total of 36 loop iterations (4 rings + 12 rays + 20 particles) per pixel.
 *    This is a heavy shader that benefits significantly from hardware 
 *    acceleration and Release mode optimizations.
 *
 * 2. Mathematical Load:
 *    Heavy use of 'exp', 'pow', 'cos', and 'atan' within loops. 
 *    Maintaining 60 FPS on lower-end devices may require reducing particle 
 *    or ray counts.
 */
@Composable
fun SupernovaView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        SupernovaContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun SupernovaContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "SupernovaTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(SUPERNOVA_SRC) }

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

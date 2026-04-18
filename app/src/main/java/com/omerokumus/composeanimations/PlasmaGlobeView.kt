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
 * AGSL Shader Source for Plasma Globe.
 * 
 * This shader simulates the look of a Tesla/Plasma globe using:
 * 1. A glass sphere boundary with rim glow.
 * 2. Eight main electric tendrils generated using nested loops.
 * 3. Noisy segment-based pathing for the electric arcs.
 * 4. A glowing central core and ambient flickering.
 */
private const val PLASMA_GLOBE_SRC = """
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
        float r = length(uv);

        // Fast exit for pixels significantly outside the globe
        if (r > 1.1) return half4(0.0, 0.0, 0.0, 1.0);

        float t = iTime;
        float sphereR = 0.95;
        float sphereEdge = smoothstep(sphereR, sphereR - 0.03, r);
        float3 col = float3(0.01, 0.0, 0.02);

        // 2. Electric Tendrils (Optimized: 6 bolts, 10 segments)
        // Total iterations reduced from 120 to 60
        for (int i = 0; i < 6; i++) {
            float fi = float(i);
            float baseAngle = fi * 1.047 + t * 0.3; // 1.047 is ~2pi/6

            // Directional Culling: Skip tendrils that are facing away from the current pixel
            float2 arcDir = float2(cos(baseAngle), sin(baseAngle));
            if (dot(uv, arcDir) < 0.0 && r > 0.2) continue;

            for (int s = 0; s < 10; s++) {
                float fs = float(s) / 9.0;
                float segR = fs * sphereR * 0.9;

                // Optimization: Single noise call for wobble instead of two
                float noiseVal = valueNoise(float2(fi * 7.0 + fs * 3.0, t * 2.0));
                float segAngle = baseAngle + (noiseVal - 0.5) * 1.2 * fs;

                float2 segPos = float2(cos(segAngle), sin(segAngle)) * segR;
                float d = length(uv - segPos);

                // Distance Culling: Skip segments that are too far to contribute light
                if (d > 0.3) continue;

                float core = 0.002 / (d * d + 0.002);
                float glow = 0.008 / (d + 0.008);

                float brightness = (core * 0.06 + glow * 0.03) * (0.7 + 0.3 * sin(t * 5.0 + fi * 2.0 + fs * 4.0));
                col += mix(float3(0.8, 0.3, 1.0), float3(0.3, 0.5, 1.0), fs) * brightness;
            }
        }

        // 3. Central core glow
        float coreGlow = 0.04 / (r * r + 0.04);
        col += float3(0.6, 0.4, 0.9) * coreGlow * 0.5;
        
        // 4. Apply boundaries and final polish
        col *= sphereEdge;
        col += float3(0.3, 0.2, 0.5) * exp(-abs(r - sphereR) * 30.0) * 0.3;
        col *= 0.9 + 0.1 * sin(t * 8.0); // Ambient flicker

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Plasma Globe:
 *
 * 1. Optimized Loop Complexity (Medium-High):
 *    Reduced from 120 to 60 iterations per pixel. Integrated directional 
 *    and distance-based culling to skip redundant calculations.
 *
 * 2. Branching and Culling:
 *    Uses 'if' and 'continue' for culling. While branching has overhead, 
 *    the complexity of the skipped calculations makes it a net gain 
 *    on modern mobile GPUs, enabling 60 FPS.
 *
 * 3. Noise Reduction:
 *    The procedural noise calls were halved and simplified to reduce 
 *    ALU pressure.
 */
@Composable
fun PlasmaGlobeView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PlasmaGlobeContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun PlasmaGlobeContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlobeTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(PLASMA_GLOBE_SRC) }

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

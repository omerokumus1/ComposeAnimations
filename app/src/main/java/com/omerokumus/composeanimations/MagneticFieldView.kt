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
 * AGSL Shader Source for Magnetic Field.
 * 
 * This shader simulates field lines between two magnetic poles using:
 * 1. Superposition of two radial fields.
 * 2. Polar-coordinate based field line patterns.
 * 3. Color blending between poles (Cyan to Magenta).
 * 4. Animated particles flowing along the field lines.
 */
private const val MAGNETIC_FIELD_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    float hashVal(float2 p) {
        return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
    }

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.3;

        float2 pole1 = float2(-0.5, 0.0);
        float2 pole2 = float2(0.5, 0.0);

        // 1. Field direction from superposition of two poles
        float2 d1 = uv - pole1;
        float2 d2 = uv - pole2;
        float r1 = length(d1) + 0.001;
        float r2 = length(d2) + 0.001;
        float2 field = d1 / (r1 * r1) - d2 / (r2 * r2);

        // 2. Field line pattern using atan of field direction
        // Note: AGSL uses atan(y, x) for two-argument version
        float angle = atan(field.y, field.x);
        float lines = sin(angle * 8.0 + t * 2.0);
        lines = pow(abs(lines), 0.3);

        // 3. Field strength for brightness scaling
        float strength = length(field);
        strength = clamp(strength * 0.3, 0.0, 1.0);

        // 4. Color: cyan near pole1, magenta near pole2
        float blend = clamp((uv.x + 0.5) * 1.0, 0.0, 1.0);
        float3 cyan = float3(0.0, 0.8, 1.0);
        float3 magenta = float3(1.0, 0.2, 0.8);
        float3 col = mix(cyan, magenta, blend) * lines * strength;

        // 5. Pole glow
        col += cyan * 0.3 / (r1 * 5.0 + 0.3);
        col += magenta * 0.3 / (r2 * 5.0 + 0.3);

        // 6. Animated particles along field lines
        float flowPhase = fract(angle * 1.27 + t + hashVal(floor(float2(angle * 8.0, 0.0))) * 5.0);
        float particle = smoothstep(0.02, 0.0, abs(flowPhase - 0.5)) * strength;
        col += float3(1.0) * particle * 0.4;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Magnetic Field:
 *
 * 1. Mathematical Intensity:
 *    Uses 'atan', 'sin', 'pow', and 'length'. These are standard shader 
 *    operations and run efficiently on most Android 13+ devices.
 *
 * 2. Particle Logic:
 *    The particles are procedural (calculated per pixel), which is much 
 *    more memory-efficient than a traditional particle system.
 */
@Composable
fun MagneticFieldView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        MagneticFieldContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun MagneticFieldContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "MagneticTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(MAGNETIC_FIELD_SRC) }

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

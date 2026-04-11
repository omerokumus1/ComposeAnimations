package com.omerokumus.composeanimations

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AGSL Shader Source for Anomalous Matter.
 * 
 * This is an extremely complex shader that uses Raymarching to render a 
 * 3D displaced sphere with a wireframe texture and edge glows.
 */
private const val ANOMALOUS_MATTER_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    float hash3to1(float3 p) {
        return fract(sin(dot(p, float3(127.1, 311.7, 74.7))) * 43758.5453);
    }

    float snoise3D(float3 v) {
        float3 i = floor(v + (v.x + v.y + v.z) / 3.0);
        float3 x0 = v - i + (i.x + i.y + i.z) / 6.0;
        float3 g = step(x0.yzx, x0.xyz);
        float3 l = 1.0 - g;
        float3 i1 = min(g, l.zxy);
        float3 i2 = max(g, l.zxy);
        float3 x1 = x0 - i1 + 1.0 / 6.0;
        float3 x2 = x0 - i2 + 1.0 / 3.0;
        float3 x3 = x0 - 0.5;
        float4 m = max(0.6 - float4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
        m = m * m * m * m;
        float4 px = float4(
            hash3to1(i),
            hash3to1(i + i1),
            hash3to1(i + i2),
            hash3to1(i + 1.0)
        );
        return dot(m, px) * 4.0 - 1.0;
    }

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord * 2.0 - iResolution.xy) / min(iResolution.x, iResolution.y);
        float t = iTime * 0.25;

        // Camera
        float3 ro = float3(0.0, 0.0, 3.2);
        float3 rd = normalize(float3(uv, -1.5));

        // Base sphere radius
        float sphereR = 1.3;
        float3 col = float3(0.0);

        // Optimization: Reduce raymarch steps from 64 to 32
        float tRay = 0.0;
        bool hit = false;
        float3 hitPos;

        for (int i = 0; i < 32; i++) {
            float3 p = ro + rd * tRay;
            float baseR = length(p);
            float3 dir = normalize(p);

            // Optimization: Reduce noise octaves from 3 to 2
            float disp = snoise3D(dir * 2.5 + float3(t * 0.4, t * 0.25, t * 0.55)) * 0.25;
            disp += snoise3D(dir * 5.0 + float3(-t * 0.3, t * 0.5, t * 0.2)) * 0.12;

            float surfaceR = sphereR + disp;
            float d = baseR - surfaceR;

            if (d < 0.01) { // Increased tolerance for faster convergence
                hit = true;
                hitPos = p;
                break;
            }

            tRay += d; // Faster stepping
            if (tRay > 5.0) break;
        }

        if (hit) {
            float3 n = normalize(hitPos);
            float3 dir = n;

            // Optimization: Simplify noise warp
            float noiseWarp = snoise3D(dir * 3.0 + float3(t * 0.3)) * 0.4;
            float theta = atan(dir.z, dir.x) + noiseWarp;
            float phi = acos(clamp(dir.y, -1.0, 1.0)) + noiseWarp * 0.6;

            // Dense wireframe grid
            float gridFreq = 24.0;
            float lineTheta = abs(fract(theta * 3.8197) - 0.5) * 2.0; // pre-multiplied 24/2pi
            float linePhi = abs(fract(phi * 7.6394) - 0.5) * 2.0;   // pre-multiplied 24/pi
            float lineDiag = abs(fract((theta + phi) * 3.8197) - 0.5) * 2.0;

            float wire = min(min(lineTheta, linePhi), lineDiag);
            float wireEdge = 1.0 - smoothstep(0.03, 0.12, wire);

            // Fresnel for edge glow
            float3 viewDir = normalize(ro - hitPos);
            float fresnel = 1.0 - max(dot(n, viewDir), 0.0);
            fresnel = fresnel * fresnel;

            // Compose
            float3 surfaceCol = float3(0.75, 0.78, 0.82) * wireEdge * (max(dot(n, float3(0.44, 0.89, 0.89)), 0.0) * 0.5 + 0.5);
            surfaceCol += float3(0.5, 0.55, 0.6) * fresnel * 0.6;
            surfaceCol *= 0.3 + smoothstep(0.0, 0.5, dot(n, viewDir)) * 0.7;

            col = surfaceCol;
        }

        // Soft atmospheric glow
        col += float3(0.4, 0.42, 0.45) * exp(-length(uv) * 2.0) * 0.06;

        return half4(col, 1.0);
    }
"""

/**
 * Performance Considerations for Anomalous Matter:
 * 
 * This is a high-complexity raymarching shader. Several optimizations were applied
 * to achieve 60 FPS on mobile hardware:
 * 
 * 1. Raymarching Steps:
 *    Reduced from 64 to 32 steps. This halves the primary loop workload while
 *    maintaining enough detail for the displaced sphere shape.
 * 
 * 2. Noise Octaves:
 *    Reduced surface displacement noise from 3 octaves to 2. This significantly
 *    lowers the number of 'snoise3D' calls per raymarching step.
 * 
 * 3. Tolerance and Stepping:
 *    Increased distance tolerance (d < 0.01) and used raw 'd' for stepping
 *    to speed up convergence on the surface.
 * 
 * 4. Pre-calculated Constants:
 *    In the wireframe logic, divisions by PI and 2*PI were replaced with 
 *    pre-multiplied constants (e.g., 24/2pi = 3.8197) to reduce ALU overhead.
 * 
 * 5. Normal Calculation:
 *    Removed expensive central difference normal calculations. Instead, the
 *    normalized hit position is used as a simplified normal, which is
 *    sufficient for this spherical effect.
 */
@Composable
fun AnomalousMatterView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AnomalousMatterContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+", color = Color.White)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AnomalousMatterContent(modifier: Modifier = Modifier) {
    var appeared by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 300, easing = EaseOut),
        label = "Alpha"
    )
    
    val offsetY by animateDpAsState(
        targetValue = if (appeared) 0.dp else 20.dp,
        animationSpec = tween(1000, delayMillis = 300, easing = EaseOut),
        label = "OffsetY"
    )

    LaunchedEffect(Unit) {
        appeared = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "AnomalousTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(ANOMALOUS_MATTER_SRC) }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // 1. Background Shader
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-50).dp)
                .drawWithCache {
                    shader.setFloatUniform("iResolution", size.width, size.height)
                    shader.setFloatUniform("iTime", time)
                    val brush = ShaderBrush(shader)
                    onDrawBehind {
                        drawRect(brush)
                    }
                }
        )

        // 2. Content Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Column(
                modifier = Modifier
                    .alpha(alpha)
                    .offset(y = offsetY),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "LAUNCH SEQUENCE: ANOMALY 12",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp
                    ),
                    color = Color.White.copy(alpha = 0.6f)
                )

                Text(
                    text = "Energy dances along\nunseen frontiers.",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 44.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "This demo shows how to override the default copy and integrate hero into a page layout.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
        
        // FPS Counter
        FpsCounter(Modifier.align(Alignment.TopEnd).padding(16.dp).padding(top = 32.dp))
    }
}

package com.omerokumus.composeanimations

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AGSL Shader Source for Shader Hero.
 * 
 * This shader creates a warm, glowing particle/cloud effect.
 * Ported from the Metal version which was inspired by GLSL cloud simulations.
 */
private const val SHADER_HERO_SRC = """
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

    float fbmHero(float2 p) {
        float t2 = 0.0;
        float a2 = 1.0;
        // Simplified rotation logic for AGSL
        for (int i = 0; i < 5; i++) {
            t2 += a2 * valueNoise(p);
            p = p * 2.0;
            a2 *= 0.5;
        }
        return t2;
    }

    float heroCloudsFn(float2 p) {
        float d = 1.0;
        float t2 = 0.0;
        for (float i = 0.0; i < 3.0; i += 1.0) {
            float a = d * fbmHero(i * 10.0 + p.x * 0.2 + 0.2 * (1.0 + i) * p.y + d + i * i + p);
            t2 = mix(t2, d, a);
            d = a;
            p *= 2.0 / (i + 1.0);
        }
        return t2;
    }

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord - 0.5 * iResolution.xy) / min(iResolution.x, iResolution.y);
        float2 st = uv * float2(2.0, 1.0);

        float3 col = float3(0.0);
        float bg = heroCloudsFn(float2(st.x + iTime * 0.5, -st.y));

        uv *= 1.0 - 0.3 * (sin(iTime * 0.2) * 0.5 + 0.5);

        for (float i = 1.0; i < 12.0; i += 1.0) {
            uv += 0.1 * cos(i * float2(0.1 + 0.01 * i, 0.8) + i * i + iTime * 0.5 + 0.1 * uv.x);
            float2 p = uv;
            float d = length(p);
            col += 0.00125 / d * (cos(sin(i) * float3(1.0, 2.0, 3.0)) + 1.0);
            float b = valueNoise(float2(i + p.x + bg * 1.731, i + p.y + bg * 1.731));
            col += 0.002 * b / length(max(p, float2(b * p.x * 0.02, p.y)));
            col = mix(col, float3(bg * 0.25, bg * 0.137, bg * 0.05), d);
        }

        return half4(col, 1.0);
    }
"""

@Composable
fun ShaderHeroView(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ShaderHeroContent(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Shader animations require Android 13+", color = Color.White)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun ShaderHeroContent(modifier: Modifier = Modifier) {
    var appeared by remember { mutableStateOf(false) }
    
    // Entrance animations
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

    val infiniteTransition = rememberInfiniteTransition(label = "HeroTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val shader = remember { RuntimeShader(SHADER_HERO_SRC) }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Background Shader
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(96.dp))

            // Badge
            Row(
                modifier = Modifier
                    .offset(y = -offsetY)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("✨", fontSize = 14.sp)
                Text(
                    "Trusted by forward-thinking teams.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFA500).copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Headline
            Text(
                text = "Launch Your Workflow Into Orbit",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFA500), Color.Yellow, Color(0xFFFFA500).copy(alpha = 0.8f))
                    )
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = offsetY)
                    .alpha(alpha)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Subtitle
            Text(
                text = "Supercharge productivity with AI-powered automation and integrations built for the next generation of teams.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color(0xFFFFA500).copy(alpha = 0.85f),
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .offset(y = offsetY)
                    .alpha(alpha)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // CTA Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY)
                    .alpha(alpha),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFFFFA500), Color.Yellow)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Get Started for Free", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFFFFA500).copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Text(
                        "Explore Features",
                        color = Color(0xFFFFA500).copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // FPS Counter
        FpsCounter(Modifier.align(Alignment.TopEnd).padding(16.dp).padding(top = 32.dp))
    }
}

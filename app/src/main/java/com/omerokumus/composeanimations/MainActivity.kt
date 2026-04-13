package com.omerokumus.composeanimations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.omerokumus.composeanimations.ui.theme.ComposeAnimationsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeAnimationsTheme {
                val backstack = rememberNavBackStack(Screen.Home)

                BackHandler(enabled = backstack.size > 1) {
                    backstack.removeAt(backstack.lastIndex)
                }

                NavDisplay(
                    backStack = backstack,
                    onBack = { backstack.removeAt(backstack.lastIndex) },
                    entryProvider = entryProvider<NavKey> {
                        entry<Screen.Home> {
                            HomeScreen(
                                onNavigateToAnimation = { animation ->
                                    backstack.add(Screen.Animation(animation))
                                },
                                onNavigateToStyleTwo = {
                                    backstack.add(Screen.StyleTwo)
                                }
                            )
                        }
                        entry<Screen.StyleTwo> {
                            HomeScreenTwo(
                                onNavigateToAnimation = { animation ->
                                    backstack.add(Screen.AnimationTwo(animation))
                                },
                                onBack = { backstack.removeAt(backstack.lastIndex) }
                            )
                        }
                        entry<Screen.Animation> { key ->
                            when (key.type) {
                                AnimationScreen.SHADER_ANIMATION -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        ShaderAnimationView()
                                        Text(
                                            text = "Shader Animation",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.PLASMA_WAVES -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        PlasmaWavesView()
                                        Text(
                                            text = "Plasma Waves",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.STARFIELD -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        StarfieldView()
                                        Text(
                                            text = "Starfield",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.WATER_RIPPLES -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        WaterRipplesView()
                                        Text(
                                            text = "Water Ripples",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.FIRE -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        FireView()
                                        Text(
                                            text = "Fire",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.OCEAN_WAVES -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        OceanWavesView()
                                        Text(
                                            text = "Ocean Waves",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.MATRIX_RAIN -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        MatrixRainView()
                                        Text(
                                            text = "Matrix Rain",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.NEON_PULSE -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        NeonPulseView()
                                        Text(
                                            text = "Neon Pulse",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.TUNNEL -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        TunnelView()
                                        Text(
                                            text = "Tunnel",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.FRACTAL_CLOUDS -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        FractalCloudsView()
                                        Text(
                                            text = "Fractal Clouds",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.NEBULA -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        NebulaView()
                                        Text(
                                            text = "Nebula",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                AnimationScreen.SHADER_HERO -> {
                                    ShaderHeroView()
                                }
                                AnimationScreen.ANOMALOUS_MATTER -> {
                                    AnomalousMatterView()
                                }
                                else -> {
                                    AnimationPlaceholderScreen(key.type.label) {
                                        backstack.removeAt(backstack.lastIndex)
                                    }
                                }
                            }
                        }


                        entry<Screen.AnimationTwo> { key ->
                            when (key.type) {
                                AnimationScreenTwo.DIAMOND_RINGS -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        DiamondRingsView()
                                        Text(
                                            text = "Diamond Rings",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                letterSpacing = (-2).sp
                                            ),
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                                else -> {
                                    AnimationPlaceholderScreen(key.type.label) {
                                        backstack.removeAt(backstack.lastIndex)
                                    }
                                }
                            }
                        }
                    }
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationPlaceholderScreen(name: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Animation $name will be here.")
        }
    }
}
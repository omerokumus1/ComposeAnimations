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
                            AnimationPlaceholderScreen(key.type.label) {
                                backstack.removeAt(backstack.lastIndex)
                            }
                        }
                        entry<Screen.AnimationTwo> { key ->
                            AnimationPlaceholderScreen(key.type.label) {
                                backstack.removeAt(backstack.lastIndex)
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
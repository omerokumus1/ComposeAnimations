package com.omerokumus.composeanimations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAnimation: (AnimationScreen) -> Unit,
    onNavigateToStyleTwo: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animations") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                SectionHeader("Style One")
            }
            items(AnimationScreen.entries) { screen ->
                ListItem(
                    headlineContent = { Text(screen.label) },
                    modifier = Modifier.clickable { onNavigateToAnimation(screen) }
                )
            }
            item {
                SectionHeader("Style Two")
            }
            item {
                ListItem(
                    headlineContent = { Text("Style Two Collection") },
                    modifier = Modifier.clickable { onNavigateToStyleTwo() }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

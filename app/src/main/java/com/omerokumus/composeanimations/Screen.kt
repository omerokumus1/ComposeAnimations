package com.omerokumus.composeanimations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object Home : Screen
    @Serializable
    data object StyleTwo : Screen
    @Serializable
    data class Animation(val type: AnimationScreen) : Screen
    @Serializable
    data class AnimationTwo(val type: AnimationScreenTwo) : Screen
}

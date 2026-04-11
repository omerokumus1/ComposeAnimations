package com.omerokumus.composeanimations

import kotlinx.serialization.Serializable

@Serializable
enum class AnimationScreen(val label: String) {
    SHADER_ANIMATION("Shader Animation"),
    PLASMA_WAVES("Plasma Waves"),
    STARFIELD("Starfield"),
    WATER_RIPPLES("Water Ripples"),
    FIRE("Fire"),
    OCEAN_WAVES("Ocean Waves"),
    MATRIX_RAIN("Matrix Rain"),
    NEON_PULSE("Neon Pulse"),
    TUNNEL("Tunnel"),
    FRACTAL_CLOUDS("Fractal Clouds"),
    NEBULA("Nebula"),
    SHADER_HERO("Shader Hero"),
    ANOMALOUS_MATTER("Anomalous Matter")
}

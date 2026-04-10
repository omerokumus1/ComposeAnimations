//
//  AnimationScreen.swift
//  webAnimation
//
//  Created by Youssef on 2026-03-26.
//

import SwiftUI

enum AnimationScreen: String, CaseIterable, Identifiable {
    case shaderAnimation = "Shader Animation"
    case plasmaWaves = "Plasma Waves"
    case starfield = "Starfield"
    case waterRipples = "Water Ripples"
    case fire = "Fire"
    case oceanWaves = "Ocean Waves"
    case matrixRain = "Matrix Rain"
    case neonPulse = "Neon Pulse"
    case tunnel = "Tunnel"
    case fractalClouds = "Fractal Clouds"
    case nebula = "Nebula"
    case shaderHero = "Shader Hero"
    case anomalousMatter = "Anomalous Matter"

    var id: String { rawValue }

    @ViewBuilder
    var destination: some View {
        switch self {
        case .shaderAnimation:
            ContentView()
        case .plasmaWaves:
            PlasmaWavesView()
        case .starfield:
            StarfieldView()
        case .waterRipples:
            WaterRipplesView()
        case .fire:
            FireView()
        case .oceanWaves:
            OceanWavesView()
        case .matrixRain:
            MatrixRainView()
        case .neonPulse:
            NeonPulseView()
        case .tunnel:
            TunnelView()
        case .fractalClouds:
            FractalCloudsView()
        case .nebula:
            NebulaView()
        case .shaderHero:
            ShaderHeroView()
        case .anomalousMatter:
            AnomalousMatterView()
        }
    }
}

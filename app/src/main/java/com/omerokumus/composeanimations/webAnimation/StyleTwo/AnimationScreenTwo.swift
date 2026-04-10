//
//  AnimationScreenTwo.swift
//  webAnimation
//

import SwiftUI

enum AnimationScreenTwo: String, CaseIterable, Identifiable {
    case diamondRings = "Diamond Rings"
    case liquidChrome = "Liquid Chrome"
    case hologram = "Hologram"
    case supernova = "Supernova"
    case warpSpeed = "Warp Speed"
    case magneticField = "Magnetic Field"
    case glitchArt = "Glitch Art"
    case inkSmoke = "Ink Smoke"
    case neonOrbit = "Neon Orbit"
    case plasmaGlobe = "Plasma Globe"

    var id: String { rawValue }

    @ViewBuilder
    var destination: some View {
        switch self {
        case .diamondRings:
            DiamondRingsView()
        case .liquidChrome:
            LiquidChromeView()
        case .hologram:
            HologramView()
        case .supernova:
            SupernovaView()
        case .warpSpeed:
            WarpSpeedView()
        case .magneticField:
            MagneticFieldView()
        case .glitchArt:
            GlitchArtView()
        case .inkSmoke:
            InkSmokeView()
        case .neonOrbit:
            NeonOrbitView()
        case .plasmaGlobe:
            PlasmaGlobeView()
        }
    }
}

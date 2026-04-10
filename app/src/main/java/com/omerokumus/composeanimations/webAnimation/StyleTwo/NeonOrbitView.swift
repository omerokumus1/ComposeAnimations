//
//  NeonOrbitView.swift
//  webAnimation
//

import SwiftUI

struct NeonOrbitView: View {
    private let startDate = Date()

    var body: some View {
        ZStack {
            TimelineView(.animation) { timeline in
                let elapsed = timeline.date.timeIntervalSince(startDate)
                GeometryReader { geo in
                    Rectangle()
                        .fill(.black)
                        .colorEffect(
                            ShaderLibrary.neonOrbit(
                                .float2(geo.size.width, geo.size.height),
                                .float(Float(elapsed))
                            )
                        )
                }
            }
            .ignoresSafeArea()

            Text("Neon Orbit")
                .font(.system(size: 56, weight: .semibold))
                .tracking(-2)
                .foregroundStyle(.white)
                .allowsHitTesting(false)
        }
    }
}

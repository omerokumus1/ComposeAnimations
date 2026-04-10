//
//  FractalCloudsView.swift
//  webAnimation
//
//  Created by Youssef on 2026-03-26.
//

import SwiftUI

struct FractalCloudsView: View {
    private let startDate = Date()

    var body: some View {
        ZStack {
            TimelineView(.animation) { timeline in
                let elapsed = timeline.date.timeIntervalSince(startDate)
                GeometryReader { geo in
                    Rectangle()
                        .fill(.black)
                        .colorEffect(
                            ShaderLibrary.fractalClouds(
                                .float2(geo.size.width, geo.size.height),
                                .float(Float(elapsed))
                            )
                        )
                }
            }
            .ignoresSafeArea()

            Text("Fractal Clouds")
                .font(.system(size: 56, weight: .semibold))
                .tracking(-2)
                .foregroundStyle(.white)
                .allowsHitTesting(false)
        }
    }
}

#Preview {
    FractalCloudsView()
}

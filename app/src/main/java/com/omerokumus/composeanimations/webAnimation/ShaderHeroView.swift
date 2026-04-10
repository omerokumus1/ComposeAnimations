//
//  ShaderHeroView.swift
//  webAnimation
//

import SwiftUI

struct ShaderHeroView: View {
    private let startDate = Date()
    @State private var appeared = false

    var body: some View {
        ZStack {
 
            TimelineView(.animation) { timeline in
                let elapsed = timeline.date.timeIntervalSince(startDate)
                GeometryReader { geo in
                    Rectangle()
                        .fill(.black)
                        .colorEffect(
                            ShaderLibrary.shaderHero(
                                .float2(geo.size.width, geo.size.height),
                                .float(Float(elapsed))
                            )
                        )
                }
            }
            .ignoresSafeArea()
 
            VStack(alignment: .leading,spacing: 24) {
 
                HStack(spacing: 6) {
                    Text("✨")
                    Text("Trusted by forward-thinking teams.")
                        .font(.subheadline)
                        .foregroundStyle(.orange.opacity(0.9))
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .background(.orange.opacity(0.1))
                .background(.ultraThinMaterial)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(.orange.opacity(0.3), lineWidth: 1))
                .opacity(appeared ? 1 : 0)
                .offset(y: appeared ? 0 : -20)

                // Headlines
                VStack(alignment: .leading,spacing:4) {
                    Text("Launch Your Workflow Into Orbit")
                        .font(.system(size: 42, weight: .bold))
                        .multilineTextAlignment(.leading)
                        .foregroundStyle(
                            LinearGradient(
                                colors: [.orange, .yellow, .orange.opacity(0.8)],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .opacity(appeared ? 1 : 0)
                        .offset(y: appeared ? 0 : 30)
 
                }
                .multilineTextAlignment(.center)
                Spacer()
                // Subtitle
                Text("Supercharge productivity with AI-powered automation and integrations built for the next generation of teams.")
                    .font(.footnote)
                    .fontWeight(.light)
                    .foregroundStyle(.orange.opacity(0.85))
                    .multilineTextAlignment(.center)
//                    .padding(.horizontal, 32)
                    .opacity(appeared ? 1 : 0)
                    .offset(y: appeared ? 0 : 30)
               
                // CTA Buttons
                VStack(spacing: 14) {
                    Button(action: {}) {
                        Text("Get Started for Free")
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .foregroundStyle(.black)
                            .padding(.horizontal, 32)
                            .padding(.vertical, 16)
                            .background(
                                LinearGradient(
                                    colors: [.orange, .yellow],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .clipShape(Capsule())
                    }

                    Button(action: {}) {
                        Text("Explore Features")
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .foregroundStyle(.orange.opacity(0.9))
                            .padding(.horizontal, 32)
                            .padding(.vertical, 16)
                            .background(.orange.opacity(0.1))
                            .background(.ultraThinMaterial)
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(.orange.opacity(0.3), lineWidth: 1))
                    }
                }
                .opacity(appeared ? 1 : 0)
                .offset(y: appeared ? 0 : 30)
//                Sp/*acer()*/
            }
            .padding()
        }
        .onAppear {
            withAnimation(.easeOut(duration: 1.0)) {
                appeared = true
            }
        }
    }
}

#Preview {
    ShaderHeroView()
}

 

import SwiftUI

struct AnomalousMatterView: View {
    private let startDate = Date()
    @State private var appeared = false

    var body: some View {
        ZStack {
            // Shader background
            TimelineView(.animation) { timeline in
                let elapsed = timeline.date.timeIntervalSince(startDate)
                GeometryReader { geo in
                    Rectangle()
                        .fill(.black)
                        .colorEffect(
                            ShaderLibrary.anomalousMatter(
                                .float2(geo.size.width, geo.size.height),
                                .float(Float(elapsed))
                            )
                        )
                }
                .offset(y: -50)
            }
            .ignoresSafeArea()

            // Gradient overlay from bottom
//            VStack {
//                Spacer()
//                LinearGradient(
//                    colors: [.clear, .black.opacity(0.6), .black.opacity(0.9), .black],
//                    startPoint: .top,
//                    endPoint: .bottom
//                )
//                .frame(height: 450)
//              
//            }
//            .ignoresSafeArea()

            // Hero text centered in lower portion
            VStack {
                Spacer()

                VStack(spacing: 16) {
                    Text("LAUNCH SEQUENCE: ANOMALY 12")
                        .font(.system(size: 11, weight: .medium, design: .monospaced))
                        .tracking(4)
                        .foregroundStyle(.white.opacity(0.6))

                    Text("Energy dances along\nunseen frontiers.")
                        .font(.system(size: 38, weight: .bold))
                        .foregroundStyle(.white)
                        .multilineTextAlignment(.center)

                    Text("This demo shows how to override the default copy and integrate hero into a page layout.")
                        .font(.system(size: 15, weight: .regular))
                        .foregroundStyle(.white.opacity(0.5))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
                .opacity(appeared ? 1 : 0)
                .offset(y: appeared ? 0 : 20)
                .padding(.bottom, 100)
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 1.0).delay(0.3)) {
                appeared = true
            }
        }
    }
}

#Preview {
    AnomalousMatterView()
}

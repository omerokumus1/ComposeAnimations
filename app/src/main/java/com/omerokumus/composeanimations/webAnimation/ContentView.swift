 

import SwiftUI

struct ContentView: View {
    var body: some View {
        ZStack {
            ShaderAnimationView()

            Text("Shader Animation")
                .font(.system(size: 56, weight: .semibold))
                .tracking(-2)
                .foregroundStyle(.white)
                .allowsHitTesting(false)
        }
    }
}

#Preview {
    ContentView()
}

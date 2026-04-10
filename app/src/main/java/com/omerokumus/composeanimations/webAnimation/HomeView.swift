 

import SwiftUI

struct HomeView: View {
    var body: some View {
        NavigationStack {
            List {
                Section("Style One") {
                    ForEach(AnimationScreen.allCases) { screen in
                        NavigationLink(value: screen) {
                            Text(screen.rawValue)
                                .font(.headline)
                        }
                    }
                }

                Section("Style Two") {
                    NavigationLink("Style Two Collection", value: "styleTwo")
                }
            }
            .navigationTitle("Animations")
            .navigationDestination(for: AnimationScreen.self) { screen in
                screen.destination
                    .navigationBarTitleDisplayMode(.inline)
            }
            .navigationDestination(for: String.self) { value in
                if value == "styleTwo" {
                    HomeViewTwo()
                }
            }
        }
    }
}

#Preview {
    HomeView()
}
//new line

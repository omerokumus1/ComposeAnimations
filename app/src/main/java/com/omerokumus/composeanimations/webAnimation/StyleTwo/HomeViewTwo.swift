//
//  HomeViewTwo.swift
//  webAnimation
//

import SwiftUI

struct HomeViewTwo: View {
    var body: some View {
        List(AnimationScreenTwo.allCases) { screen in
            NavigationLink(value: screen) {
                Text(screen.rawValue)
                    .font(.headline)
            }
        }
        .navigationTitle("Style Two")
        .navigationDestination(for: AnimationScreenTwo.self) { screen in
            screen.destination
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}

#Preview {
    NavigationStack {
        HomeViewTwo()
    }
}

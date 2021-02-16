//
//  Gradients.swift
//  Well
//
//  Created by Philip Dukhov on 2/14/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct GradientView: View {
    let gradient: SharedMobile.Gradient
    
    var body: some View {
        gradient.backgroundColor.toColor()
            .overlay(LinearGradient(
                gradient: SwiftUI.Gradient(
                    stops: gradient.stops.map {
                        SwiftUI.Gradient.Stop(
                            color: $0.color.toColor(),
                            location: $0.location.toCGFloat()
                        )
                    }
                ),
                startPoint: gradient.startPoint.toUnitPoint(),
                endPoint: gradient.endPoint.toUnitPoint()
            ).opacity(gradient.overlayOpacity.toDouble()))
    }
}

extension SharedMobile.Gradient {
    static let main = SharedMobile.Gradient.Companion().Main
    static let callBackground = SharedMobile.Gradient.Companion().CallBackground
    static let callBottomBar = SharedMobile.Gradient.Companion().CallBottomBar
}

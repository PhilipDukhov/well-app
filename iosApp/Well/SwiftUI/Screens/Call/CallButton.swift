//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI

struct CallUpButton: View {
    let handler: () -> Void

    var body: some View {
        CallButton(
            systemImageName: "phone.fill",
            backgroundColor: Color.green,
            handler: handler
        )
    }
}

struct CallDownButton: View {
    let handler: () -> Void

    var body: some View {
        CallButton(
            systemImageName: "phone.down.fill",
            backgroundColor: Color.pink,
            handler: handler
        )
    }
}

private struct CallButton: View {
    let systemImageName: String
    let backgroundColor: Color
    let handler: () -> Void

    var body: some View {
        Image(systemName: systemImageName)
            .font(.system(size: 31))
            .frame(size: 68)
            .background(backgroundColor)
            .clipShape(Circle())
            .onTapGesture(perform: handler)
    }
}

//
//  SelectableButton.swift
//  Well
//
//  Created by Phil on 13.02.2022.
//  Copyright © 2022 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct SelectableButton<Content: View>: View {
    let selected: Bool
    let onClick: () -> Void
    let content: Content

    var body: some View {
        Button(action: onClick) {
            content
                .foregroundColorKMM(selected ? .companion.White : .companion.DarkGrey)
        }
        .background {
            let shape = Shapes.medium
            if selected {
                shape.foregroundColorKMM(.companion.Green)
            } else {
                shape.stroke(lineWidth: 2)
                    .foregroundColorKMM(.companion.LightGray)
                    .padding(1)
            }
        }
    }
}

//
//  SelectionScreen.swift
//  Well
//
//  Created by Philip Dukhov on 2/12/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct SelectionScreen: View {
    @Binding var selection: Set<KotlinInt>
    let variants: [String]
    let multipleSelection: Bool
    let onSelectionChanged: (Set<KotlinInt>) -> Void

    var body: some View {
        List {
            ForEachIndexed(variants) { index, variant in
                let index = KotlinInt(integerLiteral: index)
                let selected = selection.contains(index)
                HStack {
                    Text(variant)
                    Spacer()
                    if selected {
                        Image(systemName: "checkmark")
                            .foregroundColorKMM(.companion.Green)
                    }
                }.padding().fillMaxWidth().listRowInsets(.zero)
                .background(
                    Color.white.overlay(
                        (selected ? ColorConstants.Green.withAlpha(alpha: 0.1) : .companion.White).toColor()
                    )
                )
                .onTapGesture {
                    if multipleSelection {
                        if selected {
                            selection.remove(index)
                        } else {
                            selection.insert(index)
                        }
                    } else {
                        selection = Set([index])
                        onSelectionChanged(selection)
                    }
                }
            }
        }
    }
}

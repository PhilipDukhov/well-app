//
//  SelectionScreen.swift
//  Well
//
//  Created by Philip Dukhov on 2/12/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct SelectionScreen: View {
    let title: String
    @State var selection: Set<String>
    let variants: [String]
    let multipleSelection: Bool
    let onSelectionChanged: (Set<String>) -> Void
    let onCancel: () -> Void
    
    var body: some View {
        NavigationBar(
            title: title,
            leftItem: NavigationBarItem(text: "Cancel", handlerOpt: onCancel),
            rightItem: multipleSelection ? NavigationBarItem(text: "Done", handler: onSelectionChanged(selection)) : nil
        )
        List {
            ForEach(variants, id: \.self) { variant in
                let selected = selection.contains(variant)
                HStack {
                    Text(variant)
                    Spacer()
                    if selected {
                        Image(systemName: "checkmark")
                            .foregroundColorKMM(ColorConstants.Green)
                    }
                }.padding().fillMaxWidth().listRowInsets(.zero)
                .backgroundColorKMM(selected ? ColorConstants.Green.withAlpha(alpha: 0.1) : ColorConstants.White)
                .onTapGesture {
                    if multipleSelection {
                        if selected {
                            selection.remove(variant)
                        } else {
                            selection.insert(variant)
                        }
                    } else {
                        selection = Set([variant])
                        onSelectionChanged(selection)
                    }
                }
            }
        }
    }
}

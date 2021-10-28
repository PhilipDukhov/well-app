//
//  TabView.swift
//  Well
//
//  Created by Phil on 27.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct TabRow<Tab: Hashable, TabView: View>: View {
    let tabs: [Tab]
    @Binding
    var selection: Tab
    
    let selectionColor: SharedMobile.Color
    
    @ViewBuilder
    let tabView: (Tab) -> TabView
    // privates
    @State
    private var tabWidths = [Tab: CGFloat]()

    var body: some View {
        VStack(alignment: .crossAlignment, spacing: 0) {
            HStack(spacing: 0) {
                ForEachIndexed(tabs) { i, tab in
                    let selected = tab == selection
                    if i > 0 {
                        Spacer().frame(width: 50)
//                        Spacer().fillMaxWidth()
                    }
                    Button(action: {
                        withAnimation {
                            selection = tab
                        }
                    }) {
                        tabView(tab)
                            .foregroundColorKMM(selectionColor)
                            .opacity(selected ? 1 : 0.5)
                            .alignmentGuide(selected ? .crossAlignment : .center) { d in
                                d[HorizontalAlignment.center]
                            }
                    }.sizeReader { size in
                        tabWidths[tab] = size.width
                    }
                }
            }
            Rectangle()
                .fill(selectionColor.toColor())
                .frame(width: tabWidths[selection], height: 3)
                .alignmentGuide(.crossAlignment) { d in
                    d[HorizontalAlignment.center]
                }
        }
    }
}

private extension HorizontalAlignment {
    private enum CrossAlignment: AlignmentID {
        static func defaultValue(in d: ViewDimensions) -> CGFloat {
            d[HorizontalAlignment.center]
        }
    }

    static let crossAlignment = HorizontalAlignment(CrossAlignment.self)
}

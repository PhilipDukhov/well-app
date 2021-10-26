//
// Created by Phil on 07.05.2021.
//

import SwiftUI

struct SelectableHVStack<Item, ItemView: View>: View {
    @Binding
    var selectedIndex: Int

    let items: [Item]
    let spacing: CGFloat
    let itemBuilder: (Item, Bool) -> ItemView

    init(items: [Item], spacing: CGFloat = defaultHVStackSpacing, selectedIndex: Binding<Int>, itemBuilder: @escaping (Item, Bool) -> ItemView) {
        self.items = items
        self.spacing = spacing
        self._selectedIndex = selectedIndex
        self.itemBuilder = itemBuilder
    }

    var body: some View {
        HVStack(items: Array(items.enumerated()), spacing: spacing) { item in
            Button {
                withAnimation {
                    selectedIndex = item.offset
                }
            } label: {
                let selected = item.offset == selectedIndex
                itemBuilder(item.element, selected)
                    .foregroundColorKMM(selected ? .companion.White : .companion.Black)
                    .backgroundColorKMM(selected ? .companion.Green : nil)
                    .clipShape(Capsule())
                    .overlay(
                        Capsule()
                            .strokeColorKMM(.companion.LightGray, lineWidth: selected ? 0 : 1.5)
                    )
            }
        }
    }
}

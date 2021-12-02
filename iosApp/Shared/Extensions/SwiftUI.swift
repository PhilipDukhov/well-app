//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI

extension View {
    @inline(__always)
    func visible(_ visible: Bool) -> some View {
        opacity(visible ? 1 : 0)
            .zIndex(visible ? 1 : 0)
    }

    @inline(__always)
    func fillMaxSize() -> some View {
        frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    @inline(__always)
    func fillMaxWidth() -> some View {
        frame(maxWidth: .infinity)
    }

    @inline(__always)
    func fillMaxHeight() -> some View {
        frame(maxHeight: .infinity)
    }
    
    @inline(__always)
    func height(_ height: CGFloat) -> some View {
        frame(height: height)
    }
    
    @inline(__always)
    func width(_ height: CGFloat) -> some View {
        frame(width: height)
    }

    @inline(__always)
    func frame(
        size: CGFloat,
        alignment: Alignment = .center
    ) -> some View {
        frame(
            width: size,
            height: size,
            alignment: alignment
        )
    }

    @inline(__always)
    func frame(
        size: CGSize?,
        alignment: Alignment = .center
    ) -> some View {
        frame(
            width: size?.width,
            height: size?.height,
            alignment: alignment
        )
    }

    @inline(__always)
    func frameInfinitable(
        size: CGSize,
        alignment: Alignment = .center
    ) -> some View {
        frame(
            minWidth: size.width == .infinity ? nil : size.width,
            maxWidth: size.width,
            minHeight: size.height == .infinity ? nil : size.height,
            maxHeight: size.height,
            alignment: alignment
        )
    }

    @inline(__always)
    func frame(
        minSize: CGFloat,
        alignment: Alignment = .center
    ) -> some View {
        frame(
            minWidth: minSize,
            minHeight: minSize,
            alignment: alignment
        )
    }
    
    func sizeReader(_ block: @escaping (CGSize) -> Void) -> some View {
        background(
            GeometryReader { geometry -> SwiftUI.Color in
                DispatchQueue.main.async { // to avoid warning
                    block(geometry.size)
                }
                return SwiftUI.Color.clear
            }
        )
    }
    
    func overlay<Content: View>(@ViewBuilder _ block: () -> Content) -> some View {
        overlay(block())
    }
    
    func background<Content: View>(@ViewBuilder _ block: () -> Content) -> some View {
        background(block())
    }
    
#if DEBUG
    @inline(__always)
    func printUI(_ items: Any..., separator: String = " ", file: String = #file, function: String = #function) -> Self {
        print(items.map { "\($0)" }.joined(separator: separator))
        return self
    }
#endif

    @ViewBuilder
    func badgeIfAvailable(_ count: Int) -> some View {
        if #available(iOS 15.0, *) {
            badge(count)
        } else {
            self
        }
    }
}

// swiftlint:disable:next identifier_name
func Button<Item, Label: View>(
    item: Item?,
    action: @escaping (Item) -> Void,
    @ViewBuilder label: () -> Label
) -> some View {
    Button(
        action: {
            action(item!)
        },
        label: label
    ).disabled(item == nil)
}

// swiftlint:disable:next identifier_name
func Button<Item, Label: View>(
    item: Item,
    disabled: (Item) -> Bool,
    action: @escaping (Item) -> Void,
    @ViewBuilder label: () -> Label
) -> some View {
    Button(
        action: {
            action(item)
        },
        label: label
    ).disabled(disabled(item))
}

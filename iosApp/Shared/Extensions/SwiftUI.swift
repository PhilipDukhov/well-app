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
    func fillMaxSize(alignment: Alignment = .center) -> some View {
        frame(maxWidth: .infinity, maxHeight: .infinity, alignment: alignment)
    }

    @inline(__always)
    func fillMaxWidth(alignment: Alignment = .center) -> some View {
        frame(maxWidth: .infinity, alignment: alignment)
    }

    @inline(__always)
    func fillMaxHeight(alignment: Alignment = .center) -> some View {
        frame(maxHeight: .infinity, alignment: alignment)
    }
    
    @inline(__always)
    func height(_ height: CGFloat) -> some View {
        frame(height: height)
    }
    
    @inline(__always)
    func width(_ width: CGFloat) -> some View {
        frame(width: width)
    }

    @inline(__always)
    func frame(
        size: CGFloat?,
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
            GeometryReader { geometry in
                SwiftUI.Color.clear
					.onAppear {
						block(geometry.size)
					}
					.onChange(of: geometry.size, perform: block)
            }
        )
    }
    
    func overlay<Content: View>(@ViewBuilder _ block: () -> Content) -> some View {
        overlay(block())
    }
    
    func background<Content: View>(@ViewBuilder _ block: () -> Content) -> some View {
        background(block())
    }

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

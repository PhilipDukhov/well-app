//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

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
        size: CGSize,
        alignment: Alignment = .center
    ) -> some View {
        frame(
            width: size.width,
            height: size.height,
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
}

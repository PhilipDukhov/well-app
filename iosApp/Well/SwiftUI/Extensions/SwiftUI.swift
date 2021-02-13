//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI

extension View {
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

    @inline(__always)
    func doBlock(block: () -> Void) -> some View {
        block()
        return EmptyView()
    }

    @inline(__always)
    func printUI(_ vars: Any...) -> some View {
        print(vars.map {
            "\($0)"
        }
            .joined(separator: " "))
        return EmptyView()
    }
}

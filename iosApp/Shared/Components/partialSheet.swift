//
//  partialSheet.swift
//  Well
//
//  Created by Phil on 29.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

extension View {
    func partialSheet<Item: Equatable, SheetContent: View, OverlayContent: View>(
        item: Item?,
        onDismiss: @escaping () -> Void,
        @ViewBuilder sheetContent: @escaping (Item) -> SheetContent,
        @ViewBuilder overlayContent: @escaping (Item) -> OverlayContent
    ) -> some View {
        modifier(
            PartialSheetModifier(
                item: item,
                onDismiss: onDismiss,
                sheetContent: sheetContent,
                overlayContent: overlayContent
            )
        )
    }
}

private struct PartialSheetModifier<Item: Equatable, SheetContent: View, OverlayContent: View>: ViewModifier {
    let item: Item?
    let onDismiss: () -> Void
    @ViewBuilder
    let sheetContent: (Item) -> SheetContent
    @ViewBuilder
    let overlayContent: (Item) -> OverlayContent
    
    private let shape = RoundedCornerRectangle(radius: 16, corners: .top)
    
    @State
    private var overlayDisplayedCounter = 0
    
    func body(content: Content) -> some View {
        ZStack(alignment: .bottom) {
            content
                .animation(.none)
                .overlay {
                    if item == nil {
                        SwiftUI.Color.clear
                    } else {
                        ColorConstants.Black
                            .withAlpha(alpha: ColorConstants.inactiveAlpha * (overlayDisplayedCounter == 0 ? 1 : 1.5))
                            .toColor()
                            .ignoresSafeArea()
                            .onTapGesture(perform: onDismiss)
                    }
                }
            if let item = item {
                sheetContent(item)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .padding(.top, 16)
                    .fillMaxWidth()
                    .background(
                        shape.foregroundColorKMM(.companion.White)
                            .edgesIgnoringSafeArea(.all)
                    )
                    .overlay {
                        if let item = item {
                            overlayContent(item)
                                .transition(.opacity)
                                .clipShape(shape)
                                .edgesIgnoringSafeArea(.all)
                                .onAppear {
                                    withAnimation {
                                        overlayDisplayedCounter += 1
                                    }
                                }
                                .onDisappear {
                                    withAnimation {
                                        overlayDisplayedCounter -= 1
                                    }
                                }
                        }
                    }
                    .zIndex(1)
            }
        }.animation(.default, value: item != nil)
    }
}

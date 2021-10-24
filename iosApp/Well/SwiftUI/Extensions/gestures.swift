//
// Created by Phil on 22.10.2021.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

extension View {
    func swipeableLeftRight(onLeft: @escaping () -> Void, onRight: @escaping () -> Void) -> some View {
        gesture(
            DragGesture(minimumDistance: 3.0, coordinateSpace: .local)
                .onEnded { value in
                    guard abs(value.translation.width) > max(abs(value.translation.height), 30) else { return }
                    if value.translation.width > 0  {
                        onRight()
                    } else {
                        onLeft()
                    }
                }
        )
    }
}

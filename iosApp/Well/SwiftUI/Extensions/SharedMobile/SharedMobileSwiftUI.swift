//
//  SwiftUI.swift
//  Well
//
//  Created by Philip Dukhov on 2/11/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

extension View {
    @inline(__always)
    func foregroundColorKMM(_ color: SharedMobile.Color) -> some View {
        foregroundColor(color.toColor())
    }
    
    @inline(__always)
    func backgroundColorKMM(_ color: SharedMobile.Color) -> some View {
        background(color.toColor())
    }
}

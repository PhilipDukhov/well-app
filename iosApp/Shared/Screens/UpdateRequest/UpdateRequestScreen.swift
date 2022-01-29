//
//  UpdateRequestScreen.swift
//  Well
//
//  Created by Phil on 29.01.2022.
//  Copyright © 2022 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct UpdateRequestScreen: View {
    let state: UpdateRequestFeature.State
    let listener: (UpdateRequestFeature.Msg) -> Void

    var body: some View {
        ZStack {
            Image("overlayMessageBackground")
                .fillMaxSize()
                .background(GradientView(gradient: .main))
                .ignoresSafeArea()
            VStack {
                Image("failedFace")
                Text(state.text)
                    .textStyle(.h4)
                    .foregroundColor(.white)
            }
        }
    }
}

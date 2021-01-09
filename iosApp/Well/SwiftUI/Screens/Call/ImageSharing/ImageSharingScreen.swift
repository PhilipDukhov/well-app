//
// Created by Philip Dukhov on 1/9/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

extension CGSize {
    func toSize() -> ServerModelsSize {
        .init(width: Double(width), height: Double(height))
    }
}

struct ImageSharingScreen: View {
    let state: ImageSharingFeature.State
    let listener: (ImageSharingFeature.Msg) -> Void

    var body: some View {
        GeometryReader { geometry in
            onAppear {
                listener(
                    ImageSharingFeature.MsgUpdateLocalViewSize.init(
                        size: geometry.size.toSize()
                    )
                )
            }
            state.image.map {
                Image(uiImage: $0.uiImage)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }.frame(maxWidth: .infinity, maxHeight: .infinity)
            .edgesIgnoringSafeArea(.all)
    }
}

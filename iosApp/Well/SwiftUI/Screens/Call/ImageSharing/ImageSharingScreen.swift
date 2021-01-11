//
// Created by Philip Dukhov on 1/9/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

extension CGSize {
    func toSize() -> ServerModelsSize {
        .init(width: Double(width), height_: Double(height))
    }
}

struct ImageSharingScreen: View {
    let state: ImageSharingFeature.State
    let listener: (ImageSharingFeature.Msg) -> Void

    var body: some View {
        Text("ok")
//        GeometryReader { geometry in
//            onAppear {
//                )
//            }
        state.image.map {
                Image(uiImage: $0.uiImage)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }.frame(maxWidth: .infinity, maxHeight: .infinity)
            .edgesIgnoringSafeArea(.all)
        onAppear {
            print("test1")
//            listener(
//                ImageSharingFeature.MsgUpdateLocalViewSize(
//                    size: UIScreen.main.bounds.size.toSize()//geometry.size.toSize()
//                )
//            )
//            print("test2")
        }
    }
}

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

extension CGSize {
    func toNativeScaleSize() -> ServerModelsSize {
        .init(
            width: Double(width * UIScreen.main.nativeScale),
            height_: Double(height * UIScreen.main.nativeScale)
        )
    }
}

struct ImageSharingScreen: View {
    let state: ImageSharingFeature.State
    let listener: (ImageSharingFeature.Msg) -> Void

    var body: some View {
        ZStack {
            topPanel()
//        GeometryReader { geometry in
//            onAppear {
//                )
//            }
//            if let image = state.image {
//                Image(uiImage: $0.uiImage)
//                    .resizable()
//                    .aspectRatio(contentMode: .fit)
//                    .frame(maxWidth: .infinity, maxHeight: .infinity)
//            } else {
//                Text("ok")
//            }
        }
            .onAppear {
                listener(
                    ImageSharingFeature.MsgUpdateLocalViewSize(
                        size: UIScreen.main.bounds.size.toNativeScaleSize()
                    )
                )
            }
    }

    @ViewBuilder
    func topPanel() -> some View {
        HStack {
            Image(systemName: "chevron.left")
                .frame(size: 45)
                .onTapGesture {
                }
            Spacer()
            Image(systemName: "arrow.uturn.left")
                .frame(size: 45)
                .onTapGesture {
                }
            Image(systemName: "arrow.uturn.right")
                .frame(size: 45)
                .onTapGesture {
                }
            Spacer()
            Text("Done")
                .onTapGesture {
                }
        }
    }

    @ViewBuilder
    func bottomPanel() -> some View {

    }
}

//            DrawShape(points: points)
//                .stroke(lineWidth: 5) // here you put width of lines
//                .foregroundColor(.blue)
//

//            .gesture(
//                DragGesture()
//                    .onChanged { value in
//                        addNewPoint(value)
//                    }
//                    .onEnded { _ in
//                        // here you perform what you need at the end
//                    }
//            )

//            private func addNewPoint(
//                _ value: DragGesture.Value
//            ) {
//                // here you can make some calculations based on previous points
//                points.append(value.location)
//            }
//
//            @State var points: [CGPoint] = []

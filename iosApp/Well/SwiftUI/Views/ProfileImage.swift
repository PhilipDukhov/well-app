//
// Created by Philip Dukhov on 12/31/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct ProfileImage: View {
    let image: SharedImage?
    let clipCircle: Bool

    init(
        _ user: User,
        clipCircle: Bool = true
    ) {
        self.init(image: user.profileImage(), clipCircle: clipCircle)
    }
    
    init(
        image: SharedImage?,
        clipCircle: Bool = true
    ) {
        self.image = image
        self.clipCircle = clipCircle
    }

    var body: some View {
        content
            .clipShape(PartCircleShape(part: clipCircle ? 1 : 0))
    }
    
    private var content: some View {
        if let image = image {
            switch image {
            case let image as UrlImage where image.url.toURL() != nil:
                return AnyView(
                    AsyncImage(
                        url: image.url.toURL()!,
                        placeholder: {
                            ActivityIndicator()
                        },
                        image: {
                            buildImage(uiImage: $0)
                        }
                    )
                )
                
            case let image as ImageContainer:
                return AnyView(
                    buildImage(uiImage: image.uiImage)
                )
                
            default: fatalError()
            }
        } else {
            return AnyView(
                Rectangle().foregroundColorKMM(ColorConstants.LightGray)
            )
        }
    }
    
    private func buildImage(uiImage: UIImage) -> some View {
        Image(uiImage: uiImage)
            .resizable()
            .scaledToFill()
    }
}

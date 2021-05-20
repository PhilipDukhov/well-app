//
// Created by Philip Dukhov on 12/31/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct ProfileImage: View {
    let image: SharedImage?
    let clipCircle: Bool
    let aspectRatio: CGFloat?
    let contentMode: ContentMode

    init(
        _ user: User,
        clipCircle: Bool = true,
        aspectRatio: CGFloat? = nil,
        contentMode: ContentMode = .fill
    ) {
        self.init(image: user.profileImage(),
            clipCircle: clipCircle,
            aspectRatio: aspectRatio,
            contentMode: contentMode)
    }

    init(
        image: SharedImage?,
        clipCircle: Bool = true,
        aspectRatio: CGFloat? = nil,
        contentMode: ContentMode = .fill
    ) {
        self.image = image
        self.clipCircle = clipCircle
        self.aspectRatio = aspectRatio
        self.contentMode = contentMode
    }

    var body: some View {
        content.clipShape(PartCircleShape(part: clipCircle ? 1 : 0))
    }

    @ViewBuilder
    private var content: some View {
        if let image = image {
            switch image {
            case let image as UrlImage where image.url.toURL() != nil:
                AsyncImage(
                    url: image.url.toURL()!,
                    placeholder: {
                        ActivityIndicator()
                    },
                    image: {
                        buildImage(uiImage: $0)
                    }
                )

            case let image as ImageContainer:
                buildImage(uiImage: image.uiImage)

            default: fatalError()
            }
        } else {
            Rectangle().foregroundColorKMM(ColorConstants.LightGray)
                .aspectRatio(aspectRatio, contentMode: contentMode)
        }
    }

    private func buildImage(uiImage: UIImage) -> some View {
        Image(uiImage: uiImage)
            .resizable()
            .aspectRatio(contentMode: .fill)
    }
}

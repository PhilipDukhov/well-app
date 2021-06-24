//
// Created by Philip Dukhov on 12/31/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct ProfileImage: View {
    let image: SharedMobile.SharedImage?
    let clipCircle: Bool
    let aspectRatio: CGFloat?
    let contentMode: ContentMode
    let initials: String?

    init(
        _ user: User,
        clipCircle: Bool = true,
        aspectRatio: CGFloat? = nil,
        contentMode: ContentMode = .fill
    ) {
        self.init(
            image: user.profileImage(),
            clipCircle: clipCircle,
            aspectRatio: aspectRatio,
            contentMode: contentMode,
            initials: user.initials
        )
    }

    init(
        image: SharedMobile.SharedImage?,
        clipCircle: Bool = true,
        aspectRatio: CGFloat? = nil,
        contentMode: ContentMode = .fill,
        initials: String? = nil
    ) {
        self.image = image
        self.clipCircle = clipCircle
        self.aspectRatio = aspectRatio
        self.contentMode = contentMode
        self.initials = initials
    }

    var body: some View {
        SharedImage(
            image: image,
            placeholder: placeholder(),
            aspectRatio: aspectRatio,
            contentMode: contentMode
        ).clipShape(PartCircleShape(part: clipCircle ? 1 : 0))
    }

    private func placeholder() -> some View {
        GeometryReader { geometry in
            ZStack {
                Rectangle().foregroundColorKMM(ColorConstants.LightGray)
                    .aspectRatio(aspectRatio, contentMode: contentMode)
                initials.map { initials in
                    Text(initials)
                        .font(.system(size: geometry.size.height > geometry.size.width ? geometry.size.width * 0.4 : geometry.size.height * 0.4))
                        .foregroundColor(.white)
                }
            }
        }
    }
}

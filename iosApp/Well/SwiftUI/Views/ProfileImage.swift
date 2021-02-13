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
        let view: AnyView
        if let image = image {
            switch image {
            case let image as UrlImage where image.url.toURL() != nil:
                view = AnyView(
                    AsyncImage(
                        url: image.url.toURL()!,
                        placeholder: {
                            ActivityIndicator()
                        },
                        image: {
                            Image(uiImage: $0)
                                .resizable()
                                .scaledToFill()
                        }
                    )
                )
                
            case let image as ImageContainer:
                view = AnyView(
                    Image(uiImage: image.uiImage)
                        .resizable()
                        .scaledToFill()
                )
                
            default: fatalError()
            }
        } else {
            view = AnyView(
                Rectangle().foregroundColorKMM(ColorConstants.LightGray)
            )
        }
        return view
            .clipShape(PartCircleShape(part: clipCircle ? 1 : 0))
    }
}

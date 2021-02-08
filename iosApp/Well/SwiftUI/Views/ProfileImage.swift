//
// Created by Philip Dukhov on 12/31/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct ProfileImage: View {
    let viewModel: User
    let clipCircle: Bool

    init(
        _ viewModel: User,
        clipCircle: Bool = true
    ) {
        self.viewModel = viewModel
        self.clipCircle = clipCircle
    }

    var body: some View {
        let shape = PartCircleShape(part: clipCircle ? 1 : 0)
        if let profileImageURL = viewModel.profileImageURL {
            printUI("ProfileImage update")
            AsyncImage(
                url: profileImageURL,
                placeholder: {
                    ActivityIndicator()
                },
                image: {
                    printUI("AsyncImage update")
                    Image(uiImage: $0)
                        .resizable()
                        .scaledToFill()
                }
            )
                .clipShape(shape)
        } else {
            Rectangle()
                .foregroundColor(.orange)
                .clipShape(shape)
        }
    }
}

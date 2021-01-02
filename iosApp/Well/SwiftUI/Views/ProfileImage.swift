//
// Created by Philip Dukhov on 12/31/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct ProfileImage: View {
    let viewModel: ServerModelsUser

    init(
        _ viewModel: ServerModelsUser
    ) {
        self.viewModel = viewModel
    }

    var body: some View {
        if let profileImageURL = viewModel.profileImageURL {
            AsyncImage(
                url: profileImageURL,
                placeholder: {
                    ActivityIndicator()
                },
                image: {
                    Image(uiImage: $0)
                        .resizable()
                }
            )
                .clipShape(Circle())
        } else {
            Circle()
                .foregroundColor(.orange)
        }
    }
}

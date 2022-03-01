//
//  UserCell.swift
//  Well
//
//  Created by Philip Dukhov on 12/3/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile
import Combine

struct UserCell: View {
    let user: User
    let toggleFavorite: () -> Void

    var body: some View {
        HStack {
            ProfileImage(user)
                .frame(size: 45)
                .padding(.trailing)
            VStack(alignment: .leading) {
                Text(user.fullName)
                    .textStyle(.caption)
                user.academicRank.map { academicRank in
                    Text(academicRank.localizedDescription())
                        .textStyle(.captionLight)
                }
                user.countryName().map { countryName in
                    HStack {
                        Image("profile/location")
                        Text(countryName)
                            .textStyle(.captionLight)
                    }
                }
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 0) {
                ReviewInfoView(reviewInfo: user.reviewInfo)
                Spacer()
                ToggleFavoriteButton(favorite: user.favorite, action: toggleFavorite)
            }
        }.padding().background(Color.white)
    }
}

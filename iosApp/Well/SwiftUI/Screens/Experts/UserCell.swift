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
                    .style(.caption)
                user.academicRank.map { academicRank in
                    Text(academicRank.localizedDescription())
                        .style(.captionLight)
                }
                user.countryName().map { countryName in
                    HStack {
                        Image(uiImage: R.image.profile.location()!)
                        Text(countryName)
                            .style(.captionLight)
                    }
                }
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 0) {
                RatingInfoView(ratingInfo: user.ratingInfo)
                Spacer()
                ToggleFavoriteButton(favorite: user.favorite, action: toggleFavorite)
            }
        }.padding().background(Color.white)
    }
}

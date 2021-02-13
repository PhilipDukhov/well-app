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
    let viewModel: User
    let onSelect: () -> Void
    let onCallButtonTap: () -> Void

    var body: some View {
        HStack {
            ProfileImage(viewModel)
                .frame(size: 45)
                .padding()
            Text(viewModel.fullName)
            Spacer()
            Image(systemName: "phone.fill")
                .font(.system(size: 30))
                .foregroundColorKMM(ColorConstants.Green)
                .padding()
                .onTapGesture(perform: onCallButtonTap)
        }.background(Color.white)
        .onTapGesture(perform: onSelect)
    }
}

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
    let viewModel: ServerModelsUser

    var body: some View {
        HStack {
            ProfileImage(viewModel)
                .frame(size: 45)
                .padding()
            Text(viewModel.fullName)
            Spacer()
        }
    }
}

//
//  UserCell.swift
//  Well
//
//  Created by Philip Dukhov on 12/3/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import Shared
import Combine

struct UserCell: View {
    let viewModel: ServerModelsUser
    
    var body: some View {
        HStack {
            Image(uiImage: {
                switch viewModel.type {
                case .facebook:
                    return R.image.loginSocials.facebook()!
                    
                case .google:
                    return R.image.loginSocials.google()!
                default: fatalError()
                }
            }())
            .renderingMode(.template)
            .foregroundColor(.orange)
            Text(viewModel.fullName)
            Spacer()
            Image(systemName: "phone.fill")
                .renderingMode(.template)
                .foregroundColor(.green)
        }
    }
}

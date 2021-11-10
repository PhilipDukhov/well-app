//
//  LoginScreen.swift
//  Well
//
//  Created by Philip Dukhov on 2/13/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct LoginScreen: View {
    let state: LoginFeature.State
    let listener: (LoginFeature.Msg) -> Void
    
    var body: some View {
        ZStack {
            VStack {
                Spacer()
                HStack(spacing: 0) {
                    Spacer()
                    ForEach(SocialNetwork.companion.allCases, id: \.self) { socialNetwork in
                        Image("loginSocials/\(socialNetwork.name.lowercased())")
                            .onTapGesture {
                                listener(LoginFeature.MsgOnSocialNetworkSelected(socialNetwork: socialNetwork))
                            }
                        Spacer()
                    }
                }.padding(.bottom)
            }.fillMaxSize().background(
                Image("loginBackground")
                    .resizable()
                    .scaledToFill()
                    .overlay(GradientView(gradient: .login))
                    .edgesIgnoringSafeArea(.all)
            )
            if state.processing {
                InactiveOverlay.withActivityIndicator()
            }
        }
    }
}

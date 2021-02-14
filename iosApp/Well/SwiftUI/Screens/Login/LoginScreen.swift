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
                    ForEach(SocialNetwork.Companion().allCases, id: \.self) { socialNetwork in
                        Image(uiImage: socialNetwork.image)
                            .onTapGesture {
                                listener(LoginFeature.MsgOnSocialNetworkSelected(socialNetwork: socialNetwork))
                            }
                        Spacer()
                    }
                }.padding(.bottom)
            }.fillMaxSize().background(background.edgesIgnoringSafeArea(.all))
            if state.processing {
                Color.black
                    .opacity(0.3)
                    .fillMaxSize()
                    .edgesIgnoringSafeArea(.all)
                ActivityIndicator(style: .large, color: .white)
            }
        }
    }
    
    var background: some View {
        Color(hex: 0x1B3D6D).overlay(LinearGradient(
            gradient: Gradient(
                stops: [
                    .init(color: Color(hex: 0x1BFFE4, alpha: 0.8), location: 0.109375),
                    .init(color: Color(hex: 0x1B3D6D), location: 0.984375),
                ]
            ),
            startPoint: .init(x: 0.762648, y: -0.376472566),
            endPoint: .init(x: 0.812653333, y: 0.803190265)
        ).opacity(0.5))
    }
}

private extension SocialNetwork {
     var image: UIImage {
        let images = R.image.loginSocials.self
        switch self {
        //        case .apple:
        //            return images.apple()
        //        case .twitter:
        //            return images.twitter()
        case .facebook:
            return images.facebook()!
        case .google:
            return images.google()!
            
        default: fatalError()
        }
    }
}

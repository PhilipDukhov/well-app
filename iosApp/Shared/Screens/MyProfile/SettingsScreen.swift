//
//  SettingsScreen.swift
//  Well
//
//  Created by Phil on 23.01.2022.
//  Copyright © 2022 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = SettingsFeature

struct SettingsScreen: View {
    let state: SettingsFeature.State
    let listener: (SettingsFeature.Msg) -> Void

    var body: some View {
        List {
            Button(action: {
                listener(Feature.MsgOpenTechnicalSupport())
            }) {
                Text(Feature.Strings.shared.technicalSupport)
            }
            Button(action: {
                listener(Feature.MsgLogout())
            }) {
                Text(Feature.Strings.shared.logout)
            }
            Button(action: {
                listener(Feature.MsgDeleteProfile())
            }) {
                Text(Feature.Strings.shared.deleteProfile)
            }
        }.textStyle(.body1Light)
    }
}

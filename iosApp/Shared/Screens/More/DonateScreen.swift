//
//  DonateScreen.swift
//  Well
//
//  Created by Phil on 12.02.2022.
//  Copyright © 2022 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = DonateFeature

struct DonateScreen: View {
    let state: DonateFeature.State
    let listener: (DonateFeature.Msg) -> Void

    var body: some View {
        NavigationBar(
            title: Feature.Strings.shared.title,
            leftItem: .back {
                listener(Feature.MsgBack())
            }
        )
    }
}

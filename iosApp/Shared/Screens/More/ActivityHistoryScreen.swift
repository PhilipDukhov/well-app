//
//  ActivityHistoryScreen.swift
//  Well
//
//  Created by Phil on 12.02.2022.
//  Copyright © 2022 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = ActivityHistoryFeature

struct ActivityHistoryScreen: View {
    let state: ActivityHistoryFeature.State
    let listener: (ActivityHistoryFeature.Msg) -> Void

    var body: some View {
        NavigationBar(
            title: state.title,
            leftItem: .back {
                listener(Feature.MsgBack())
            }
        )
    }
}

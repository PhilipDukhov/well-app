//
//  WellAcademyScreen.swift
//  Well
//
//  Created by Phil on 22.01.2022.
//  Copyright © 2022 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = WellAcademyFeature

struct WellAcademyScreen: View {
    let state: WellAcademyFeature.State
    let listener: (WellAcademyFeature.Msg) -> Void

    @State
    private var text = ""

    var body: some View {
        NavigationBar(
            title: Feature.StateCompanion.shared.title,
            leftItem: NavigationBarItem(view: Image(systemName: "chevron.left")) {
                listener(Feature.MsgBack())
            }
        )
        VStack {
            Text(state.text)
                .textStyle(.body1Light)
            ForEachIndexed(state.items) { _, item in
                HStack {
                    Image("wellAcademy/\(item.name)")
                    Text(item.title)
                        .textStyle(.body1Light)
                    Spacer()
                }
                .opacity(0.5)
            }.padding(.horizontal)
            Spacer()
        }.padding()
    }
}

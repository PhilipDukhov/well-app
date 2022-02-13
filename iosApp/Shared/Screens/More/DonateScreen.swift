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

    @State
    private var selectedVariantIndex = 0

    @State
    private var isRecurring = true

    var body: some View {
        NavigationBar(
            title: Feature.Strings.shared.title,
            leftItem: .back {
                listener(Feature.MsgBack())
            }
        )
        VStack(spacing: 0) {
            Spacer(minLength: 34)
            Text(Feature.Strings.shared.text)
                .textStyle(.body1Light)
            Text(Feature.Strings.shared.howMuch)
                .textStyle(.subtitle2)
                .padding(.vertical, 38)
            HStack(spacing: 17) {
                ForEachIndexed(state.variants) { i, variant in
                    SelectableButton(
                        selected: i == selectedVariantIndex,
                        onClick: {
                            withAnimation {
                                selectedVariantIndex = i
                            }
                        },
                        content: Text("$\(variant.price)")
                            .textStyle(.body1)
                            .padding(.vertical)
                            .multilineTextAlignment(.center)
                            .fillMaxWidth()
                    )
                }
            }
            Spacer(minLength: 31)
            Toggle(Feature.Strings.shared.isRecurring, isOn: $isRecurring)
                .foregroundColor(.black)
                .textStyle(.body1)
            Spacer(minLength: 50)
            Button(action: {
                listener(Feature.MsgDonate(variant: state.variants[selectedVariantIndex], isRecurring: isRecurring))
            }) {
                Text(Feature.Strings.shared.continue_)
            }.buttonStyle(ActionButtonStyle(style: .onWhite))
            Spacer(minLength: 300)
        }.padding(.horizontal, 17)
    }
}

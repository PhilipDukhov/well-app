//
// Created by Phil on 29.05.2021.
//

import SwiftUI
import SharedMobile

struct MoreScreen: View {
    let state: MoreFeature.State
    let listener: (MoreFeature.Msg) -> Void

    var body: some View {
        NavigationBar(
            title: "More"
        )
        VStack {
            ForEachIndexed(state.items) { _, item in
                Button(action: {
                    listener(MoreFeature.MsgSelectItem(item: item))
                }) {
                    HStack(spacing: 16) {
                        item.icon()
                            .font(.system(size: 25))
                            .foregroundColorKMM(.companion.LightBlue)
                        Text("\(item)")
                            .textStyle(.body1)
                        Spacer()
                    }.padding()
                }
            }
        }
        Spacer()
    }
}

private extension MoreFeature.StateItem {
    func icon() -> Image {
        switch self {
        case .support:
            return Image(systemName: "wrench")
        case .about:
            return Image(systemName: "info.circle")
//            person.badge.plus "Invite a colleague"
        case .wellacademy:
            return Image(systemName: "building.columns")
//            building.columns "WELL Academy"
//            clock.arrow.circlepath "Activity history"
//            face.smiling "Sponsor & donate"
//            wrench "Technical support"
//            info.circle "About"
        default:
            fatalError("\(self) MoreFeature.StateItem icon needed ")
        }
    }
}

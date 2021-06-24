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
            ForEachEnumerated(state.items) { i, item in
                Button(action: {
                    listener(MoreFeature.MsgSelectItem(item: item))
                }) {
                    HStack(spacing: 16) {
                        item.icon()
                            .font(.system(size: 25))
                            .foregroundColorKMM(ColorConstants.LightBlue)
                        Text("\(item)")
                            .style(.body2)
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
//            calendar "My calendar"
//            person.badge.plus "Invite a colleague"
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

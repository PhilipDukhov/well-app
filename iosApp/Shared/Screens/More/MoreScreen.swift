//
// Created by Phil on 29.05.2021.
//

import SwiftUI
import SharedMobile

private typealias Feature = MoreFeature

struct MoreScreen: View {
    let state: MoreFeature.State
    let listener: (MoreFeature.Msg) -> Void

    var body: some View {
        NavigationBar(title: state.title)
        VStack {
            ForEachIndexed(state.items) { _, item in
                Button(action: {
                    listener(Feature.MsgSelectItem(item: item))
                }) {
                    HStack(spacing: 16) {
                        item.icon()
                            .font(.system(size: 25))
                            .foregroundColorKMM(.companion.LightBlue)
                        Text(item.title)
                            .textStyle(.body1)
                        Spacer()
                    }.padding()
                }
            }
        }
        Spacer()
    }
}

private extension Feature.StateItem {
    @ViewBuilder
    func icon() -> some View {
        switch self {
        case .technicalsupport:
            Image(systemName: "wrench")
        case .about:
            Image(systemName: "info.circle")
        case .wellacademy:
            Image(systemName: "building.columns")
        case .activityhistory:
            Image(systemName: "clock.arrow.circlepath")
        case .favorites:
            Image(systemName: "heart")
        case .donate:
            Image(systemName: "face.smiling")
        case .invitecolleague:
            ZStack {
                // hack to match system image size
                Image(systemName: "heart").opacity(0)
                Image("inviteColleague")
            }
        default:
            fatalError("\(self) MoreFeature.StateItem icon needed ")
        }
    }
}

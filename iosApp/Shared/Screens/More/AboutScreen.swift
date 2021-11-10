//
// Created by Phil on 29.05.2021.
//

import SwiftUI
import SharedMobile

struct AboutScreen: View {
    let state: AboutFeature.State
    let listener: (AboutFeature.Msg) -> Void

    var body: some View {
        NavigationBar(
            title: "About",
            leftItem: NavigationBarItem(view: Image(systemName: "chevron.left")) {
                listener(AboutFeature.MsgBack())
            }
        )
        VStack {
            ForEachIndexed(state.teamMembers) { _, teamMember in
                TeamMemberCell(user: teamMember) {
                    listener(AboutFeature.MsgOpenTwitter(teamMember: teamMember))
                }
            }
            Text(state.text)
                .textStyle(.body1)
            Spacer()
        }.padding()
    }
}

private struct TeamMemberCell: View {
    let user: AboutFeature.StateTeamMember
    let onTwitterSelect: () -> Void

    var body: some View {
        HStack {
            ProfileImage(image: user.image)
                .frame(size: 45)
                .padding(.trailing)
            VStack(alignment: .leading) {
                Text(user.name)
                    .textStyle(.caption)
                Text(user.position)
                    .textStyle(.captionLight)
            }
            Spacer()
            Button(action: onTwitterSelect) {
                Image("twitter")
                    .foregroundColorKMM(.companion.LightBlue)
                    .padding()
            }
        }.padding().background(Color.white)
    }
}

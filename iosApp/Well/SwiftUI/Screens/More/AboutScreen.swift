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
                .style(.body1)
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
                    .style(.caption)
                Text(user.position)
                    .style(.captionLight)
            }
            Spacer()
            Button(action: onTwitterSelect) {
                Image(uiImage: R.image.twitter()!)
                    .foregroundColorKMM(ColorConstants.LightBlue)
                    .padding()
            }
        }.padding().background(Color.white)
    }
}

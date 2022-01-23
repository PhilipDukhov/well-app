//
// Created by Phil on 29.05.2021.
//

import SwiftUI
import SharedMobile

private typealias Feature = AboutFeature

struct AboutScreen: View {
    let state: AboutFeature.State
    let listener: (AboutFeature.Msg) -> Void

    var body: some View {
        NavigationBar(
            title: state.title,
            leftItem: .back {
                listener(Feature.MsgBack())
            }
        )
        VStack {
            ForEachIndexed(state.teamMembers) { _, teamMember in
                TeamMemberCell(user: teamMember) {
                    listener(Feature.MsgOpenTwitter(teamMember: teamMember))
                }
            }
            Text(state.text)
                .textStyle(.body1)
            Spacer()
        }.padding()
    }
}

private struct TeamMemberCell: View {
    let user: Feature.StateTeamMember
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

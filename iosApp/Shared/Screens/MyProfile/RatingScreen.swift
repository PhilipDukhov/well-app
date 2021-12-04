//
// Created by Phil on 18.05.2021.
//

import SwiftUI
import SharedMobile

struct RatingScreen: View {
    let user: User
    let send: (Rating) -> Void

    @Environment(\.presentationMode)
    private var presentationMode
    @State
    private var selectedRating: Int
    @State
    private var text: String

    init(
        user: User,
        send: @escaping (Rating) -> Void
    ) {
        self.user = user
        self.send = send
        _selectedRating = .init(wrappedValue: user.ratingInfo.currentUserRating?.value.toInt() ?? 0)
        _text = .init(wrappedValue: user.ratingInfo.currentUserRating?.text ?? "")
    }

    var body: some View {
        let profileImageSize = UIScreen.main.bounds.width * 0.42
        ZStack {
            VStack {
                NavigationBar(
                    rightItem: .cancel(
                        handler: {
                            presentationMode.wrappedValue.dismiss()
                        }
                    ),
                    minContentHeight: profileImageSize / 2 + 20
                )
                Spacer()
            }
            VStack {
                ProfileImage(user)
                    .frame(size: profileImageSize)
                Spacer().frame(height: 18)
                ScrollView {
                    Text(user.ratingInfo.currentUserRating != nil ? "Update your review" : "Please write a review about")
                        .textStyle(.body2)
                    Text(user.fullName)
                        .textStyle(.h4)
                    Spacer().frame(height: 20)
                    starSelector
                    Spacer().frame(height: 50)
                    ProjectTextEditor(placeholder: "Tell us about your experience", text: $text)
                    Spacer().frame(minHeight: 70)
                    Button {
                        send(Rating(value: Int32(selectedRating), text: text))
                    } label: {
                        Text("Send")
                    }
                        .disabled(selectedRating == 0)
                        .buttonStyle(ActionButtonStyle(style: .onWhite))
                }
            }.padding()
        }
    }

    @ViewBuilder
    private var starSelector: some View {
        HStack {
            ForEach(1...5, id: \.self) { star in
                let selected = star <= selectedRating
                Button {
                    selectedRating = star
                } label: {
                    Image(systemName: selected ? "star.fill" : "star")
                        .font(.system(size: 27))
                        .foregroundColorKMM(selected ? .companion.Green : .companion.LightGray)
                }
            }
        }
    }
}

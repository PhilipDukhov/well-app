//
//  ProfileScreen.swift
//  Well
//
//  Created by Philip Dukhov on 2/11/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = MyProfileFeature

struct MyProfileScreen: View {
    let state: MyProfileFeature.State
    let listener: (MyProfileFeature.Msg) -> Void

    @State
    private var editingRating = false
    
    @State
    private var selectedTab: Feature.ProfileTab = .profileinformation
    
    var body: some View {
        VStack(spacing: 0) {
            content
        }
        .partialSheet(
            item: state.requestConsultationState,
            onDismiss: {
                listener(Feature.MsgRequestConsultationMsg(msg: RequestConsultationFeature.MsgClose()))
            },
            sheetContent: {
                RequestConsultationView(state: $0) {
                    listener(Feature.MsgRequestConsultationMsg(msg: $0))
                }
            },
            overlayContent: RequestConsultationOverlay.init
        )
    }
    
    @ViewBuilder
    var content: some View {
        state.navigationBarModelForTab(tab: selectedTab).map {
            ModeledNavigationBar(
                model: $0,
                listener: listener,
                extraContent: {
                    if state.tabs.count > 1 {
                        TabRow(
                            tabs: state.tabs,
                            selection: $selectedTab,
                            selectionColor: .companion.White
                        ) { tab in
                            Text(tab.title)
                                .textStyle(.body2)
                                .padding(.bottom, 8)
                                .padding(.top, 2)
                                .padding(.horizontal, 4)
                        }.padding(.top)
                    }
                }
            )
        }
        if !state.loaded {
            ProgressView()
        }
        if state.tabs.count > 1 {
            PageView(selection: $selectedTab, pages: state.tabs) {tab in
                switch tab {
                case .profileinformation:
                    profileInformation()

                case .availability:
                    state.availabilityState.map {
                        AvailabilitiesCalendarView(state: $0) {
                            listener(Feature.MsgAvailabilityMsg(msg: $0))
                        }
                    }
                    
                default:
                    fatalError("Unexpected tab")
                }
            }
        } else {
            profileInformation()
        }
    }

    @ViewBuilder
    func profileInformation() -> some View{
        ZStack(alignment: .topLeading) {
            ScrollView {
                ForEach(state.groups, id: \.self) { group in
                    groupView(group)
                    Divider()
                }
            }.edgesIgnoringSafeArea(state.isCurrent ? Edge.Set() : .top)
            if !state.isCurrent {
                Control(Image(systemName: "chevron.left").foregroundColor(.white).shadow(color: .black, radius: 3.5)) {
                    listener(Feature.MsgBack())
                }
            }
            if state.editingStatus == .uploading {
                InactiveOverlay()
            }
        }.sheet(isPresented: $editingRating) {
            state.user.map { user in
                RatingScreen(user: user) { rating in
                    listener(Feature.MsgRate(rating: rating))
                    editingRating = false
                }
            }
        }
    }

    @ViewBuilder
    private func groupView(_ group: UIGroup) -> some View {
        let paddingNeeded = !(group is UIGroup.Header) || state.isCurrent
        VStack(alignment: .leading, spacing: 0) {
            switch group {
            case let header as UIGroup.Header:
                if state.isCurrent {
                    currentUserHeader(header)
                } else {
                    otherUserHeader(header)
                }

            case let group as UIGroup.Preview:
                ForEach(group.fields, id: \.self) { field in
                    HStack {
                        previewField(field)
                        Spacer()
                    }
                        .padding(.vertical, 5)
                }

            case let group as UIGroup.Editing:
                Text(group.title)
                    .foregroundColorKMM(.companion.LightBlue)
                ForEach(group.fields, id: \.self) { field in
                    EditingField(field as! UIEditingField<UIEditingFieldContent, Feature.Msg>,
                        listener: listener)
                        .fillMaxWidth()
                        .padding(.vertical, 7)
                }

            default: fatalError()
            }
        }.padding(.horizontal, paddingNeeded ? nil : 0)
    }

    private func previewField(_ field: UIPreviewField) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            if field.title.isNotEmpty {
                Text(field.title)
                    .foregroundColor(.gray)
                    .padding(.bottom, 5)
            }

            switch field.content {
            case let content as UIPreviewField.ContentText:
                Text(content.text)

            case let content as UIPreviewField.ContentList:
                HVStack(items: content.list) { item in
                    Text(item)
                        .padding(.vertical, 5)
                        .padding(.horizontal, 10)
                        .font(.body)
                        .backgroundColorKMM(
                            .companion.LightBlue
                                .withAlpha(alpha: 0.15)
                        )
                        .foregroundColor(Color.black)
                        .clipShape(Capsule())
                }

            case let content as UIPreviewField.ContentTextAndIcon:
                HStack {
                    Image("profile/\(content.icon.name.lowercased())")
                        .foregroundColorKMM(.companion.LightBlue)
                    Text(content.text)
                        .onTapGesture {
                            if content.isLink {
                                listener(Feature.MsgOpenUrl(url: content.text))
                            }
                        }
                }

            case let content as UIPreviewFieldContentButton<Feature.Msg>:
                Button {
                    listener(content.msg!)
                } label: {
                    Text(content.title)
                }.buttonStyle(ActionButtonStyle(style: .onWhite))

            default: fatalError("view not provided for \(field.content)")
            }
        }
    }

    private func currentUserHeader(_ header: UIGroup.Header) -> some View {
        HStack(spacing: 0) {
            if let image = header.image {
                ProfileImage(image: image)
                    .frame(size: 100)
                    .padding(.trailing)
            } else {
                Spacer()
            }
            VStack(alignment: .leading, spacing: 7) {
                if let name = header.name {
                    Text(name)
                } else {
                    Text(header.initiateImageUpdateText)
                        .foregroundColorKMM(.companion.LightBlue)
                        .onTapGesture {
                            listener(Feature.MsgInitiateImageUpdate())
                        }
                }
                header.accountType.map { accountType in
                    HStack {
                        accountType.imageView()
                            .font(.system(size: 14, weight: .black))
                        Text(accountType.title)
                    }.foregroundColorKMM(.companion.LightBlue)
                }
                if let completeness = header.completeness {
                    Text("Profile \(completeness)% complete")
                        .foregroundColor(Color.gray)
                        .opacity(Int(truncating: completeness) < 100 ? 1 : 0)
                }
            }
            Spacer()
        }.padding(.vertical)
    }

    private func otherUserHeader(_ header: UIGroup.Header) -> some View {
        VStack {
            let profileImageWidth = UIScreen.main.bounds.width
            ProfileImage(image: header.image, clipCircle: false, aspectRatio: 1.2, contentMode: .fill)
                .frame(size: CGSize(width: profileImageWidth, height: profileImageWidth / 1.2))
                .clipped()
            HStack {
                header.accountType.map {
                    Text($0.description)
                }
                Spacer()
                ToggleFavoriteButton(favorite: header.favorite) {
                    listener(Feature.MsgToggleFavorite())
                }
                header.twitterLink.map { link in
                    Button {
                        listener(Feature.MsgOpenUrl(url: link))
                    } label: {
                        Image("profile/twitter")
                            .foregroundColorKMM(.companion.LightBlue)
                            .padding()
                    }
                }
                Button {
                    listener(Feature.MsgMessage())
                } label: {
                    Image(systemName: "message.fill")
                        .font(.system(size: 20))
                        .foregroundColorKMM(.companion.Green)
                        .padding()
                }
                Button {
                    listener(Feature.MsgCall())
                } label: {
                    Image(systemName: "phone.fill")
                        .font(.system(size: 20))
                        .foregroundColorKMM(.companion.Green)
                        .padding()
                }
            }.padding()
            header.nameWithCredentials.map {
                Text($0)
                    .textStyle(.h4)
            }
            RatingInfoView(ratingInfo: header.ratingInfo) {
                editingRating = true
            }.padding()
        }
    }
}

private extension User.Type_ {
    func imageView() -> some View {
        switch self {
        case .doctor, .pendingexpert:
            return Image(systemName: "plus")

        case .expert:
            return Image(systemName: "plus")

        default: fatalError()
        }
    }
}

extension User.RatingInfo: Identifiable {
    public var id: String {
        "\(String(describing: currentUserRating?.value))"
    }
}

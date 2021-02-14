//
//  ProfileScreen.swift
//  Well
//
//  Created by Philip Dukhov on 2/11/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile
import Introspect

private var observation: NSKeyValueObservation?

struct MyProfileScreen: View {
    let state: MyProfileFeature.State
    let listener: (MyProfileFeature.Msg) -> Void
    
    var body: some View {
        state.navigationBarModel.map {
            ModeledNavigationBar(model: $0, listener: listener)
        }
        ZStack(alignment: .topLeading) {
            ScrollView {
                ForEach(state.groups, id: \.self) { group in
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
                                .foregroundColorKMM(ColorConstants.HippieBlue)
                            ForEach(group.fields, id: \.self) { field in
                                EditingField(field, listener: listener)
                                    .fillMaxWidth()
                                    .padding(.vertical, 7)
                            }
                            
                        default: fatalError()
                        }
                    }.padding(.horizontal, paddingNeeded ? nil : 0)
                    Divider()
                }
            } // ScrollView
            .edgesIgnoringSafeArea(state.isCurrent ? Edge.Set() : .top)
            Control(Image(systemName: "chevron.left").foregroundColor(.white)) {
                listener(MyProfileFeature.MsgBack())
            }
        }
    }
    
    private func previewField(_ field: UIPreviewField) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(field.title)
                .foregroundColor(.gray)
                .padding(.bottom, 5)
            
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
                            ColorConstants.HippieBlue
                                .withAlpha(alpha: 0.15)
                        )
                        .foregroundColor(Color.black)
                        .clipShape(RoundedRectangle(cornerRadius: .greatestFiniteMagnitude))
                }
                
            case let content as UIPreviewField.ContentTextAndIcon:
                HStack {
                    Image(uiImage: content.icon.uiImage())
                        .foregroundColorKMM(ColorConstants.HippieBlue)
                    Text(content.text)
                        .onTapGesture {
                            if content.isLink {
                                
                            }
                        }
                }
                
            default: fatalError()
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
                        .foregroundColorKMM(ColorConstants.LightBlue)
                        .onTapGesture {
                            listener(MyProfileFeature.MsgInitiateImageUpdate())
                        }
                }
                header.accountType.map { $0.view() }
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
            ProfileImage(image: header.image, clipCircle: false)
                .fillMaxWidth()
                .aspectRatio(1.2, contentMode: .fit)
            HStack {
                header.accountType.map { Text($0.description) }
                Spacer()
                header.twitterLink.map { link in
                    Image(uiImage: R.image.profile.twitter()!)
                        .foregroundColorKMM(ColorConstants.LightBlue)
                        .onTapGesture {
                            listener(MyProfileFeature.MsgOpenUrl(url: link))
                        }
                }
                Image(systemName: "phone.fill")
                    .font(.system(size: 20))
                    .foregroundColorKMM(ColorConstants.Green)
                    .padding()
                    .onTapGesture {
                        listener(MyProfileFeature.MsgCall())
                    }
            }.padding()
            header.nameWithCredentials.map {
                Text($0)
                .style(.h4)
            }
        }
    }
}

private extension UIPreviewField.Icon {
    func uiImage() -> UIImage {
        switch self {
        case .location:
            return R.image.profile.location()!
        case .publications:
            return R.image.profile.publications()!
        case .twitter:
            return R.image.profile.twitter()!
        case .doximity:
            return R.image.profile.doximity()!
        default: fatalError()
        }
    }
}

private extension User.Type_ {
    func view() -> some View {
        let image: Image
        switch self {
        case .doctor:
            image = Image(systemName: "plus")
            
        case .expert:
            image = Image(systemName: "plus")
            
        default: fatalError()
        }
        return HStack {
            image.font(.system(size: 14, weight: .black))
            Text(description())
        }.foregroundColorKMM(ColorConstants.HippieBlue)
    }
}

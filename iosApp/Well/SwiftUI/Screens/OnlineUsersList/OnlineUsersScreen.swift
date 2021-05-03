//
//  OnlineUsersScreen.swift
//  Well
//
//  Created by Philip Dukhov on 12/3/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile
import Combine

struct OnlineUsersScreen: View {
    let state: OnlineUsersFeature.State
    let listener: (OnlineUsersFeature.Msg) -> Void
    
    @State
    private var searchText = ""

    var body: some View {
        NavigationBar(
            title: state.connectionStatus.stringRepresentation,
            leftItem: NavigationBarItem(
                view: Text("Log out"),
                handler: listener(OnlineUsersFeature.MsgOnLogout())
            ),
            rightItem: NavigationBarItem(
                view: Image(systemName: state.filter.favorite ? "suit.heart.fill" : "heart")
                    .font(.system(size: 25))
                    .foregroundColorKMM(ColorConstants.White),
                handler: listener(OnlineUsersFeature.MsgToggleFilterFavorite())
            )
        )
        SearchBar(text: $searchText)
            .fillMaxWidth()
            .padding()
            
        ScrollView {
            if !state.users.isEmpty {
                Divider()
            }
            Rectangle()
                .foregroundColor(.white)
                .frame(height: 1)
            VStack {
                ForEach(state.users, id: \.id) { user in
                    UserCell(viewModel: user) {
                        listener(OnlineUsersFeature.MsgOnUserSelected(user: user))
                    } onCallButtonTap: {
                        listener(OnlineUsersFeature.MsgOnUserFavorite(user: user))
                    }
                    Divider()
                }
            }
        }
        .onChange(of: searchText) { searchText in
            listener(OnlineUsersFeature.MsgSetSearchString(searchString: searchText))
        }
        CustomTabBar(selected: 1, onAccountClick: {
            listener(OnlineUsersFeature.MsgOnCurrentUserSelected())
        }).padding()
    }
}

struct CustomTabBar: View {
    struct Item {
        enum ImageContent {
            case systemName(String)
            case uiImage(UIImage)
        }
        let imageContent: ImageContent
        let title: String
        let action: () -> Void
        
        @ViewBuilder
        var image: some View {
            switch imageContent {
            case .systemName(let name):
                Image(systemName: name).font(.system(size: 30))
            case .uiImage(let image):
                Image(uiImage: image)
                    .resizable()
                    .frame(width: 30, height: 26)
            }
        }
    }
    
    let items: [Item]
    let selected: Int
    init(selected: Int, onAccountClick: @escaping () -> Void = {}, onExpertsClick: @escaping () -> Void = {}) {
        self.selected = selected
        items = [
            Item(imageContent: .systemName("person.fill"), title: "My Profile", action: onAccountClick),
            Item(imageContent: .uiImage(R.image.profile.expert()!), title: "Experts", action: onExpertsClick),
            Item(imageContent: .systemName("message.fill"), title: "Messages", action: {}),
            Item(imageContent: .systemName("bell.fill"), title: "Notice", action: {}),
        ]
    }
    
    var body: some View {
        VStack(spacing: 10) {
            Divider()
            HStack {
                ForEachEnumerated(items) { i, item in
                    Button(action: item.action) {
                        VStack {
                            item.image
                            Text(item.title)
                                .font(.system(size: 14))
                        }
                        .foregroundColor(i == selected ? SwiftUI.Color(hex: "1B3D6D") : ColorConstants.LightGray.toColor())
                    }
                    if i != items.count {
                        Spacer()
                    }
                }
            }
        }
    }
}

struct ForEachEnumerated<Data: RandomAccessCollection, Content: View>: View {
    var data: [EnumeratedSequence<Data>.Element]
    var content: (Int, Data.Element) -> Content
    
    init(_ data: Data, @ViewBuilder content: @escaping (Int, Data.Element) -> Content) {
        self.data = Array(data.enumerated())
        self.content = content
    }
    
    var body: some View {
        ForEach(data, id: \.offset) { element in
            content(element.offset, element.element)
        }
    }
}

extension SwiftUI.Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }
        
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

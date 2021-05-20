//
//  ExpertsScreen.swift
//  Well
//
//  Created by Philip Dukhov on 12/3/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile
import Combine

struct ExpertsScreen: View {
    let state: ExpertsFeature.State
    let listener: (ExpertsFeature.Msg) -> Void

    @State
    private var searchText = ""

    @State
    private var filterPresented = false

    var body: some View {
        NavigationBar(
            title: state.connectionStatus.stringRepresentation,
            leftItem: NavigationBarItem(
                view: HStack {
                    Image(systemName: "line.horizontal.3.decrease.circle")
                    Text("Filter")
                }.style(.title2),
                handler: {
                    filterPresented = true
                }
            ),
            rightItem: NavigationBarItem(
                view: Image(systemName: state.filterState.filter.favorite ? "suit.heart.fill" : "heart")
                    .font(.system(size: 25))
                    .foregroundColorKMM(ColorConstants.White)
            ) {
                listener(ExpertsFeature.MsgToggleFilterFavorite())
            }
        ).sheet(isPresented: $filterPresented) {
            FilterScreen(state: state.filterState) { msg in
                listener(ExpertsFeature.MsgFilterMsg(msg: msg))
            } hide: {
                filterPresented = false
            }
        }
        SearchBar(text: $searchText)
            .fillMaxWidth()
            .padding()
            .onChange(of: searchText) { searchText in
                listener(ExpertsFeature.MsgSetSearchString(searchString: searchText))
            }
        if state.updating {
            ProgressView()
            Spacer()
        } else if state.users.isEmpty {
            Text("Тo users satisfying the filter")
            Spacer()
        } else {
            ScrollView {
                if !state.users.isEmpty {
                    Divider()
                }
                Rectangle()
                    .foregroundColor(.white)
                    .frame(height: 1)
                VStack {
                    ForEach(state.users, id: \.id) { user in
                        UserCell(user: user) {
                            listener(ExpertsFeature.MsgOnUserSelected(user: user))
                        } toggleFavorite: {
                            listener(ExpertsFeature.MsgOnUserFavorite(user: user))
                        }
                        Divider()
                    }
                }
            }
        }
        CustomTabBar(selected: 1, onAccountClick: {
            listener(ExpertsFeature.MsgOnCurrentUserSelected())
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

    init(selected: Int, onAccountClick: @escaping () -> Void = {
    }, onExpertsClick: @escaping () -> Void = {
    }) {
        self.selected = selected
        items = [
            Item(imageContent: .systemName("person.fill"), title: "My Profile", action: onAccountClick),
            Item(imageContent: .uiImage(R.image.profile.expert()!), title: "Experts", action: onExpertsClick),
            Item(imageContent: .systemName("message.fill"), title: "Messages", action: {
            }),
            Item(imageContent: .systemName("bell.fill"), title: "Notice", action: {
            }),
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
                            .foregroundColor(i == selected ? SwiftUI.Color(hex: 0x1B3D6D) : ColorConstants.LightGray.toColor())
                    }
                    if i != items.count {
                        Spacer()
                    }
                }
            }
        }
    }
}

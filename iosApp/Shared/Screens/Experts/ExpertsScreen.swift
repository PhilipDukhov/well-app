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

private typealias Feature = ExpertsFeature

struct ExpertsScreen: View {
    let state: ExpertsFeature.State
    let listener: (ExpertsFeature.Msg) -> Void

    @State
    private var searchText = ""

    @State
    private var filterPresented = false

    var body: some View {
        NavigationBar(
            title: state.connectionStatusDescription,
            leftItem: NavigationBarItem(
                view: HStack {
                    Image(systemName: "line.horizontal.3.decrease.circle")
                    Text(Feature.Strings.shared.filter)
                }.textStyle(.subtitle2),
                handler: {
                    filterPresented = true
                }
            ),
            rightItem: NavigationBarItem(
                view: Image(systemName: state.filterState.filter.favorite ? "suit.heart.fill" : "heart")
                    .font(.system(size: 25))
                    .foregroundColorKMM(.companion.White)
            ) {
                listener(Feature.MsgToggleFilterFavorite())
            }
        ).sheet(isPresented: $filterPresented) {
            FilterScreen(state: state.filterState) { msg in
                listener(Feature.MsgFilterMsg(msg: msg))
            } hide: {
                filterPresented = false
            }
        }
        SearchBar(text: $searchText)
            .fillMaxWidth()
            .padding()
            .onChange(of: searchText) { searchText in
                listener(Feature.MsgSetSearchString(searchString: searchText))
            }
        if state.updating {
            ProgressView()
            Spacer()
        } else if state.users.isEmpty {
            Text("No users satisfying the filter")
            Spacer()
        } else {
            ScrollView {
                if !state.users.isEmpty {
                    Divider()
                }
                Rectangle()
                    .foregroundColor(.white)
                    .frame(height: 1)
                LazyVStack {
                    ForEach(state.users, id: \.id) { user in
                        UserCell(user: user) {
                            listener(Feature.MsgOnUserFavorite(user: user))
                        }.onTapGesture {
                            listener(Feature.MsgOnUserSelected(user: user))
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

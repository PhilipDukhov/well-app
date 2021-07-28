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
                }.style(.subtitle2),
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
                VStack {
                    ForEach(state.users, id: \.id) { user in
                        UserCell(user: user) {
                            listener(ExpertsFeature.MsgOnUserFavorite(user: user))
                        }.onTapGesture {
                            listener(ExpertsFeature.MsgOnUserSelected(user: user))
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

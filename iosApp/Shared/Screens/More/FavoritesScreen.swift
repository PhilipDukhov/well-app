//
//  FavoritesScreen.swift
//  Well
//
//  Created by Phil on 12.02.2022.
//  Copyright © 2022 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = FavoritesFeature

struct FavoritesScreen: View {
    let state: FavoritesFeature.State
    let listener: (FavoritesFeature.Msg) -> Void

    var body: some View {
        let filterString = state.filterString
        NavigationBar(
            title: filterString == nil ? state.title : nil,
            leftItem: filterString == nil ? .back { listener(Feature.MsgBack()) } : nil,
            rightItem: NavigationBarItem(view: searchField())
        )
        UsersList(
            users: state.users,
            onSelect: {
                listener(Feature.MsgOnUserSelected(user: $0))
            },
            onToggleFavorite: {
                listener(Feature.MsgOnUserFavorite(user: $0))
            }
        )
    }

    @ViewBuilder
    func searchField() -> some View {
        let filterString = state.filterString
        let editing = filterString != nil
        let forcedSize = !editing ? controlMinSize : nil
        HStack(spacing: 0) {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.white)
                .frame(size: controlMinSize)
                .offset(x: forcedSize ?? 0)
            TextField(
                editing ? "Search" : "",
                text: .init(get: { filterString ?? "" }, set: { listener(Feature.MsgUpdateFilterString(filterString: $0)) })
            )
                .autocapitalization(.none)
                .disableAutocorrection(true)
                .foregroundColor(.white)
                .frame(size: forcedSize)
                .onTapGesture {
                    listener(Feature.MsgUpdateFilterString(filterString: ""))
                }
            Button {
                listener(Feature.MsgUpdateFilterString(filterString: nil))
                UIApplication.shared.endEditing()
            } label: {
                Image(systemName: "xmark.circle.fill")
                    .foregroundColor(.white)
            }.opacity(editing ? 1 : 0)
                .offset(x: -(forcedSize ?? 0))
                .frame(size: controlMinSize)
        }
        .background(RoundedRectangle(cornerRadius: 10).foregroundColor(.white).opacity(editing ? 0.2 : 0))
        .frame(width: forcedSize, height: controlMinSize)
        .animation(.default, value: editing)
    }
}

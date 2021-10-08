//
//  FilterScreen.swift
//  Well
//
//  Created by Phil on 06.05.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SharedMobile
import SwiftUI

struct FilterScreen: View {
    let state: FilterFeature.State
    let listener: (FilterFeature.Msg) -> Void
    let hide: () -> Void

    var body: some View {
        NavigationBar(title: HStack {
            Image(systemName: "line.horizontal.3.decrease.circle")
            Text("Filter")
        })
        ScrollView {
            HStack {
                Text("Sort by")
                SelectableHVStack(
                    items: UsersFilter.SortByCompanion().allCases,
                    selectedIndex: .init(get: {
                        Int(state.sortByIndex)
                    }, set: {
                        listener(FilterFeature.MsgSetSortByIndex(index: Int32($0)))
                    })
                ) { item, _ in
                    Text("\(item.name)")
                        .padding(.vertical, 5)
                        .padding(.horizontal, 10)
                }
            }.style(.body1Light).padding()
            Divider()
            ForEach(state.fields, id: \.self) { field in
                EditingField1(field, listener: listener)
                    .padding(.vertical, 7).padding(.horizontal)
                Divider()
            }
            HStack {
                Text("Rating")
                    .style(.body1).padding()
                Spacer()
            }
            SelectableHVStack(
                items: UsersFilter.RatingCompanion().allCases,
                selectedIndex: .init(get: {
                    Int(state.ratingIndex)
                }, set: {
                    listener(FilterFeature.MsgSetRatingIndex(index: Int32($0)))
                })
            ) { item, selected in
                HStack {
                    Text("\(item.title)")
                        .style(.body1Light)
                    if item != UsersFilter.Rating.all {
                        Image(systemName: "star.fill")
                            .foregroundColorKMM(selected ? ColorConstants.White : ColorConstants.LightGray)
                    }
                }
                    .padding(.vertical, 5)
                    .padding(.horizontal, 10)
            }.padding()
            HStack {
                Toggle(isOn: .init(get: {
                    state.filter.withReviews
                }, set: { _ in
                    listener(FilterFeature.MsgToggleWithReviews())
                })) {
                    Text("With reviews")
                }
            }.padding()
            Button {
                listener(FilterFeature.MsgShow())
                hide()
            } label: {
                Text("Show")
            }.buttonStyle(ActionButtonStyle(style: .onWhite))
            Button {
                listener(FilterFeature.MsgClear())
            } label: {
                Text("clear all")
            }.buttonStyle(ActionButtonStyle(style: .white))
        }
    }
}

private struct EditingField1<Msg: AnyObject>: View {
    let field: UIEditingField<UIEditingFieldContentList<AnyObject>, Msg>
    let listener: (Msg) -> Void
    @State
    private var text: String
    @State private var showModal = false

    init(_ field: UIEditingField<UIEditingFieldContentList<AnyObject>, Msg>, listener: @escaping (Msg) -> Void) {
        self.field = field
        self.listener = listener

        _text = State(initialValue: field.content.description)
    }

    var body: some View {
        Button {
            showModal = true
        } label: {
            HStack {
                Text(field.placeholder)
                    .foregroundColorKMM(ColorConstants.Black)
                Spacer()
                Image(systemName: "chevron.right")
                    .foregroundColorKMM(ColorConstants.LightGray)
            }.style(.body1)
                .padding(.vertical, 7)
        }
            .sheet(isPresented: $showModal) {
                selectionScreen()
            }
    }

    @ViewBuilder
    private func selectionScreen() -> some View {
        FilterSelectionScreen(
            title: field.placeholder,
            selection: field.content.selectionIndices,
            variants: field.content.itemDescriptions,
            multipleSelection: field.content.multipleSelectionAvailable,
            onSelectionChanged: { newSelection in
                Napier.i(newSelection, field.content.doCopy(selectionIndices: newSelection))
                listener(
                    field.updateMsg(
                        field.content.doCopy(selectionIndices: newSelection)
                    )!
                )
                showModal = false
            },
            onBack: {
                showModal = false
            }
        )
    }
}

private struct FilterSelectionScreen: View {
    let title: String
    @State var selection: Set<KotlinInt>
    let variants: [String]
    let multipleSelection: Bool
    let onSelectionChanged: (Set<KotlinInt>) -> Void
    let onBack: () -> Void

    var body: some View {
        NavigationBar(
            title: title,
            leftItem: NavigationBarItem(view: Image(systemName: "chevron.left")) {
                onSelectionChanged(selection)
            },
            rightItem: { () -> NavigationBarItem<Text>? in
                guard multipleSelection else {
                    return nil
                }
                let allVariants = Set(Array(variants.indices).map(KotlinInt.init(integerLiteral:)))
                let allVariantsSelected = selection == allVariants
                    return NavigationBarItem(text: allVariantsSelected ? "Deselect all" : "Select all") {
                    selection = allVariantsSelected ? Set() : allVariants
                }
            }()
        )
        SelectionScreen(
            selection: $selection,
            variants: variants,
            multipleSelection: multipleSelection,
            onSelectionChanged: onSelectionChanged
        )
    }
}

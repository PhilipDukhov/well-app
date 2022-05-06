//
//  EditingField.swift
//  Well
//
//  Created by Philip Dukhov on 2/12/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct EditingField<Msg: AnyObject>: View {
    let field: UIEditingField<UIEditingFieldContent, Msg>
    let listener: (Msg) -> Void

    private let isTextField: Bool
    private let fieldContent: UIEditingFieldContent

    @State private var textEditing = false
    @State private var showModal = false

    init(_ field: UIEditingField<UIEditingFieldContent, Msg>, listener: @escaping (Msg) -> Void) {
        self.field = field
        self.listener = listener
        fieldContent = field.content
        switch fieldContent {
        case is UIEditingFieldContentList<AnyObject>:
            isTextField = false

        default:
            isTextField = true
        }
    }

    var body: some View {
        if isTextField {
            content()
        } else {
            Button(
                action: {
                    showModal = true
                },
                label: content
            ).sheet(isPresented: $showModal) {
                selectionScreen()
            }
        }
    }

    @ViewBuilder
    private func content() -> some View {
        let text = field.description()
        HStack {
            if isTextField {
                TextField(
                    field.placeholder,
                    text: .init(get: { text }, set: { textContentUpdated($0) })
                ) { editing in
                    textEditing = editing
                }
                Spacer()
            } else {
                Text(text.isEmpty ? field.placeholder : text)
                    .foregroundColorKMM(
                        text.isEmpty ? .companion.LightBlue : .companion.Black
                    )
                Spacer()
                Image(systemName: "chevron.down")
                    .foregroundColorKMM(.companion.LightBlue)
            }
        }.padding()
            .overlay(
                Shapes.medium
                    .strokeColorKMM(
                        fieldContent.valid() || textEditing ? .companion.LightBlue : .companion.RadicalRed,
                        lineWidth: 2
                    )
            )
    }

    private func textContentUpdated(_ text: String) {
        guard fieldContent.description != text else {
            return
        }
        switch fieldContent {
        case let fieldContent as UIEditingFieldContent.Text:
            listener(
                field.updateMsg(
                    fieldContent.doCopy(text: text)
                )!
            )
        case let fieldContent as UIEditingFieldContent.Email:
            listener(
                field.updateMsg(
                    fieldContent.doCopy(email: text)
                )!
            )

        default: fatalError()
        }
    }

    private func selectionScreen() -> some View {
        switch fieldContent {
        case let fieldContent as UIEditingFieldContentList<AnyObject>:
            return ProfileSelectionScreen(
                title: field.placeholder,
                selection: fieldContent.selectionIndices,
                variants: fieldContent.itemDescriptions,
                multipleSelection: fieldContent.multipleSelectionAvailable,
                onSelectionChanged: { newSelection in
                    listener(
                        field.updateMsg(
                            fieldContent.doCopy(selectionIndices: newSelection)
                        )!
                    )
                    showModal = false
                },
                onCancel: {
                    showModal = false
                }
            )

        default: fatalError()
        }
    }
}

private struct ProfileSelectionScreen: View {
    let title: String
    @State var selection: Set<KotlinInt>
    let variants: [String]
    let multipleSelection: Bool
    let onSelectionChanged: (Set<KotlinInt>) -> Void
    let onCancel: () -> Void

    var body: some View {
        NavigationBar(
            title: title,
			leftItem: NavigationBarItem(text: GlobalStringsBase.companion.shared.cancel, handler: onCancel),
            rightItem: !multipleSelection ? nil :
				NavigationBarItem(text: GlobalStringsBase.companion.shared.done) {
                           onSelectionChanged(selection)
                       }
        )
        SelectionScreen(
            selection: $selection,
            variants: variants,
            multipleSelection: multipleSelection,
            onSelectionChanged: onSelectionChanged
        )
    }
}

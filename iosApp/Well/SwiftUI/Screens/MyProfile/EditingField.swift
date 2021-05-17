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

    @State private var text: String
    @State private var textEditing = false
    @State private var showModal = false

    init(_ field: UIEditingField<UIEditingFieldContent, Msg>, listener: @escaping (Msg) -> Void) {
        self.field = field
        self.listener = listener
        fieldContent = field.content as! UIEditingFieldContent
        switch fieldContent {
        case is UIEditingFieldContentList<AnyObject>:
            isTextField = false

        default:
            isTextField = true
        }
        _text = State(initialValue: fieldContent.description)
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
        HStack {
            if let image = fieldContent.icon?.image {
                Image(uiImage: image)
                    .foregroundColorKMM(ColorConstants.Silver)
            }
            if isTextField {
                TextField(
                    field.placeholder,
                    text: $text
                ) { editing in
                    textEditing = editing
                    guard !editing else {
                        return
                    }
                    textContentUpdated()
                }
                Spacer()
            } else {
                Text(text.isEmpty ? field.placeholder : text)
                    .foregroundColorKMM(
                        text.isEmpty ? ColorConstants.Silver : ColorConstants.Black
                    )
                Spacer()
                Image(systemName: "chevron.down")
                    .foregroundColorKMM(ColorConstants.Silver)
            }
        }.padding()
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(
                        (fieldContent.valid() || textEditing ? ColorConstants.Silver : ColorConstants.RadicalRed).toColor(),
                        lineWidth: 2
                    )
            )
    }

    private func textContentUpdated() {
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
//        case let fieldContent as UIEditingFieldContent.Location:
//            listener(
//                field.updateMsg(
//                    fieldContent.doCopy(location: text)
//                )
//            )
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

fileprivate struct ProfileSelectionScreen: View {
    let title: String
    @State var selection: Set<KotlinInt>
    let variants: [String]
    let multipleSelection: Bool
    let onSelectionChanged: (Set<KotlinInt>) -> Void
    let onCancel: () -> Void

    var body: some View {
        NavigationBar(
            title: title,
            leftItem: NavigationBarItem(text: "Cancel", handlerOpt: onCancel),
            rightItem: !multipleSelection ? nil :
                       NavigationBarItem(text: "Done", handler: onSelectionChanged(selection))
        )
        SelectionScreen(
            selection: $selection,
            variants: variants,
            multipleSelection: multipleSelection,
            onSelectionChanged: onSelectionChanged
        )
    }
}

extension UIEditingFieldContent.Icon {
    var image: UIImage {
        switch self {
        case .location:
            return R.image.profile.location()!

        default: fatalError()
        }
    }
}

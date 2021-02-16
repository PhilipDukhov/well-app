//
//  EditingField.swift
//  Well
//
//  Created by Philip Dukhov on 2/12/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct EditingField: View {
    let field: UIEditingField<AnyObject>
    let listener: (MyProfileFeature.Msg) -> Void
    
    private let isTextField: Bool
    private let content: UIEditingFieldContent
    
    @State private var text: String
    @State private var textEditing = false
    @State private var showModal = false
    
    init(_ field: UIEditingField<AnyObject>, listener: @escaping (MyProfileFeature.Msg) -> Void) {
        self.field = field
        self.listener = listener
        content = field.content as! UIEditingFieldContent
        switch content {
        case is UIEditingFieldContent.List:
            isTextField = false
            
        default:
            isTextField = true
        }
        _text = State(initialValue: content.description)
    }
    
    var body: some View {
        HStack {
            if let image = content.icon?.image {
                Image(uiImage: image)
                    .foregroundColorKMM(ColorConstants.Silver)
            }
            if isTextField {
                TextField(
                    field.placeholder,
                    text: $text
                ) { editing in
                    textEditing = editing
                    guard !editing else { return }
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
                    (content.valid() || textEditing ? ColorConstants.Silver : ColorConstants.RadicalRed).toColor(),
                    lineWidth: 2
                )
        ).onTapGesture {
            if !isTextField {
                showModal = true
            }
        }.sheet(isPresented: $showModal) {
            selectionScreen()
        }
    }
    
    private func textContentUpdated() {
        guard content.description != text else { return }
        switch content {
        case let content as UIEditingFieldContent.Text:
            listener(
                field.updateMsg(
                    content.doCopy(text: text)
                )
            )
        //        case let content as UIEditingFieldContent.Location:
        //            listener(
        //                field.updateMsg(
        //                    content.doCopy(location: text)
        //                )
        //            )
        case let content as UIEditingFieldContent.Email:
            listener(
                field.updateMsg(
                    content.doCopy(email: text)
                )
            )
            
        default: fatalError()
        }
    }
    
    private func selectionScreen() -> some View {
        switch content {
        case let content as UIEditingFieldContent.List:
            return SelectionScreen(
                title: field.placeholder,
                selection: content.selection,
                variants: content.list,
                multipleSelection: content.multipleSelectionAvailable,
                onSelectionChanged: {
                    listener(
                        field.updateMsg(
                            content.doCopy(selection: $0)
                        )
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

private extension UIEditingFieldContent.Icon {
    var image: UIImage {
        switch self {
        case .location:
            return R.image.profile.location()!
            
        default: fatalError()
        }
    }
}

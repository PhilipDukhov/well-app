//
//  SearchBar.swift
//  Well
//
//  Created by Philip Dukhov on 4/27/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct SearchBar: View {
    @Binding var text: String
    
    @State private var isEditing = false
    
    var body: some View {
        HStack {
            HStack {
                Image(systemName: "magnifyingglass")
                TextField("Search ...", text: $text)
                    .disableAutocorrection(true)
                    .onTapGesture {
                        self.isEditing = true
                    }
            }.padding().background(Color(.systemGray6))
            .cornerRadius(8)
            .animation(.default)
            
            if isEditing {
                Button {
                    self.isEditing = false
                    self.text = ""
                    UIApplication.shared.endEditing()
                } label: {
                    Text(GlobalStringsBase.companion.shared.cancel)
                }
                .padding(.trailing, 10)
                .transition(.move(edge: .trailing).combined(with: .offset(x: 18, y: 0)))
                .animation(.default)
            }
        }
    }
}

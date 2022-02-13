//
//  TopLevelView.swift
//  Well
//
//  Created by Phil on 26.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct AppContainer<Content: View>: View {
    let content: Content
    
    var body: some View {
        content
            .statusBar(style: .lightContent)
            .accentColor(ColorConstants.Green.toColor())
            .progressViewStyle(CircularProgressViewStyle(tint: .companion.White))
            .toggleStyle(SwitchToggleStyle(tint: SharedMobile.Color.companion.Green.toColor()))
            .onAppear {
                UITableView.appearance().separatorInset = .zero
            }
    }
}

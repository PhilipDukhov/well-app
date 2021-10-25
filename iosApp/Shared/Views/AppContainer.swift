//
//  TopLevelView.swift
//  Well
//
//  Created by Phil on 26.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct AppContainer<Content: View>: View {
    let content: Content
    
    var body: some View {
        content
            .environment(\.defaultMinListRowHeight, 0)
            .statusBar(style: .lightContent)
            .onAppear {
                UITableView.appearance().separatorInset = .zero
            }
    }
}

//
//  InactiveOverlay.swift
//  Well
//
//  Created by Philip Dukhov on 2/15/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct InactiveOverlay<Content: View>: View {
    let showActivityIndicator: Bool
    let content: Content
    
    init(
        showActivityIndicator: Bool = true,
        @ViewBuilder content: () -> Content
    ) {
        self.showActivityIndicator = showActivityIndicator
        self.content = content()
    }

    init(
        showActivityIndicator: Bool = true
    ) where Content == EmptyView {
        self.showActivityIndicator = showActivityIndicator
        self.content = EmptyView()
    }
        
    var body: some View {
        ZStack(alignment: .center) {
            ColorConstants.InactiveOverlay.toColor()
                .fillMaxSize()
            if showActivityIndicator || !(content is EmptyView) {
                RoundedRectangle(cornerRadius: 16)
                    .foregroundColor(.white)
                    .frame(size: 150)
            }
            if showActivityIndicator {
                ProgressView()
            }
            content
        }.fillMaxSize().edgesIgnoringSafeArea(.all)
    }
}

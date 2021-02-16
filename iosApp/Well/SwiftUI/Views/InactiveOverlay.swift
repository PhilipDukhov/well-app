//
//  InactiveOverlay.swift
//  Well
//
//  Created by Philip Dukhov on 2/15/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct InactiveOverlay: View {
    let showActivityIndicator: Bool
    
    init(showActivityIndicator: Bool = true) {
        self.showActivityIndicator = showActivityIndicator
    }
        
    var body: some View {
        ZStack(alignment: .center) {
            ColorConstants.InactiveOverlay.toColor()
                .fillMaxSize()
            if showActivityIndicator {
                ActivityIndicator(style: .large)
            }
        }.fillMaxSize().edgesIgnoringSafeArea(.all)
    }
}

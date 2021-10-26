//
// Created by Phil on 18.05.2021.
//

import SwiftUI

struct ToggleFavoriteButton: View {
    let favorite: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: favorite ? "suit.heart.fill" : "heart")
                .font(.system(size: 25))
                .foregroundColorKMM(favorite ? .companion.Green : .companion.LightGray)
                .frame(minSize: 45)
        }
    }
}

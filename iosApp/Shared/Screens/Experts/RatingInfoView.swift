//
// Created by Phil on 17.05.2021.
//

import SwiftUI
import SharedMobile

struct RatingInfoView: View {
    let ratingInfo: User.RatingInfo
    let viewAll: (() -> Void)?

    init(ratingInfo: User.RatingInfo, viewAll: (() -> Void)? = nil) {
        self.ratingInfo = ratingInfo
        self.viewAll = viewAll
    }

    var body: some View {
        if let viewAll = viewAll {
            Button(action: viewAll) {
                HStack {
                    content
                }
            }
        } else {
            content
        }
    }

    @ViewBuilder
    var content: some View {
        let value = ratingInfo.currentUserRating?.value.toDouble() ?? ratingInfo.average
        HStack {
            ForEach(1...5, id: \.self) { star in
                Image(systemName: "star.fill")
                    .foregroundColorKMM(
                        Double(star) <= value ?
                        (ratingInfo.currentUserRating != nil ? .companion.Supernova : .companion.Green)
                        : .companion.LightGray
                    )
            }
        }
        Spacer()
        Text("reviews (\(Int(ratingInfo.count)))")
            .foregroundColorKMM(.companion.MediumBlue)
            .textStyle(.captionLight)
    }
}

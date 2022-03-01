//
// Created by Phil on 17.05.2021.
//

import SwiftUI
import SharedMobile

struct ReviewInfoView: View {
    let reviewInfo: User.ReviewInfo
    let viewAll: (() -> Void)?

    init(reviewInfo: User.ReviewInfo, viewAll: (() -> Void)? = nil) {
        self.reviewInfo = reviewInfo
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
        let value = reviewInfo.currentUserReview?.value.toDouble() ?? reviewInfo.average
        HStack {
            ForEach(1...5, id: \.self) { star in
                Image(systemName: "star.fill")
                    .foregroundColorKMM(
                        Double(star) <= value ?
                        (reviewInfo.currentUserReview != nil ? .companion.Supernova : .companion.Green)
                        : .companion.LightGray
                    )
            }
        }   
        Spacer()
        Text("reviews (\(Int(reviewInfo.count)))")
            .foregroundColorKMM(.companion.MediumBlue)
            .textStyle(.captionLight)
    }
}

//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import UIKit
import SharedMobile

extension ServerModelsUser {
    var profileImageURL: URL? {
        profileImageUrl.flatMap { URL(string: $0) }
    }
}

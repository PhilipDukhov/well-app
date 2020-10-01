//
//  SocialNetwork.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import Foundation

enum SocialNetwork: CaseIterable {
    @available(iOS 13.0, *)
    case apple
    
    case twitter
    case facebook
    case google
    
    static var allCases: [SocialNetwork] {
        var result: [SocialNetwork] = [
            .twitter,
            .facebook,
            .google
        ]
        if #available(iOS 13.0, *) {
            result.insert(.apple, at: 0)
        }
        return result
    }
}

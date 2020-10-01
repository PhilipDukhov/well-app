//
//  TwitterSigner.swift
//  Well
//
//  Created by Philip Dukhov on 10/1/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit

class TwitterSigner: BaseSocialSigner {
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?)
    {
    }
    
    override func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any])
    -> Bool
    { false }
    
    override func baseRequestCredential(placeholder: UIViewController) {
    }
}

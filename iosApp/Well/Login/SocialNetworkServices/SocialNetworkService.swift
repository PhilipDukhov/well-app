//
//  SocialNetworkService.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit
import FirebaseAuth

final class SocialNetworkService {
    private let signers: EnumDictionary<SocialNetwork, SocialSigner>
    
    var disconnectedHandler: SocialSigner.DisconnectedHandler? {
        didSet {
            signers.forEach {
                $0.value.disconnectedHandler = disconnectedHandler
            }
        }
    }
    
    init() {
        signers = EnumDictionary<SocialNetwork, SocialSigner> {
            switch $0 {
            case .facebook:
                return FacebookSigner()
            case .google:
                return GoogleSigner()
            case .apple:
                return AppleSigner()
            case .twitter:
                return TwitterSigner()
            }
        }
    }
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?)
    {
        signers.values.forEach {
            $0.application(
                application,
                didFinishLaunchingWithOptions: launchOptions
            )
        }
    }
    
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any])
    -> Bool
    {
        signers.values.first {
            $0.application(app, open: url, options: options)
        } != nil
    }
    
    func requestCredential(in controller: UIViewController, social: SocialNetwork) -> Future<SocialSigner.Result>
    {
        return signers[social].requestCredential(placeholder: controller)
    }
}

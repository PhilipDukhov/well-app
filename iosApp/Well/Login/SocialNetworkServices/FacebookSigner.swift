//
//  FacebookSigner.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit
import FirebaseAuth
import FBSDKLoginKit

class FacebookSigner: BaseSocialSigner {
    private let loginManager = LoginManager()
    
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?)
    {
        ApplicationDelegate.shared.application(
            application,
            didFinishLaunchingWithOptions: launchOptions
        )
    }
    
    override func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any])
    -> Bool
    { ApplicationDelegate.shared.application(
        app,
        open: url,
        sourceApplication: options[UIApplication.OpenURLOptionsKey.sourceApplication] as? String,
        annotation: options[UIApplication.OpenURLOptionsKey.annotation]
    ) }
    
    override func baseRequestCredential(placeholder: UIViewController) {
        loginManager.logIn(
            permissions: ["email"],
            from: placeholder)
        { [weak self] result, error in
            self?.finishCredentialRequest({
                if let error = error {
                    return .failure(.other(error))
                }
                if result?.isCancelled == true {
                    return .failure(.canceled)
                }
                guard let token = result?.token else {
                    return .failure(.unknown)
                }
                let credential = FacebookAuthProvider.credential(withAccessToken: token.tokenString)
                return .success(credential)
            }())
        }
    }
}

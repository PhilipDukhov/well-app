//
//  GoogleSigner.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit
import Firebase
import FirebaseAuth
import GoogleSignIn

class GoogleSigner: BaseSocialSigner {
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?)
    {
        GIDSignIn.sharedInstance().clientID = FirebaseApp.app()?.options.clientID
        GIDSignIn.sharedInstance().delegate = self
    }
    
    override func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any])
    -> Bool
    { GIDSignIn.sharedInstance().handle(url) }
    
    override func baseRequestCredential(placeholder: UIViewController) {
        GIDSignIn.sharedInstance().presentingViewController = placeholder
        GIDSignIn.sharedInstance().signOut()
        GIDSignIn.sharedInstance().signIn()
    }
}

extension GoogleSigner: GIDSignInDelegate {
    func sign(
        _ signIn: GIDSignIn!,
        didSignInFor user: GIDGoogleUser!,
        withError error: Error?)
    {
        finishCredentialRequest({
            if let error = error as NSError? {
                return .failure(.forGoogleError(error))
            }
            guard let authentication = user.authentication else {
                return .failure(.unknown)
            }
            let credential = GoogleAuthProvider.credential(withIDToken: authentication.idToken, accessToken: authentication.accessToken)
            return .success(credential)
        }())
    }
    
    func sign(_ signIn: GIDSignIn!, didDisconnectWith user: GIDGoogleUser!, withError error: Error!) {
        disconnectedHandler?(self)
    }
}

extension SocialSignerError {
    static fileprivate func forGoogleError(_ error: NSError) -> Self {
        guard error.domain == kGIDSignInErrorDomain else { return .other(error) }
        switch GIDSignInErrorCode(rawValue: error.code) {
        case .canceled:
            return .canceled
            
        default:
            return .other(error)
        }
    }
}

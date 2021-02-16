//
//  GoogleProvider.swift
//  Well
//
//  Created by Philip Dukhov on 2/14/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SharedMobile
import GoogleSignIn

final class GoogleProvider: CredentialProvider {
    private let context: Context
    private let signIn = GIDSignIn.sharedInstance()!
    // swiftlint:disable:next weak_delegate
    private var signInDelegate: Delegate?
    
    override init(context: Context) {
        self.context = context
        super.init(context: context)
        signIn.clientID = Bundle.main.object(forInfoDictionaryKey: "googleClientId") as? String
    }
    
    override func getCredentials(completionHandler: @escaping (AuthCredential?, Error?) -> Void) {
        signIn.presentingViewController = context.rootController
        signInDelegate = Delegate(completionHandler: completionHandler)
        signIn.delegate = signInDelegate
        signIn.signIn()
    }
    
    override func application(app: UIApplication, openURL: URL, options: [AnyHashable: Any] = [:]) -> Bool {
        signIn.handle(openURL)
    }
}

final private class Delegate: NSObject, GIDSignInDelegate {
    let completionHandler: (AuthCredential?, Error?) -> Void
    
    init(completionHandler: @escaping (AuthCredential?, Error?) -> Void) {
        self.completionHandler = completionHandler
        super.init()
    }
    
    func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!, withError error: Error!) {
        signIn.delegate = nil
        if let token = user?.authentication?.idToken {
            completionHandler(AuthCredential.GoogleCredential(token: token), nil)
            return
        }
        guard let error = error as NSError? else { fatalError("No token and no error") }
        if error.domain == kGIDSignInErrorDomain && GIDSignInErrorCode(rawValue: error.code) == .canceled {
            completionHandler(nil, KotlinCancellationException().toNSError())
        } else {
            completionHandler(nil, error)
        }
    }
}

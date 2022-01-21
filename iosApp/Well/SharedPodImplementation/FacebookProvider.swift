//
//  FacebookProvider.swift
//  Well
//
//  Created by Philip Dukhov on 2/14/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SharedMobile
import FBSDKCoreKit
import FBSDKLoginKit

final class FacebookProvider: CredentialProvider {
    private let loginManager: LoginManager

    override init(systemContext: SystemContext) {
        Settings.shared.isAdvertiserIDCollectionEnabled = true
        Settings.shared.loggingBehaviors = Set()
        ApplicationDelegate.shared.initializeSDK()
        Settings.shared.isAdvertiserIDCollectionEnabled = false
        loginManager = LoginManager()
        super.init(systemContext: systemContext)
    }

    override func getCredentials(completionHandler: @escaping (AuthCredential?, Error?) -> Void) {
        loginManager.logOut()
        loginManager.logIn(configuration: LoginConfiguration()!) { result in
            switch result {
            case let .success(_, _, token):
                if let token = token {
                    completionHandler(AuthCredential.FacebookCredential(token: token.tokenString), nil)
                } else {
                    completionHandler(nil, NSError(description: "Facebook login succeed but token is empty")/*KotlinIllegalStateException().toNSError()*/)
                }
            case .failed(let error):
                completionHandler(nil, error)

            case .cancelled:
                completionHandler(nil, KotlinCancellationException().toNSError())
            @unknown default:
                fatalError()
            }
        }
    }

    override func application(app: UIApplication, openURL: URL, options: [AnyHashable: Any] = [:]) -> Bool {
        ApplicationDelegate.shared.application(
            app,
            open: openURL,
            options: options as? [UIApplication.OpenURLOptionsKey: Any] ?? [:]
        )
    }
}

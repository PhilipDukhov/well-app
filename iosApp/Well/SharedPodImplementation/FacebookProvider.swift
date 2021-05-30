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
    private let appContext: AppContext
    private let loginManager = LoginManager()
    
    override init(appContext: AppContext) {
        self.appContext = appContext
        super.init(appContext: appContext)
        Settings.isAdvertiserIDCollectionEnabled = false
        ApplicationDelegate.shared.application(
            appContext.application,
            didFinishLaunchingWithOptions: appContext.launchOptions as? [UIApplication.LaunchOptionsKey: Any]
        )
    }
    
    override func getCredentials(completionHandler: @escaping (AuthCredential?, Error?) -> Void) {
        loginManager.logIn(
            permissions: [],
            viewController: appContext.rootController
        ) { result in
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

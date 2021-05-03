//
// Created by Philip Dukhov on 4/30/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SharedMobile
import UIKit
import AuthenticationServices

final class AppleProvider: CredentialProvider {
    private let context: Context
    private var completionHandler: ((AuthCredential?, Error?) -> Void)?

    override init(context: Context) {
        self.context = context
        super.init(context: context)
    }

    override func getCredentials(completionHandler: @escaping (AuthCredential?, Error?) -> Void) {
        self.completionHandler = completionHandler
        let appleIDProvider = ASAuthorizationAppleIDProvider()
        let request = appleIDProvider.createRequest()
        request.requestedScopes = [.fullName, .email]

        let authorizationController = ASAuthorizationController(authorizationRequests: [request])
        authorizationController.delegate = self
        authorizationController.presentationContextProvider = context.rootController as? ASAuthorizationControllerPresentationContextProviding
        authorizationController.performRequests()
    }
}

extension AppleProvider: ASAuthorizationControllerDelegate {
    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        print(#function, authorization)
        switch authorization.credential {
        case let appleIDCredential as ASAuthorizationAppleIDCredential:

            // Create an account in your system.
            let userIdentifier = appleIDCredential.user
            let fullName = appleIDCredential.fullName
            let email = appleIDCredential.email

            print("test", appleIDCredential, userIdentifier, email, fullName)
            completionHandler?(AuthCredential.AppleCredential(userIdentifier: appleIDCredential.user), nil)

        case let passwordCredential as ASPasswordCredential:

            // Sign in using an existing iCloud Keychain credential.
            let username = passwordCredential.user
            let password = passwordCredential.password

            // For the purpose of this demo app, show the password credential as an alert.
            print(passwordCredential, username, password)

        default:
            print("default", authorization.credential)
        }
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        let error = error as NSError
        if error.domain == ASAuthorizationErrorDomain && error.code == ASAuthorizationError.canceled.rawValue {
            completionHandler?(nil, KotlinCancellationException().toNSError())
        } else {
            print(#function, error)
            completionHandler?(nil, error)
        }
    }
}

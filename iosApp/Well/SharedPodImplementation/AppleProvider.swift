//
// Created by Philip Dukhov on 4/30/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SharedMobile
import UIKit
import AuthenticationServices

final class AppleProvider: CredentialProvider {
    private let systemContext: SystemContext
    private var completionHandler: ((AuthCredential?, Error?) -> Void)?

    override init(systemContext: SystemContext) {
        self.systemContext = systemContext
        super.init(systemContext: systemContext)
    }

    override func getCredentials(completionHandler: @escaping (AuthCredential?, Error?) -> Void) {
        self.completionHandler = completionHandler
        let appleIDProvider = ASAuthorizationAppleIDProvider()
        let request = appleIDProvider.createRequest()
        request.requestedScopes = [.fullName, .email]

        let authorizationController = ASAuthorizationController(authorizationRequests: [request])
        authorizationController.delegate = self
        authorizationController.presentationContextProvider = systemContext.rootController as? ASAuthorizationControllerPresentationContextProviding
        authorizationController.performRequests()
    }
}

extension AppleProvider: ASAuthorizationControllerDelegate {
    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        switch authorization.credential {
        case let appleIDCredential as ASAuthorizationAppleIDCredential:
            guard let identityToken = appleIDCredential.identityToken?.toString(encoding: .utf8) else {
                completionHandler?(nil, NSError(description: "authorizationCode missing"))
                return
            }
            completionHandler?(AuthCredential.AppleCredential(identityToken: identityToken), nil)
        case let passwordCredential as ASPasswordCredential:

            // Sign in using an existing iCloud Keychain credential.
            let username = passwordCredential.user
            let password = passwordCredential.password

            // For the purpose of this demo app, show the password credential as an alert.
            Napier.i(passwordCredential, username, password)

        default:
            Napier.i("default", authorization.credential)
        }
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        let error = error as NSError
        if error.domain == ASAuthorizationErrorDomain
            && [ASAuthorizationError.canceled, .unknown].contains(where: { error.code == $0.rawValue })
        {
            completionHandler?(nil, KotlinCancellationException().toNSError())
        } else {
            Napier.e(#function, error)
            completionHandler?(nil, error)
        }
    }
}

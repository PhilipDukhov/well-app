//
//  TwitterSigner.swift
//  Well
//
//  Created by Philip Dukhov on 10/1/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit
import FirebaseAuth

class TwitterSigner: BaseSocialSigner {
    private lazy var provider = OAuthProvider(providerID: "twitter.com")

    override func baseRequestCredential(placeholder: UIViewController) {
        provider.getCredentialWith(nil) { [weak self] credential, error in
            self?.finishCredentialRequest({
                if let error = error {
                    return .failure(.forFirebaseAuthError(error as NSError))
                }
                guard let credential = credential else {
                    return .failure(.unknown)
                }
                return .success(credential)
            }())
        }
    }
}

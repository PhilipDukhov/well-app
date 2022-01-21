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
    private let systemContext: SystemContext

    override init(systemContext: SystemContext) {
        self.systemContext = systemContext
        super.init(systemContext: systemContext)
    }

    override func getCredentials(completionHandler: @escaping (AuthCredential?, Error?) -> Void) {
        GIDSignIn.sharedInstance.signIn(
            with: GIDConfiguration(
                clientID: (Bundle.main.object(forInfoDictionaryKey: "firebaseInfo") as! [String: String])["clientId"]!
            ),
            presenting: systemContext.rootController
        ) { user, error in
            if let token = user?.authentication.idToken {
                completionHandler(AuthCredential.GoogleCredential(token: token), nil)
                return
            }
            guard let error = error as? GIDSignInError else { fatalError("No token and no error \(String(describing: error))") }
            if error.code == .canceled {
                completionHandler(nil, KotlinCancellationException().toNSError())
            } else {
                completionHandler(nil, error)
            }
        }
    }

    override func application(app: UIApplication, openURL: URL, options: [AnyHashable: Any] = [:]) -> Bool {
        GIDSignIn.sharedInstance.handle(openURL)
    }
}

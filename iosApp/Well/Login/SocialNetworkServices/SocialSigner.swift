//
//  SocialSigner.swift
//  Well
//
//  Created by Philip Dukhov on 10/1/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit
import FirebaseAuth

enum SocialSignerError: Error {
    case canceled
    case unknown
    
    case other(Error)
    
    static func forFirebaseAuthError(_ error: NSError) -> Self {
        guard error.domain == AuthErrorDomain else { return .other(error) }
        switch AuthErrorCode(rawValue: error.code) {
        case .webContextCancelled:
            return .canceled
            
        default:
            return .other(error)
        }
    }
}

protocol SocialSigner: class {
    typealias Result = Swift.Result<AuthCredential, SocialSignerError>
    typealias DisconnectedHandler = (SocialSigner) -> Void
    
    var disconnectedHandler: DisconnectedHandler? { get set }
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?)
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any])
    -> Bool
    
    func requestCredential(placeholder: UIViewController) -> Future<Result>
}

class BaseSocialSigner: NSObject, SocialSigner {
    var disconnectedHandler: DisconnectedHandler?
    private var promise: ((SocialSigner.Result) -> Void)?
    
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any])
    -> Bool
    { return false }
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) {}
    
    func requestCredential(placeholder: UIViewController)
    -> Future<SocialSigner.Result>
    {
        .init { promise in
            self.promise = promise
            baseRequestCredential(placeholder: placeholder)
        }
    }
    
    func baseRequestCredential(placeholder: UIViewController) { }
    
    func finishCredentialRequest(_ result: SocialSigner.Result) {
        promise?(result)
        promise = nil
    }
}

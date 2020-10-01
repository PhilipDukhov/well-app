//
//  AppleSigner.swift
//  Well
//
//  Created by Philip Dukhov on 10/1/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit
import FirebaseAuth
import CryptoKit
import AuthenticationServices

@available(iOS 13, *)
class AppleSigner: BaseSocialSigner {
    enum RequestError: Error {
        case unexpectedCallback
        case unexpectedCredential
        case missinIdentityToken
        case invalidIdentityToken
    }
    
    override func baseRequestCredential(placeholder: UIViewController) {
        let nonce = randomNonceString()
        requestInfo = .init(nonce: nonce, placeholder: placeholder)
        
        let appleIDProvider = ASAuthorizationAppleIDProvider()
        let request = appleIDProvider.createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = sha256(nonce)
        
        let authorizationController = ASAuthorizationController(authorizationRequests: [request])
        authorizationController.delegate = self
        authorizationController.presentationContextProvider = self
        authorizationController.performRequests()
    }
    
    override func finishCredentialRequest(_ result: Result<AuthCredential, SocialSignerError>) {
        super.finishCredentialRequest(result)
        requestInfo = nil
    }
    
    private struct RequestInfo {
        let nonce: String
        let placeholder: UIViewController
    }
    
    private var requestInfo: RequestInfo?
    
    private func randomNonceString(length: Int = 32) -> String {
        precondition(length > 0)
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var result = ""
        var remainingLength = length
        
        while remainingLength > 0 {
            let randoms: [UInt8] = (0 ..< 16).map { _ in
                var random: UInt8 = 0
                let errorCode = SecRandomCopyBytes(kSecRandomDefault, 1, &random)
                if errorCode != errSecSuccess {
                    fatalError("Unable to generate nonce. SecRandomCopyBytes failed with OSStatus \(errorCode)")
                }
                return random
            }
            
            randoms.forEach { random in
                if remainingLength == 0 {
                    return
                }
                
                if random < charset.count {
                    result.append(charset[Int(random)])
                    remainingLength -= 1
                }
            }
        }
        
        return result
    }
    
    private func sha256(_ input: String) -> String {
        let inputData = Data(input.utf8)
        let hashedData = SHA256.hash(data: inputData)
        let hashString = hashedData.compactMap {
            return String(format: "%02x", $0)
        }.joined()
        
        return hashString
    }
}

@available(iOS 13, *)
extension AppleSigner: ASAuthorizationControllerDelegate {
    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization)
    {
        finishCredentialRequest({
            guard let nonce = requestInfo?.nonce else {
                return .failure(.other(RequestError.unexpectedCallback))
            }
            guard let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential else {
                return .failure(.other(RequestError.unexpectedCredential))
            }
            guard let appleIDToken = appleIDCredential.identityToken else {
                return .failure(.other(RequestError.missinIdentityToken))
            }
            guard let idTokenString = String(data: appleIDToken, encoding: .utf8) else {
                return .failure(.other(RequestError.invalidIdentityToken))
            }
            return .success(
                OAuthProvider.credential(
                    withProviderID: "apple.com",
                    idToken: idTokenString,
                    rawNonce: nonce)
            )
        }())
    }
    
    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error)
    {
        finishCredentialRequest(.failure(.forAppleError(error as NSError)))
    }
}

@available(iOS 13, *)
extension AppleSigner: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        requestInfo!.placeholder.view.window!
    }
}

@available(iOS 13, *)
extension SocialSignerError {
    static fileprivate func forAppleError(_ error: NSError) -> Self {
        guard let authError = error as? ASAuthorizationError else { return .other(error) }
        switch authError.code {
        case .canceled:
            return .canceled
            
        default:
            return .other(error)
        }
    }
}

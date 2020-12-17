import UIKit
#if !AUTH_DISABLED
import Auth
#endif
import Shared
import SwiftUI

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    
    let facebookToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6Imt0b3IuaW8iLCJpZCI6Mn0.IE6mMsjdWMzkuBnEHqI9LcS67C8BT7O_Ooe4KzRCULFtLwduhbDy7-e0VMOZEwZtbWJV8MbFMYfkZ1FQj0np6A"
    let googleToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6Imt0b3IuaW8iLCJpZCI6M30.prOmXoRUj22v6VYwX6iDLIZc60J706T8GXxvYGhHj86kGoS7mjiZ36f5EKft-J3u8TK83YfDz9E5W_GPFhzRMA"
        
    #if !AUTH_DISABLED
    private var socialNetworkService: SocialNetworkService!
    #endif
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        #if !AUTH_DISABLED
        socialNetworkService = SocialNetworkService(
            context: .init(
                application: application,
                launchOptions: launchOptions
            )
        )
        #endif
        window = initializeWindow()
        initializeLogging()
        return true
    }
    
    func initializeWindow() -> UIWindow {
        let window = UIWindow(frame: UIScreen.main.bounds)
        window.rootViewController = UIHostingController(
            rootView: OnlineUsersList(viewModel: .init(token: googleToken))
        )
        #if !AUTH_DISABLED
        let loginInitialViewController = LoginInitialViewController()
        loginInitialViewController.props = .init(
            socialNetworkAction: .init { [self, unowned loginInitialViewController] in
                socialNetworkService.login(
                    network: $0,
                    loginView: loginInitialViewController
                ) { result, error in
                    if let result = result {
                        window.rootViewController = UIHostingController(
                            rootView: OnlineUsersList(viewModel: .init(token: result.bearerToken))
                        )
                    }
                    if let error = error {
                        if error.isKotlinCancellationException {
                            print("\(#function) cancelled")
                            return
                        }
                        print("\(#function) \(error.sharedLocalizedDescription)")
                        return
                    }
                }
            },
            createAccountAction: .zero,
            signInAction: .zero)
        window.rootViewController = loginInitialViewController
        #endif
        window.makeKeyAndVisible()
        return window
    }
    
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:])
    -> Bool
    {
        #if !AUTH_DISABLED
        return socialNetworkService.application(app: app, openURL: url, options: options)
        #else
        return false
        #endif
    }
    
    private func initializeLogging() {
        #if DEBUG
        NapierProxy().buildDebug()
        #else
//        NapierProxy().build(antilog: CrashlyticsAntilog
//        { _, tag, message in
//            let args = [tag, message].compactMap { $0 }
//            Crashlytics.crashlytics().log(args.joined(separator: " "))
//        }
//        crashlyticsSendLog: { throwable in
//            Crashlytics.crashlytics().record(
//                error: NSError(description: throwable.description())
//            )
//        })
        #endif
    }
}

extension Error {
    var sharedLocalizedDescription: String {
        "\(kotlinException ?? localizedDescription)"
    }
    
    var kotlinException: Any? {
        (self as NSError?)?.kotlinException
    }
    
    var isKotlinCancellationException: Bool {
        kotlinException.map {
            "\(type(of: $0))".contains("CancellationException")
        } ?? false
    }
}

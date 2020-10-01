import UIKit
import shared
import Firebase
import FBSDKCoreKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    
    private let socialNetworkService = SocialNetworkService()
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        window = initializeWindow()
        initializeLogging()
        FirebaseApp.configure()
        socialNetworkService.application(
            application,
            didFinishLaunchingWithOptions: launchOptions
        )
        socialNetworkService.disconnectedHandler = { signer in
            print("disconnected \(signer)")
        }
        return true
    }
    
    func initializeWindow() -> UIWindow {
        let window = UIWindow(frame: UIScreen.main.bounds)
        let loginInitialViewController = LoginInitialViewController()
        loginInitialViewController.props = .init(
            socialNetworkAction: .init { [self, unowned loginInitialViewController] in
                socialNetworkService.requestCredential(
                    in: loginInitialViewController,
                    social: $0
                ).onComplete { result in
                    print(result)
                }
            },
            createAccountAction: .zero,
            signInAction: .zero)
        let navigationController = UINavigationController(rootViewController: loginInitialViewController)
        navigationController.setNavigationBarHidden(true, animated: false)
        window.rootViewController = navigationController
        window.makeKeyAndVisible()
        return window
    }
    
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:])
    -> Bool
    {
        socialNetworkService.application(app, open: url, options: options)
    }
    
    private func initializeLogging() {
        #if DEBUG
        FirebaseConfiguration.shared.setLoggerLevel(.min)
        NapierProxyKt.buildDebug()
        #else
        NapierProxyKt.build(antilog: CrashlyticsAntilog
        { _, tag, message in
            let args = [tag, message].compactMap { $0 }
            Crashlytics.crashlytics().log(args.joined(separator: " "))
        }
        crashlyticsSendLog: { throwable in
            Crashlytics.crashlytics().record(
                error: NSError(description: throwable.description())
            )
        })
        #endif
    }
}

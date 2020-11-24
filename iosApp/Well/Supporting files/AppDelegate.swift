import UIKit
import Auth
import Shared

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    
    private var socialNetworkService: SocialNetworkService!
        
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        socialNetworkService = SocialNetworkService(
            context: .init(
                application: application,
                launchOptions: launchOptions
            )
        )
        window = initializeWindow()
        initializeLogging()
        return true
    }
    
    func initializeWindow() -> UIWindow {
        let window = UIWindow(frame: UIScreen.main.bounds)
        let loginInitialViewController = LoginInitialViewController()
        loginInitialViewController.props = .init(
            socialNetworkAction: .init { [self, unowned loginInitialViewController] in
                socialNetworkService.login(
                    network: $0,
                    loginView: loginInitialViewController
                ) { result, error in
                    print(result, (error as NSError?)?.kotlinException ?? error)
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
        socialNetworkService.application(app: app, openURL: url, options: options)
    }
    
    private func initializeLogging() {
        #if DEBUG
//        FirebaseConfiguration.shared.setLoggerLevel(.min)
        NapierProxy().buildDebug()
        #else
        NapierProxy().build(antilog: CrashlyticsAntilog
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

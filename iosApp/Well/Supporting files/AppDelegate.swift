import UIKit
import shared
import Firebase

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        window = Self.initializeWindow()
        initializeLogging()
        return true
    }
    
    static func initializeWindow() -> UIWindow {
        let window = UIWindow(frame: UIScreen.main.bounds)
        let navigationController = UINavigationController(rootViewController: LoginInitialViewController())
        navigationController.setNavigationBarHidden(true, animated: false)
        window.rootViewController = navigationController
        window.makeKeyAndVisible()
        return window
    }
    
    private func initializeLogging() {
        #if DEBUG
        NapierProxyKt.buildDebug()
        #else
        FirebaseApp.configure()
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

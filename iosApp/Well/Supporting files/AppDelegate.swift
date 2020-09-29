import UIKit
import shared
import Firebase

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        initializeLogging()
        print(Greeting().greeting())
        return true
    }
    
    @available(iOS 13.0, *)
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }
    
    private func initializeLogging() {
//        #if DEBUG
//        NapierProxyKt.buildDebug()
//        #else
        FirebaseApp.configure()
        NapierProxyKt.build(antilog: CrashlyticsAntilog
        { _, tag, message in
            let args = [tag, message].compactMap { $0 }
            Crashlytics.crashlytics().log(args.joined(separator: " "))
            print("test \(args)")
        }
        crashlyticsSendLog: { throwable in
            print("test \(throwable)")
            Crashlytics.crashlytics().record(
                error: NSError(
                    domain: "Bubble",
                    code: 0,
                    userInfo: [NSLocalizedDescriptionKey: throwable.description()]
                )
            )
        })
//        #endif
    }
}

import UIKit
import shared
import Firebase
import FirebaseAuth
import GoogleSignIn

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        window = Self.initializeWindow()
        initializeLogging()
        initializeGoogleServices()
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
    
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:])
    -> Bool
    {
        return GIDSignIn.sharedInstance().handle(url)
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
    
    private func initializeGoogleServices() {
        FirebaseApp.configure()
        
        GIDSignIn.sharedInstance().clientID = FirebaseApp.app()?.options.clientID
        GIDSignIn.sharedInstance().delegate = self
    }
}

extension AppDelegate: GIDSignInDelegate {
    func sign(_ signIn: GIDSignIn!, didSignInFor user: GIDGoogleUser!, withError error: Error?) {
        if let error = error {
            print(error)
            return
        }
        
        guard let authentication = user.authentication else { return }
        let credential = GoogleAuthProvider.credential(withIDToken: authentication.idToken, accessToken: authentication.accessToken)
        print(credential)
    }
    
    func sign(_ signIn: GIDSignIn!, didDisconnectWith user: GIDGoogleUser!, withError error: Error!) {
        // Perform any operations when the user disconnects from app here.
        // ...
    }
}

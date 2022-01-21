import UIKit
import SharedMobile
import SwiftUI

@UIApplicationMain
final class AppDelegate: UIResponder, UIApplicationDelegate {
    let rootViewController = HostingController(
        wrappedView: TopLevelView(
            state: TopLevelFeature().initialState()
        ) { _ in
        }
    )
    private var featureProvider: FeatureProvider!
    
    var window: UIWindow?

    var featureStateCloseable: AtomicCloseable?
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        featureProvider = CreateFeatureProviderKt.createFeatureProvider(
            applicationContext: ApplicationContext(application: application),
            webRtcManagerGenerator: WebRtcManager.init,
            providerGenerator: { socialNetwork, systemContext, _ in
                switch socialNetwork {
                case .facebook:
                    return FacebookProvider(systemContext: systemContext)
                    
                case .google:
                    return GoogleProvider(systemContext: systemContext)
                    
                case .apple:
                    return AppleProvider(systemContext: systemContext)
                    
                default: fatalError()
                }
            }
        )
        featureProvider.accept(
            msg: TopLevelFeature.MsgUpdateSystemContext(
                systemContext: SystemContext(
                    rootController: rootViewController,
                    application: application,
                    launchOptions: launchOptions,
                    cacheImage: { image, url in
                        ImageLoader.cache(image.uiImage, url)
                    }
                )
            )
        )
        window = initializeWindow()
        setupNotifications(application)
        return true
    }
    
    func initializeWindow(
    ) -> UIWindow {
        let window = UIWindow(frame: UIScreen.main.bounds)
        window.rootViewController = rootViewController
        startListening()
        window.makeKeyAndVisible()
        return window
    }
    
    func startListening() {
        featureStateCloseable = featureProvider
            .state
            .onChange {  [self] (state: TopLevelFeature.State) in
                rootViewController.updateWrapperView(
                    TopLevelView(
                        state: state,
                        listener: featureProvider.accept
                    )
                )
            }
    }
    
    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        SocialNetworkServiceExtKt.application(featureProvider, app: app, openURL: url, options: options)
    }

//    func setupFirebase() {
////        FirebaseCoordinator.configure()
//
//        let firebaseInfo = Bundle.main.object(forInfoDictionaryKey: "firebaseInfo") as! [String: String]
//        let options = FirebaseOptions(
//            googleAppID: firebaseInfo["appId"]!,
//            gcmSenderID: firebaseInfo["gcmSenderID"]!
//        )
//        options.apiKey = firebaseInfo["apiKey"]!
//        options.clientID = firebaseInfo["clientId"]!
//        options.googleAppID = firebaseInfo["appId"]!
//        options.projectID = firebaseInfo["projectID"]!
//
//        FirebaseApp.configure(options: options)
//    }

    func setupNotifications(
        _ application: UIApplication
    ) {
        UNUserNotificationCenter.current().delegate = self

        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(
            options: authOptions,
            completionHandler: { success, _ in
                guard success else { return }
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                    UNUserNotificationCenter.current().getDeliveredNotifications { [self] notifications in
                        notifications
                            .map(TopLevelFeature.MsgRawNotificationReceived.init)
                            .forEach(featureProvider.accept)
                    }
                }
            }
        )
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        print(#function, deviceToken.toHexEncodedString())
        featureProvider.accept(
            msg: TopLevelFeature.MsgUpdateNotificationToken(
                token: NotificationToken.Apns(
                    token: deviceToken.toHexEncodedString(),
                    bundleId: Bundle.main.bundleIdentifier!
                )
            )
        )
    }
}

extension AppDelegate: UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        SocialNetworkServiceExtKt.userNotificationCenter(
            featureProvider,
            didReceiveNotificationResponse: response
        )
    }
}

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
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        NapierProxy().initializeLogging()
        featureProvider = CreateFeatureProviderKt.createFeatureProvider(
            appContext: .init(
                rootController: rootViewController,
                application: application,
                launchOptions: launchOptions,
                cacheImage: { image, url in
                    ImageLoader.cache(image.uiImage, url)
                }
            ),
            webRtcManagerGenerator: WebRtcManager.init,
            providerGenerator: { socialNetwork, appContext, _ in
                switch socialNetwork {
                case .facebook:
                    return FacebookProvider(appContext: appContext)
                    
                case .google:
                    return GoogleProvider(appContext: appContext)
                    
                case .apple:
                    return AppleProvider(appContext: appContext)
                    
                default: fatalError()
                }
            }
        )
        window = initializeWindow()
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
        featureProvider.listenState { [self] state in
            rootViewController.updateWrapperView(
                TopLevelView(
                    state: state as! TopLevelFeature.State,
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
}

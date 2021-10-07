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
        // swiftlint:disable:next trailing_closure
        featureProvider = FeatureProvider(
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
        UITableView.appearance().separatorInset = .zero
        window = initializeWindow()
        return true
    }

    func initializeWindow(
    ) -> UIWindow {
        let window = UIWindow(frame: UIScreen.main.bounds)
        window.rootViewController = rootViewController
        if !TopLevelView.testing {
            featureProvider.feature.listenState { [self] state in
                rootViewController.updateWrapperView(
                    TopLevelView(
                        state: state,
                        listener: featureProvider.feature.accept
                    )
                )
            }
        }
        window.makeKeyAndVisible()
        return window
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        featureProvider.application(app: app, openURL: url, options: options)
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

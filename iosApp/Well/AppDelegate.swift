import UIKit
import SharedMobile
import SwiftUI
import WebRTC

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
            context: .init(
                rootController: rootViewController,
                application: application,
                launchOptions: launchOptions
            ),
            webRtcManagerGenerator: WebRtcManager.init,
            providerGenerator: { socialNetwork, context -> CredentialProvider in
                switch socialNetwork {
                case .facebook:
                    return FacebookProvider(context: context)

                case .google:
                    return GoogleProvider(context: context)

                case .apple:
                    return AppleProvider(context: context)

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

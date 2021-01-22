#if !AUTH_DISABLED
import Auth
#endif
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
    private let featureProvider: FeatureProvider

    override init() {
        NapierProxy().initializeLogging()
        featureProvider = FeatureProvider(
            context: .init(rootController: rootViewController),
            webRtcManagerGenerator: WebRtcManager.init
        )
    }

    var window: UIWindow?

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
        UITableView.appearance().separatorColor = .clear
        window = initializeWindow()
        initializeLogging()
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
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    )
            -> Bool {
        #if !AUTH_DISABLED
        return socialNetworkService.application(app: app, openURL: url, options: options)
        #else
        return false
        #endif
    }

    // MARK: Privates

    private func initializeLogging() {
        NapierProxy().initializeLogging()
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

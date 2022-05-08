import UIKit
import SharedMobile
import SwiftUI
import PushKit
import CallKit
import AVFoundation

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

    let voipRegistry = PKPushRegistry(queue: nil)
    let config = CXProviderConfiguration()
    let callProvider: CXProvider

    override init() {
        printd(#function)

        config.supportsVideo = true
        config.supportedHandleTypes = [.generic]
        config.maximumCallsPerCallGroup = 1
        config.maximumCallGroups = 1

        callProvider = CXProvider(configuration: config)
        super.init()
        printd("pushToken(for: .voIP)", voipRegistry.pushToken(for: .voIP)?.toHexEncodedString() as Any)

        let id = UUID()
//        Task {
//            try await Task.sleep(nanoseconds: 1_000_000_000)
//            callProvider.reportOutgoingCall(with: id, startedConnectingAt: nil)
//            try await Task.sleep(nanoseconds: 1_000_000_000)
//            callProvider.reportOutgoingCall(with: id, connectedAt: nil)
//        }

        DispatchQueue.main.asyncAfter(deadline: .now() + 2) { [self] in
//            let callUpdate = CXCallUpdate()
//            callUpdate.remoteHandle = CXHandle(type: .generic, value: "Phil")
//            callUpdate.hasVideo = false
//            callUpdate.supportsHolding = false
//
//            callProvider.reportNewIncomingCall(with: id, update: callUpdate) { error in
//                print("reportNewIncomingCall", error as Any)
//            }

//            let handle = CXHandle(type: .generic, value: "123")
//            let startCallAction = CXStartCallAction(call: id, handle: handle)
//            startCallAction.contactIdentifier = "displayTitle"
//
//            startCallAction.isVideo = false
//            let transaction = CXTransaction(action: startCallAction)
//
//            requestTransaction(transaction)
//            callProvider.reportOutgoingCall(with: id, startedConnectingAt: nil)
//            print("reportOutgoingCall")
////            let callUpdate = CXCallUpdate()
////            callUpdate.remoteHandle = CXHandle(type: .generic, value: "Phil")
////            callUpdate.hasVideo = true
////            callUpdate.supportsHolding = false
////            callProvider.reportCall(with: id, updated: callUpdate)
//            DispatchQueue.main.asyncAfter(deadline: .now() + 1) { [self] in
//                callProvider.reportOutgoingCall(with: id, connectedAt: nil)
//            }
        }
    }

    private func requestTransaction(_ transaction: CXTransaction, completion: ((Bool) -> Void)? = nil) {
        callController.request(transaction) { error in
            if let error = error {
                print("Error requesting transaction: \(error)")
            }
            completion?(error == nil)
        }
    }
    
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
            .onChange { [self] (state: TopLevelFeature.State) in
                rootViewController.updateWrapperView(
                    TopLevelView(
                        state: state,
                        listener: featureProvider.accept
                    )
                )
                if state.loggedIn != (voipRegistry.delegate == nil) {
//                    if state.loggedIn {
//                        callProvider.setDelegate(nil, queue: nil)
//                        voipRegistry.delegate = nil
//                        voipRegistry.desiredPushTypes = nil
//                        print("unregistered")
//                    } else {
//                        callProvider.setDelegate(self, queue: nil)
//                        voipRegistry.delegate = self
//                        voipRegistry.desiredPushTypes = [.voIP]
//                        print("registered")
//                    }
                }
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

        let notificationCenter = UNUserNotificationCenter.current()
        notificationCenter.delegate = self

        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        notificationCenter.requestAuthorization(
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
        printd(#function, response)
        SocialNetworkServiceExtKt.userNotificationCenter(
            featureProvider,
            didReceiveNotificationResponse: response
        )
    }
}

extension AppDelegate: PKPushRegistryDelegate {
    func pushRegistry(_ registry: PKPushRegistry, didUpdate pushCredentials: PKPushCredentials, for type: PKPushType) {
        printd(#function, pushCredentials, type, pushCredentials.token.toHexEncodedString())
    }

    func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenFor type: PKPushType) {
        printd(#function, type)
    }

    func pushRegistry(_ registry: PKPushRegistry, didReceiveIncomingPushWith payload: PKPushPayload, for type: PKPushType, completion: @escaping () -> Void) {
        print(#function, payload.dictionaryPayload)
        guard type == .voIP else { return }

        let callUpdate = CXCallUpdate()
        callUpdate.remoteHandle = CXHandle(type: .generic, value: "Phil")
        callUpdate.hasVideo = false
        callUpdate.supportsHolding = false

        callProvider.reportNewIncomingCall(with: id, update: callUpdate) { error in
            printd("reportNewIncomingCall", error as Any)
            completion()
//            DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: { [self] in

//                callUpdate.hasVideo = false
//                callProvider.invalidate()
//                printd("invalidate")
//            })
        }
    }
}

let callController = CXCallController()

let id = UUID()
var timer: Timer?

extension AppDelegate: CXProviderDelegate {
    func providerDidReset(_ provider: CXProvider) {
        printd(#function, provider)
    }

    func providerDidBegin(_ provider: CXProvider) {
        printd(#function)
    }
    func provider(_ provider: CXProvider, execute transaction: CXTransaction) -> Bool {
        printd(#function, transaction.actions)
        transaction.actions.forEach {
            $0.fulfill()
        }
        let callUpdate = CXCallUpdate()
        callUpdate.remoteHandle = CXHandle(type: .generic, value: "Phil")
        callUpdate.hasVideo = false
        callUpdate.supportsHolding = false
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true, block: { _ in
            callUpdate.hasVideo.toggle()
            provider.reportCall(with: id, updated: callUpdate)
        })
        return true
    }

    func provider(_ provider: CXProvider, timedOutPerforming action: CXAction) {
        printd(#function, action)
    }

    func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        printd(#function, audioSession)
    }

    func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
        printd(#function, audioSession)
    }
}

func printd(_ items: Any..., separator: String = " ", terminator: String = "\n") {
    NSLog((["tagtag"] + items).map { String(describing: $0) }.joined(separator: separator))
}

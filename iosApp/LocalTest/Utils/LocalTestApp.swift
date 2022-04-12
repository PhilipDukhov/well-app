//
//  LocalTestApp.swift
//  LocalTest
//
//  Created by Phil on 25.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile
import Introspect

@main
struct LocalTestApp: App {
    @State
//    var selectedScreen: TestScreen = .welcome
    var selectedScreen: TestScreen = .companion.initial
    
    @State
    var opened = true

    @State
    var systemHelper: SystemHelper?

    @State
    var viewController: UIViewController?

    init() {
        NapierProxy.shared
            .initializeLogging(applicationContext: ApplicationContext(application: UIApplication.shared))
        UINavigationController.swizzleIsNavigationBarHiddenImplementation
    }
    
    var body: some Scene {
        WindowGroup {
            NavigationView {
                VStack {
                    ScrollViewReader { scrollViewReader in
                        ScrollView(.horizontal, showsIndicators: false) {
                            LazyHStack {
                                ForEach(TestScreen.companion.allCases, id: \.self) { screen in
                                    Button(screen.name) {
                                        selectedScreen = screen
                                        opened = true
                                    }.padding().background(
                                        ZStack {
                                            RoundedRectangle(cornerRadius: 20)
                                                .foregroundColor(screen == selectedScreen ? Color.yellow : .clear)
                                            RoundedRectangle(cornerRadius: 20)
                                                .stroke(Color.gray, lineWidth: 3)
                                        }
                                    ).padding(.horizontal)
                                }
                            }.onAppear {
                                scrollViewReader.scrollTo(selectedScreen, anchor: .center)
                            }.onChange(of: selectedScreen) { screen in
                                scrollViewReader.scrollTo(screen, anchor: .center)
                            }.onVolumeChange {
                                opened = false
                            }
                            Spacer().fillMaxHeight()
                        }
                    }
                    Spacer().fillMaxHeight()

                    if let systemHelper = systemHelper {
                        NavigationLink(isActive: $opened) {
                            AppContainer(
                                content: TestingScreens(selectedScreen: selectedScreen)
                                    .navigationBarHidden(true)
                            ).environmentObject(systemHelper)
                        } label: {
                            Text("Open")
                        }
                    }
                    Spacer().fillMaxHeight()
                    Spacer().fillMaxHeight()
                }.navigationBarHidden(true)
            }.introspectViewController { viewController in
                if viewController == self.viewController {
                    return
                }
                self.viewController = viewController
                systemHelper = SystemHelper(
                    systemContext: .init(
                        rootController: viewController,
                        application: UIApplication.shared,
                        launchOptions: nil,
                        cacheImage: { _, _ in }
                    )
                )
            }
        }
    }
}

extension SystemHelper: ObservableObject {
    
}

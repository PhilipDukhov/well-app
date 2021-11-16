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
    var selectedScreen: Screen = .initial
    
    @State
    var opened = true

    @State
    var contextHelper: ContextHelper?

    @State
    var viewController: UIViewController?

    init() {
        NapierProxy.shared.initializeLogging()
        UINavigationController.swizzleIsNavigationBarHiddenImplementation
    }
    
    var body: some Scene {
        WindowGroup {
            NavigationView {
                VStack {
                    ScrollViewReader { scrollViewReader in
                        ScrollView(.horizontal, showsIndicators: false) {
                            LazyHStack {
                                ForEach(Screen.allCases, id: \.self) { screen in
                                    Button(screen.rawValue.capitalized) {
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

                    if contextHelper != nil {
                        NavigationLink(isActive: $opened) {
                            AppContainer(
                                content: TestingScreens(selectedScreen: selectedScreen)
                                    .navigationBarHidden(true)
                            ).environmentObject(contextHelper!)
                            
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
                contextHelper = ContextHelper(
                    appContext: AppContext(
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

extension ContextHelper: ObservableObject {
    
}

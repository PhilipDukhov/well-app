//
//  LocalTestApp.swift
//  LocalTest
//
//  Created by Phil on 25.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

@main
struct LocalTestApp: App {
    @State
    var selectedScreen: Screen = .availabilityCalendar
    
    @State
    var opened = true
    
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
                    NavigationLink(isActive: $opened) {
                        AppContainer(content: TestingScreens(selectedScreen: selectedScreen).navigationBarHidden(true))
                    } label: {
                        Text("Open")
                    }
                    Spacer().fillMaxHeight()
                    Spacer().fillMaxHeight()
                }.navigationBarHidden(true)
            }
        }
    }
}

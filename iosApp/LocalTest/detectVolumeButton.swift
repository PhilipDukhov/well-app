//
//  detectVolumeButton.swift
//  LocalTest
//
//  Created by Phil on 25.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import AVFoundation
import SwiftUI

extension View {
    func onVolumeChange(perform: @escaping () -> Void) -> some View {
        overlay(OnVolumeChange(perform: perform))
    }
}

private struct OnVolumeChange: View {
    let perform: () -> Void
    
    @State
    private var outputVolumeObserve: NSKeyValueObservation?
    
    var body: some View {
        Rectangle().foregroundColor(.clear)
            .onAppear {
                do {
                    try AVAudioSession.sharedInstance().setActive(true)
                } catch {}
                outputVolumeObserve = AVAudioSession.sharedInstance().observe(\.outputVolume) { _, _ in
                    perform()
                }
            }
        
        EmptyView()
    }
}

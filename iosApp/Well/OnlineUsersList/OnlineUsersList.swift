//
//  OnlineUsersList.swift
//  Well
//
//  Created by Philip Dukhov on 12/3/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import Shared
import Combine

struct OnlineUsersList: View {
    @ObservedObject var viewModel: ViewModel
    
    var body: some View {
        Image(uiImage: UIImage(systemName: "shield.fill")!)
            .renderingMode(.template)
            .foregroundColor(.init(viewModel.state.accentColor))
        List(viewModel.users, id: \.id) { user in
            UserCell(viewModel: user)
                .onTapGesture {
                    print(user)
                }
        }.onAppear {
            viewModel.subscribeOnOnlineUsers()
        }
    }
}

extension OnlineNotifier.State {
    var accentColor: UIColor {
        switch self {
        case .notconnected:
            return .gray
            
        case .connected:
            return .green
            
        default: fatalError()
        }
    }
}

extension OnlineUsersList {
    class ViewModel: ObservableObject {
        @Published private(set) var state: OnlineNotifier.State = .notconnected
        @Published private(set) var users: [ServerModelsUser] = []
        
        private let onlineNotifier: OnlineNotifier
        
        init(token: String) {
            onlineNotifier = OnlineNotifier(token: token)
        }
        
        func subscribeOnOnlineUsers() {
            onlineNotifier.state.watch { [weak self] state in
                DispatchQueue.main.async {
                    self?.state = state!
                }
            }
            
            onlineNotifier.subscribeOnOnlineUsers { [weak self] users in
                DispatchQueue.main.async {
                    self?.users = users
                }
            }
        }
    }
}

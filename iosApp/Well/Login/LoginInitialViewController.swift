//
//  InitialViewController.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit
import PinLayout
#if !AUTH_DISABLED
import Auth

class LoginInitialViewController: UIViewController {
    struct Props {
        let socialNetworkAction: ActionWith<SocialNetwork>
        let createAccountAction: Action
        let signInAction: Action
    }
    
    private let loginSocialsView = LoginSocialsView()
    private let createAccountButton = ActionButton().apply {
        $0.setTitle("Create an account", for: .normal)
        $0.titleLabel?.font = UIFont.preferredFont(forTextStyle: .title2)
        $0.setTitleColor(R.color.darkBlue(), for: .normal)
        $0.backgroundColor = .white
        $0.layer.cornerRadius = 14
    }
    private let signInContainer = UIView()
    private let alreadyHaveAccountLabel = UILabel().apply {
        $0.text = "Already have an account?"
        $0.font = UIFont.preferredFont(forTextStyle: .subheadline)
        $0.textColor = .white
    }
    private var signInButton = ActionButton().apply {
        $0.setTitle("Sign in", for: .normal)
        $0.titleLabel?.font = UIFont.preferredFont(forTextStyle: .headline)
        $0.titleLabel?.textColor = .white
    }
    
    var props: Props? {
        didSet {
            guard isViewLoaded else { return }
            propsUpdated()
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        [alreadyHaveAccountLabel,
         signInButton,
        ].forEach(signInContainer.addSubview)
        [createAccountButton,
         signInContainer,
         loginSocialsView
        ].forEach(view.addSubview)
        propsUpdated()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        loginSocialsView.pin
            .bottom(view.pin.safeArea)
            .height(48)
            .hCenter()
            .width(75%)
            .marginBottom(20)
        
        alreadyHaveAccountLabel.pin
            .sizeToFit()
        signInButton.pin
            .minHeight(45)
            .marginLeft(3)
            .sizeToFit(.widthFlexible)
            .right(of: alreadyHaveAccountLabel, aligned: .center)
        signInContainer.pin
            .above(of: loginSocialsView)
            .margin(20, 0, 44, 0)
            .hCenter()
            .wrapContent()
        
        createAccountButton.pin
            .above(of: signInContainer)
            .height(57)
            .hCenter()
            .width(85%)
    }
    
    private func propsUpdated() {
        guard let props = props else { return }
        loginSocialsView.action = props.socialNetworkAction
        createAccountButton.action = props.createAccountAction
        signInButton.action = props.signInAction
    }
}
#endif

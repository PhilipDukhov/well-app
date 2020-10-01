//
//  LoginSocialsView.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit

class LoginSocialsView: UIStackView {
    private var socialButtons: [ActionButton]!
    
    var action: ActionWith<SocialNetwork>?
    
    init() {
        super.init(frame: .zero)
        socialButtons = SocialNetwork.allCases.map { social -> ActionButton in
            let button = ActionButton()
            button.setImage(social.image, for: .normal)
            button.action = .init { [weak self] in
                self?.action?.execute(social)
            }
            return button
        }
        socialButtons.forEach(addArrangedSubview)
        distribution = .equalSpacing
    }
    
    required init(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
}

extension SocialNetwork {
    fileprivate var image: UIImage? {
        let images = R.image.loginSocials.self
        switch self {
        case .apple:
            return images.apple()
        case .twitter:
            return images.twitter()
        case .facebook:
            return images.facebook()
        case .google:
            return images.google()
        }
    }
}

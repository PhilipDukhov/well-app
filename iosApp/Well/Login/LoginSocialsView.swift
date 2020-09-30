//
//  LoginSocialsView.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit

class LoginSocialsView: UIStackView {
    enum Social: CaseIterable {
        case apple
        case twitter
        case facebook
        case google
        
        var image: UIImage? {
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
    
    private let socialButtons: [ActionButton]
    
    var action: ActionWith<Social>?
    
    init() {
        socialButtons = Social.allCases.map { social -> ActionButton in
            let button = ActionButton()
            button.setImage(social.image, for: .normal)
            return button
        }
        super.init(frame: .zero)
        socialButtons.forEach(addArrangedSubview)
        zip(Social.allCases, socialButtons).forEach { social, button in
            button.action = .init { [weak self] in
                self?.action?.execute(social)
            }
        }
        distribution = .equalSpacing
    }
    
    required init(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
}

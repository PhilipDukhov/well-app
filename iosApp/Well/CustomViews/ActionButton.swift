//
//  ActionButton.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import UIKit

final class ActionButton: UIButton {
    var action: Action?
    
    init(action: Action? = nil) {
        self.action = action
        super.init(frame: .zero)
        addTarget(self, action: #selector(touchUpInside(button:)), for: .touchUpInside)
    }
    
    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    
    @objc func touchUpInside(button: UIButton) {
        action?.execute()
    }
}

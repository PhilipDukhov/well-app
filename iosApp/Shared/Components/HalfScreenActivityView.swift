//
//  HalfScreenActivityView.swift
//  Well
//
//  Created by Phil on 04.12.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct HalfScreenActivityView: UIViewControllerRepresentable {
    var activityItems: [Any]
    var applicationActivities: [UIActivity]?
    @Binding var isPresented: Bool

    func makeUIViewController(context: Context) -> ActivityViewWrapper {
        ActivityViewWrapper(
            activityItems: activityItems,
            applicationActivities: applicationActivities,
            isPresented: isPresented) {
                $isPresented.wrappedValue = $0
            }
    }

    func updateUIViewController(_ uiViewController: ActivityViewWrapper, context: Context) {
        uiViewController.isPresented = isPresented
        uiViewController.activityItems = activityItems
    }
}

final class ActivityViewWrapper: UIViewController {
    var activityItems: [Any]
    var applicationActivities: [UIActivity]?

    var isPresented: Bool {
        didSet {
            updateState()
        }
    }
    let setIsPresented: (Bool) -> Void

    init(
        activityItems: [Any],
        applicationActivities: [UIActivity]? = nil,
        isPresented: Bool,
        setIsPresented: @escaping (Bool) -> Void
    ) {
        self.activityItems = activityItems
        self.applicationActivities = applicationActivities
        self.isPresented = isPresented
        self.setIsPresented = setIsPresented
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func didMove(toParent parent: UIViewController?) {
        super.didMove(toParent: parent)
        updateState()
    }

    private func updateState() {
        guard parent != nil else {return}
        let isActivityPresented = presentedViewController != nil
        if isActivityPresented != isPresented {
            if !isActivityPresented {
                let controller = UIActivityViewController(activityItems: activityItems, applicationActivities: applicationActivities)
                controller.completionWithItemsHandler = { [weak self] _, _, _, _ in
                    self?.setIsPresented(false)
                }
                present(controller, animated: true, completion: nil)
            } else {
                self.presentedViewController?.dismiss(animated: true, completion: nil)
            }
        }
    }
}

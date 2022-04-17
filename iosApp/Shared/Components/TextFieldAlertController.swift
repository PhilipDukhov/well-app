//
//  AlertController.swift
//  Well
//
//  Created by Phil on 04.04.2022.
//  Copyright © 2022 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct TextFieldAlertInfo {
    let title: String
    let placeholder: String
}

extension View {
    @ViewBuilder
    func textFieldAlertController(
        isPresented: Binding<Bool>,
        info: TextFieldAlertInfo,
        alertConfirmed: @escaping (String) -> Void
    ) -> some View {
        background(
            TextFieldAlertWrapper(
                isPresented: isPresented,
                info: info,
                alertConfirmed: alertConfirmed
            )
        )
    }
}

private struct TextFieldAlertWrapper: UIViewControllerRepresentable {
    @Binding
    var isPresented: Bool
    let info: TextFieldAlertInfo
    let alertConfirmed: (String) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        UIViewController()
    }

    final class Coordinator: NSObject, UITextFieldDelegate {
        struct AlertData {
            let alertController: UIAlertController
            let okAction: UIAlertAction
        }
        var alertData: AlertData?

        func textField(
            _ textField: UITextField,
            shouldChangeCharactersIn range: NSRange,
            replacementString string: String
        ) -> Bool {
            let finalText = ((textField.text ?? "") as NSString).replacingCharacters(in: range, with: string)
            alertData?.okAction.isEnabled = finalText.isNotBlank
            return true
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func updateUIViewController(
        _ uiViewController: UIViewController,
        context: Context
    ) {
        if isPresented && uiViewController.presentedViewController == nil {
            let alert = UIAlertController(title: info.title, message: "", preferredStyle: .alert)
            let okAction = UIAlertAction(title: GlobalStringsBase.companion.shared.ok, style: .default) { _ in
                isPresented = false
                context.coordinator.alertData = nil
                guard let text = alert.textFields?.first?.text else { return }
                alertConfirmed(text)
            }
            okAction.isEnabled = false
            alert.addTextField { textField in
                textField.placeholder = info.placeholder
                textField.delegate = context.coordinator
            }
            alert.addAction(okAction)
            alert.addAction(
                UIAlertAction(title: GlobalStringsBase.companion.shared.cancel, style: .cancel) { _ in
                    isPresented = false
                    context.coordinator.alertData = nil
                }
            )

            context.coordinator.alertData = .init(alertController: alert, okAction: okAction)

            uiViewController.present(alert, animated: true)
        }
        if !isPresented && uiViewController.presentedViewController == context.coordinator.alertData?.alertController {
            uiViewController.dismiss(animated: true)
            context.coordinator.alertData = nil
        }
    }
}

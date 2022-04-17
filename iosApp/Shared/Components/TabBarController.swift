//
//  TabBarController.swift
//  Well
//
//  Created by Phil on 08.12.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct TabBarController<TabContent: View, Item, Tab: Hashable>: UIViewControllerRepresentable {
    let items: [Item]
    let tabKeyPath: KeyPath<Item, Tab>
    let selectedTab: Tab
    let setSelectedTab: (Tab) -> Void
    let tabBarItem: (Tab) -> UITabBarItem
    let badgeValue: (Item) -> String?
    @ViewBuilder
    let contentView: (Item) -> TabContent

    func makeUIViewController(context: Context) -> UITabBarController {
        let controller = UITabBarController()
        if #available(iOS 15.0, *) {
            controller.tabBar.scrollEdgeAppearance = UITabBarAppearance()
        }
        controller.delegate = context.coordinator
        return controller
    }

    func updateUIViewController(_ uiViewController: UITabBarController, context: Context) {
        let tabs = items.map { $0[keyPath: tabKeyPath] }
        context.coordinator.viewControllers
            .keys
            .filterNot(tabs.contains(_:))
            .forEach { removedKey in
                context.coordinator.viewControllers.removeValue(forKey: removedKey)
            }
        uiViewController.viewControllers = items.map { item in
            let tab = item[keyPath: tabKeyPath]
            let rootView = contentView(item)
            let viewController = context.coordinator.viewControllers[tab] ?? {
                let viewController = UIHostingController(rootView: rootView)
                viewController.tabBarItem = tabBarItem(tab)
                context.coordinator.viewControllers[tab] = viewController
                return viewController
            }()
            viewController.rootView = rootView
            viewController.tabBarItem.badgeValue = badgeValue(item)
            return viewController
        }
        uiViewController.selectedIndex = tabs.firstIndex(of: selectedTab) ?? 0
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(setSelectedTab: setSelectedTab)
    }

    final class Coordinator: NSObject, UITabBarControllerDelegate {
        let setSelectedTab: (Tab) -> Void
        var viewControllers = [Tab: UIHostingController<TabContent>]()

        init(setSelectedTab: @escaping (Tab) -> Void) {
            self.setSelectedTab = setSelectedTab
        }

        func tabBarController(_ tabBarController: UITabBarController, didSelect viewController: UIViewController) {
            guard let newTab = viewControllers.first(where: { $0.value == viewController })?.key else {
                print("tabBarController:didSelect: unexpected")
                return
            }
            setSelectedTab(newTab)
        }
    }
}

//
//  PageView.swift
//  Well
//
//  Created by Phil on 16.11.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct PageView<Page: Hashable, PageView: View>: View {
    @Binding
    private var selection: Page
    private let pages: [Page]
    private let pageViews: [PageView]

    init(
        selection: Binding<Page>,
        pages: [Page],
        @ViewBuilder content: (Page) -> PageView
    ) {
        _selection = selection
        self.pages = pages
        self.pageViews = pages.map { content($0) }
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            PageViewController(
                pageViews: pageViews,
                currentPage: .init(
                    get: { pages.firstIndex(of: selection)! },
                    set: {
                        selection = pages[$0]
                    }
                )
            )
        }
    }
}

private struct PageViewController<PageView: View>: UIViewControllerRepresentable {
    var pageViews: [PageView]
    @Binding var currentPage: Int
    @State private var previousPage = 0

    init(
        pageViews: [PageView],
        currentPage: Binding<Int>
    ) {
        self.pageViews = pageViews
        self._currentPage = currentPage
        self.previousPage = currentPage.wrappedValue
    }

    func makeCoordinator() -> Coordinator {
        Coordinator($currentPage)
    }

    func makeUIViewController(
        context: Context
    ) -> UIPageViewController {
        let pageViewController = UIPageViewController(
            transitionStyle: .scroll,
            navigationOrientation: .horizontal
        )
        pageViewController.dataSource = context.coordinator
        pageViewController.delegate = context.coordinator
        for view in pageViewController.view.subviews {
            if let scrollView = view as? UIScrollView {
                scrollView.isScrollEnabled = false
            }
        }
        return pageViewController
    }

    func updateUIViewController(
        _ pageViewController: UIPageViewController,
        context: Context
    ) {
        guard !pageViews.isEmpty else {
            return
        }
        context.coordinator.updateViews(views: pageViews)
        pageViewController.setViewControllers(
            [context.coordinator.controllers[currentPage]],
            direction: previousPage < currentPage ? .forward : .reverse,
            animated: true
        ) { _ in
            DispatchQueue.main.async {
                previousPage = currentPage
            }
        }
    }

    final class Coordinator: NSObject, UIPageViewControllerDataSource, UIPageViewControllerDelegate {
        @Binding
        private var currentPage: Int
        var controllers = [UIHostingController<PageView>]()

        init(_ currentPage: Binding<Int>) {
            _currentPage = currentPage
        }

        func updateViews(views: [PageView]) {
            views.enumerated().forEach { i, view in
                if i < controllers.count {
                    controllers[i].rootView = view
                } else {
                    controllers.append(UIHostingController(rootView: view))
                }
            }
            let diff = controllers.count - views.count
            if diff > 0 {
                controllers.removeLast(diff)
            }
        }

        func pageViewController(
            _ pageViewController: UIPageViewController,
            viewControllerBefore viewController: UIViewController
        ) -> UIViewController? {
            guard
                let currentIndex = index(of: viewController),
                case let resultIndex = currentIndex - 1,
                controllers.indices.contains(resultIndex)
            else {
                return nil
            }
            return controllers[resultIndex]
        }

        func pageViewController(
            _ pageViewController: UIPageViewController,
            viewControllerAfter viewController: UIViewController
        ) -> UIViewController? {
            guard
                let currentIndex = index(of: viewController),
                case let resultIndex = currentIndex + 1,
                controllers.indices.contains(resultIndex)
            else {
                return nil
            }
            return controllers[resultIndex]
        }

        func pageViewController(
            _ pageViewController: UIPageViewController,
            didFinishAnimating finished: Bool,
            previousViewControllers: [UIViewController],
            transitionCompleted completed: Bool
        ) {
            if completed,
               let visibleViewController = pageViewController.viewControllers?.first,
               let index = index(of: visibleViewController)
            {
                currentPage = index
            }
        }

        private func index(of viewController: UIViewController) -> Int? {
            (viewController as? UIHostingController<PageView>)
                .flatMap(controllers.firstIndex)
        }
    }
}

//
//  NavigationBarBackground.swift
//  Well
//
//  Created by Philip Dukhov on 2/12/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct NavigationBarItem<V: View> {
    let view: V
    let enabled: Bool
    let handler: (() -> Void)?

    init(view: V, enabled: Bool = true, handler: (() -> Void)? = nil) {
        self.view = view
        self.enabled = enabled
        self.handler = handler
    }

    init(text: String, enabled: Bool = true, handler: (() -> Void)? = nil) where V == Text {
        self.view = itemTextView(text)
        self.enabled = enabled
        self.handler = handler
    }
}

struct NavigationBar<Title: View, LV: View, RV: View>: View {
    let title: Title
    let leftItem: NavigationBarItem<LV>?
    let rightItem: NavigationBarItem<RV>?
    let minContentHeight: CGFloat?

    init(
        title: String
    ) where Title == Text, RV == EmptyView, LV == EmptyView {
        self.title = Self.createTitle(title)
        self.leftItem = nil
        self.rightItem = nil
        self.minContentHeight = nil
    }

    init(
        title: Title
    ) where RV == EmptyView, LV == EmptyView {
        self.title = title
        self.leftItem = nil
        self.rightItem = nil
        self.minContentHeight = nil
    }

    init(
        title: String,
        leftItem: NavigationBarItem<LV>?,
        rightItem: NavigationBarItem<RV>?
    ) where Title == Text {
        self.title = Self.createTitle(title)
        self.leftItem = leftItem
        self.rightItem = rightItem
        self.minContentHeight = nil
    }

    init(
        title: String,
        leftItem: NavigationBarItem<LV>?
    ) where RV == EmptyView, Title == Text {
        self.title = Self.createTitle(title)
        self.leftItem = leftItem
        self.rightItem = nil
        self.minContentHeight = nil
    }

    init(
        title: String,
        rightItem: NavigationBarItem<RV>?
    ) where LV == EmptyView, Title == Text {
        self.title = Self.createTitle(title)
        self.leftItem = nil
        self.rightItem = rightItem
        self.minContentHeight = nil
    }

    init(
        rightItem: NavigationBarItem<RV>?,
        minContentHeight: CGFloat? = nil
    ) where LV == EmptyView, Title == EmptyView {
        self.title = EmptyView()
        self.leftItem = nil
        self.rightItem = rightItem
        self.minContentHeight = minContentHeight
    }

    init(
        leftItem: NavigationBarItem<LV>?,
        rightItem: NavigationBarItem<RV>?
    ) where Title == EmptyView {
        self.title = EmptyView()
        self.leftItem = leftItem
        self.rightItem = rightItem
        self.minContentHeight = nil
    }

    private static func createTitle(_ title: String) -> Text {
        Text(title.isEmpty ? " " : title)
    }

    var body: some View {
        ZStack(alignment: .top) {
            title
                .style(.title2)
                .frame(height: controlMinSize)
            HStack {
                leftItem.map(control)
                Spacer()
                rightItem.map(control)
            }
        }.padding(.horizontal).padding(.vertical, 5)
            .frame(minHeight: max(minContentHeight ?? 0, controlMinSize))
            .fillMaxWidth()
            .foregroundColor(.white)
            .background(GradientView(gradient: .main).edgesIgnoringSafeArea(.top))
    }

    private func control<V>(item: NavigationBarItem<V>) -> some View {
        Control(item.view, enabled: item.enabled, onTap: item.handler ?? {
        })
    }
}

struct ModeledNavigationBar<Msg: AnyObject>: View {
    let model: NavigationBarModel<Msg>
    let listener: (Msg) -> Void

    var body: some View {
        NavigationBar(
            title: model.title,
            leftItem: model.leftItem.map { leftItem in
                NavigationBarItem(
                    view: leftItem.content.view(),
                    enabled: leftItem.enabled,
                    handler: leftItem.msg.map { msg in
                        {
                            listener(msg)
                        }
                    }
                )
            },
            rightItem: model.rightItem.map { rightItem in
                NavigationBarItem(
                    view: rightItem.content.view(),
                    enabled: rightItem.enabled,
                    handler: rightItem.msg.map { msg in
                        {
                            listener(msg)
                        }
                    }
                )
            }
        )
    }
}

extension NavigationBarModelItemContent {
    func view() -> some View {
        switch self {
        case let content as NavigationBarModelItemContent.Text:
            return AnyView(itemTextView(content.text))

        case is NavigationBarModelItemContent.ActivityIndicator:
            return AnyView(Well.ActivityIndicator())

        case let content as NavigationBarModelItemContent.Icon:
            switch content.icon {
            case .back:
                return AnyView(Image(systemName: "chevron.left"))

            default:fatalError()
            }

        default: fatalError()
        }
    }
}

private func itemTextView(_ text: String) -> Text {
    Text(text).style(.body3)
}

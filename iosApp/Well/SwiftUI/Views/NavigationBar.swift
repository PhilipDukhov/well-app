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

    init(view: V, enabled: Bool = true, handlerOpt: (() -> Void)?) {
        self.view = view
        self.enabled = enabled
        self.handler = handlerOpt
    }

    init(text: String, enabled: Bool = true, handlerOpt: (() -> Void)?) where V == Text {
        self.view = itemTextView(text)
        self.enabled = enabled
        self.handler = handlerOpt
    }

    init(view: V, enabled: Bool = true, handler: @escaping @autoclosure () -> Void) {
        self.init(view: view, enabled: enabled, handlerOpt: handler)
    }

    init(text: String, enabled: Bool = true, handler: @escaping @autoclosure () -> Void) where V == Text {
        self.init(text: text, enabled: enabled, handlerOpt: handler)
    }
}

struct NavigationBar<Title: View, LV: View, RV: View>: View {
    let title: Title
    let leftItem: NavigationBarItem<LV>?
    let rightItem: NavigationBarItem<RV>?

    init(
        title: Title
    ) where RV == EmptyView, LV == EmptyView {
        self.title = title
        self.leftItem = nil
        self.rightItem = nil
    }

    init(
        title: String,
        leftItem: NavigationBarItem<LV>?,
        rightItem: NavigationBarItem<RV>?
    ) where Title == Text {
        self.title = Self.createTitle(title)
        self.leftItem = leftItem
        self.rightItem = rightItem
    }

    init(
        title: String,
        leftItem: NavigationBarItem<LV>?
    ) where RV == EmptyView, Title == Text {
        self.title = Self.createTitle(title)
        self.leftItem = leftItem
        self.rightItem = nil
    }

    init(
        title: String,
        rightItem: NavigationBarItem<RV>?
    ) where LV == EmptyView, Title == Text {
        self.title = Self.createTitle(title)
        self.leftItem = nil
        self.rightItem = rightItem
    }

    private static func createTitle(_ title: String) -> Text {
        Text(title.isEmpty ? " " : title)
    }

    var body: some View {
        ZStack(alignment: .center) {
            title
                .style(.title2)
                .frame(height: controlMinSize)
            HStack {
                leftItem.map(control)
                Spacer()
                rightItem.map(control)
            }
        }.padding(.horizontal).padding(.vertical, 5)
            .frame(minHeight: controlMinSize)
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
                    handlerOpt: leftItem.msg.map { msg in
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
                    handlerOpt: rightItem.msg.map { msg in
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

//
//  NavigationBarBackground.swift
//  Well
//
//  Created by Philip Dukhov on 2/12/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct NavigationBar<Title: View, LV: View, RV: View, ExtraContent: View>: View {
    let title: NavigationBarItem<Title>?
    let leftItem: NavigationBarItem<LV>?
    let rightItem: NavigationBarItem<RV>?
    let minContentHeight: CGFloat?
    let extraContent: ExtraContent?

    init(
        title: String
    ) where Title == Text, RV == EmptyView, LV == EmptyView, ExtraContent == EmptyView {
        self.title = Self.createTitle(title)
        self.leftItem = nil
        self.rightItem = nil
        self.minContentHeight = nil
        self.extraContent = nil
    }

    init(
        title: Title
    ) where RV == EmptyView, LV == EmptyView, ExtraContent == EmptyView {
        self.title = NavigationBarItem(view: title)
        self.leftItem = nil
        self.rightItem = nil
        self.minContentHeight = nil
        self.extraContent = nil
    }
    
    init(
        title: NavigationBarItem<Title>?,
        leftItem: NavigationBarItem<LV>?,
        rightItem: NavigationBarItem<RV>?
    ) where ExtraContent == EmptyView {
        self.title = title
        self.leftItem = leftItem
        self.rightItem = rightItem
        self.minContentHeight = nil
        self.extraContent = nil
    }
    
    init(
        title: String,
        leftItem: NavigationBarItem<LV>?,
        rightItem: NavigationBarItem<RV>?
    ) where Title == Text, ExtraContent == EmptyView {
        self.title = Self.createTitle(title)
        self.leftItem = leftItem
        self.rightItem = rightItem
        self.minContentHeight = nil
        self.extraContent = nil
    }

    init(
        title: String,
        leftItem: NavigationBarItem<LV>?,
        rightItem: NavigationBarItem<RV>?,
        extraContent: ExtraContent?
    ) where Title == Text {
        self.title = Self.createTitle(title)
        self.leftItem = leftItem
        self.rightItem = rightItem
        self.minContentHeight = nil
        self.extraContent = extraContent
    }

    init(
        title: String,
        leftItem: NavigationBarItem<LV>?
    ) where RV == EmptyView, Title == Text, ExtraContent == EmptyView {
        self.title = Self.createTitle(title)
        self.leftItem = leftItem
        self.rightItem = nil
        self.minContentHeight = nil
        self.extraContent = nil
    }

    init(
        title: String,
        rightItem: NavigationBarItem<RV>?
    ) where LV == EmptyView, Title == Text, ExtraContent == EmptyView {
        self.title = Self.createTitle(title)
        self.leftItem = nil
        self.rightItem = rightItem
        self.minContentHeight = nil
        self.extraContent = nil
    }

    init(
        rightItem: NavigationBarItem<RV>?,
        minContentHeight: CGFloat? = nil
    ) where LV == EmptyView, Title == EmptyView, ExtraContent == EmptyView {
        self.title = nil
        self.leftItem = nil
        self.rightItem = rightItem
        self.minContentHeight = minContentHeight
        self.extraContent = nil
    }

    init(
        leftItem: NavigationBarItem<LV>?,
        rightItem: NavigationBarItem<RV>?
    ) where Title == EmptyView, ExtraContent == EmptyView {
        self.title = nil
        self.leftItem = leftItem
        self.rightItem = rightItem
        self.minContentHeight = nil
        self.extraContent = nil
    }

    private static func createTitle(_ title: String) -> NavigationBarItem<Text> {
        NavigationBarItem(text: title.isEmpty ? " " : title)
    }

    var body: some View {
        VStack(spacing: 0) {
            ZStack(alignment: .top) {
                title.map(control)
                    .textStyle(.subtitle2)
                    .frame(height: controlMinSize)
                HStack {
                    leftItem.map(control)
                    Spacer()
                    rightItem.map(control)
                }
            }.padding(.vertical, 5)
                .frame(minHeight: max(minContentHeight ?? 0, controlMinSize))
                .fillMaxWidth()
            extraContent
        }.padding(.horizontal)
            .foregroundColor(.white)
            .background(GradientView(gradient: .main).edgesIgnoringSafeArea(.top))
    }

    private func control<V>(item: NavigationBarItem<V>) -> some View {
        Control(item.view, enabled: item.enabled, onTap: item.handler ?? {})
    }
}

struct ModeledNavigationBar<Msg: AnyObject, ExtraContent: View>: View {
    let model: NavigationBarModel<Msg>
    let listener: (Msg) -> Void
    let extraContent: ExtraContent?
    
    init(
        model: NavigationBarModel<Msg>,
        listener: @escaping (Msg) -> Void,
        @ViewBuilder extraContent: () -> ExtraContent
    ) {
        self.model = model
        self.listener = listener
        self.extraContent = extraContent()
    }
    
    init(
        model: NavigationBarModel<Msg>,
        listener: @escaping (Msg) -> Void
    ) where ExtraContent == EmptyView {
        self.model = model
        self.listener = listener
        self.extraContent = nil
    }

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
            },
            extraContent: extraContent
        )
    }
}

extension NavigationBarModelItemContent {
    @ViewBuilder
    func view() -> some View {
        switch self {
        case let content as NavigationBarModelItemContent.Text:
            itemTextView(content.text)

        case is NavigationBarModelItemContent.ActivityIndicator:
            ProgressView()

        case let content as NavigationBarModelItemContent.Icon:
            switch content.icon {
            case .back:
                Image(systemName: "chevron.left")

            default:fatalError()
            }

        default: fatalError()
        }
    }
}

private func itemTextView(_ text: String) -> Text {
    Text(text).textStyle(.body2)
}

struct NavigationBarItem<V: View> {
    let view: V
    let enabled: Bool
    let handler: (() -> Void)?
    
    init(
        @ViewBuilder
        viewBuilder: () -> V,
        enabled: Bool = true,
        handler: (() -> Void)? = nil
    ) {
        self.view = viewBuilder()
        self.enabled = enabled
        self.handler = handler
    }

    init(
        view: V,
        enabled: Bool = true,
        handler: (() -> Void)? = nil
    ) {
        self.view = view
        self.enabled = enabled
        self.handler = handler
    }

    init(
        text: String,
        enabled: Bool = true,
        handler: (() -> Void)? = nil
    ) where V == Text {
        self.view = itemTextView(text)
        self.enabled = enabled
        self.handler = handler
    }
}

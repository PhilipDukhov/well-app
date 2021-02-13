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

struct NavigationBar<LV: View, RV: View>: View {
    let title: String
    let leftItem: NavigationBarItem<LV>?
    let rightItem: NavigationBarItem<RV>?
    
    init(
        title: String, 
        leftItem: NavigationBarItem<LV>?,
        rightItem: NavigationBarItem<RV>?
    ) {
        self.title = title
        self.leftItem = leftItem
        self.rightItem = rightItem
    }
    
    init(
        title: String,
        leftItem: NavigationBarItem<LV>?
    ) where RV == EmptyView {
        self.title = title
        self.leftItem = leftItem
        self.rightItem = nil
    }
    
    init(
        title: String,
        rightItem: NavigationBarItem<RV>?
    ) where LV == EmptyView {
        self.title = title
        self.leftItem = nil
        self.rightItem = rightItem
    }
    
    var body: some View {
        ZStack(alignment: .center) {
            Text(title)
                .style(.title2)
                .frame(height: 45)
            HStack {
                leftItem.map(control)
                Spacer()
                rightItem.map(control)
            }
        }.padding(.horizontal).padding(.vertical, 5)
        .frame(minHeight: controlMinSize)
        .fillMaxWidth()
        .foregroundColor(.white)
        .background(background().edgesIgnoringSafeArea(.top))
    }
    
    private func control<V>(item: NavigationBarItem<V>)-> some View {
        Control(item.view, enabled: item.enabled, onTap: item.handler ?? {})
    }
    
    private func background()-> some View {
        Color(hex: 0x94C83D)
            .overlay(LinearGradient(
                gradient: Gradient(
                    stops: [
                        .init(color: Color(hex: 0x1BFFE4, alpha: 0.8), location: 0),
                        .init(color: Color(hex: 0x009BFF), location: 0.67),
                    ]
                ),
                startPoint: .init(x: -0.06529792748787552, y: -1.2714285714285714),
                endPoint: .init(x: 0.7983896522706257, y: 0.9809522356305803)
            ).opacity(0.5))
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
                    handlerOpt: leftItem.msg.map { msg in { listener(msg) } }
                )
            },
            rightItem: model.rightItem.map { rightItem  in
                NavigationBarItem(
                    view: rightItem.content.view(),
                    enabled: rightItem.enabled,
                    handlerOpt: rightItem.msg.map { msg in { listener(msg) } }
                )
            }
        )
    }
    
    private func background()-> some View {
        Color(hex: 0x94C83D)
            .overlay(LinearGradient(
                gradient: Gradient(
                    stops: [
                        .init(color: Color(hex: 0x1BFFE4, alpha: 0.8), location: 0),
                        .init(color: Color(hex: 0x009BFF), location: 0.67),
                    ]
                ),
                startPoint: .init(x: -0.06529792748787552, y: -1.2714285714285714),
                endPoint: .init(x: 0.7983896522706257, y: 0.9809522356305803)
            ).opacity(0.5))
    }
}

extension NavigationBarModelItemContent {
    func view() -> some View {
        switch self {
        case let content as NavigationBarModelItemContent.Text:
            return AnyView(itemTextView(content.text))
            
        case is NavigationBarModelItemContent.ActivityIndicator:
            return AnyView(Well.ActivityIndicator(color: .white))
            
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

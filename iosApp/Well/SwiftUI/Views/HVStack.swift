//
//  TagCloudView.swift
//  Well
//
//  Created by Philip Dukhov on 2/11/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct HVStack<Item, ItemView>: View where ItemView: View {
    let items: [Item]
    let spacing: CGFloat
    let itemBuilder: (Item) -> ItemView
    
    init(items: [Item], spacing: CGFloat = 9, itemBuilder: @escaping (Item) -> ItemView) {
        self.items = items
        self.spacing = spacing
        self.itemBuilder = itemBuilder
    }
    
    @State private var totalHeight = CGFloat.zero
    
    var body: some View {
        GeometryReader { geometry in
            HStack(spacing: 0) {
                generateContent(in: geometry)
                Spacer()
            }
        }.frame(height: totalHeight)
    }
    
    private func generateContent(in geometry: GeometryProxy) -> some View {
        var offset = CGPoint.zero
        
        return ZStack(alignment: .topLeading) {
            ForEach(Array(items.enumerated()), id: \.0) { i, item in
                let isLast = i == items.indices.last
                itemBuilder(item)
                    .alignmentGuide(.leading) { d in
                        if abs(offset.x - d.width) > geometry.size.width {
                            offset.x = 0
                            offset.y -= d.height + spacing
                        }
                        let result = offset.x
                        if isLast {
                            offset.x = 0
                        } else {
                            offset.x -= d.width + spacing
                        }
                        return result
                    }
                    .alignmentGuide(.top) { _ in
                        let result = offset.y
                        if isLast {
                            offset.y = 0
                        }
                        return result
                    }
            }
        }.background(viewHeightReader($totalHeight))
        
    }
    
    private func viewHeightReader(_ binding: Binding<CGFloat>) -> some View {
        return GeometryReader { geometry -> Color in
            let rect = geometry.frame(in: .local)
            DispatchQueue.main.async {
                binding.wrappedValue = rect.size.height
            }
            return .clear
        }
    }
}

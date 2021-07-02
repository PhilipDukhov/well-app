//
//  AsyncImage.swift
//  AsyncImage
//
//  Created by Vadym Bulavin on 2/13/20.
//  Copyright © 2020 Vadym Bulavin. All rights reserved.
//

import SwiftUI

struct AsyncImage<Placeholder: View, ResImage: View>: View, Equatable {
    typealias ImageGenerator = (UIImage) -> ResImage
    @ObservedObject private var loader: ImageLoader

    init(
        url: URL,
        @ViewBuilder placeholder: () -> Placeholder,
        @ViewBuilder image: @escaping ImageGenerator
    ) {
        loader = ImageLoader(url: url)
        self.placeholder = placeholder()
        self.image = image
    }

    static func ==(lhs: Self, rhs: Self) -> Bool {
        lhs.loader.url == rhs.loader.url
    }

    var body: some View {
        content
            .onAppear(perform: loader.load)
            .onDisappear(perform: loader.cancel)
    }

    private let placeholder: Placeholder
    private let image: ImageGenerator

    @ViewBuilder
    private var content: some View {
        printUI("AsyncImage \(loader.image)")
        if let image = loader.image {
            self.image(image)
        } else {
            placeholder
        }
    }
}

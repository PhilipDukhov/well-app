//
//  AsyncImage.swift
//  AsyncImage
//
//  Created by Vadym Bulavin on 2/13/20.
//  Copyright © 2020 Vadym Bulavin. All rights reserved.
//

import SwiftUI

struct AsyncImage<Placeholder: View>: View {
    @ObservedObject private var loader: ImageLoader

    init(
        url: URL,
        cache: ImageCache? = nil,
        @ViewBuilder placeholder: () -> Placeholder,
        @ViewBuilder image: @escaping (UIImage) -> Image
        // = Image.init(uiImage:)
    ) {
        loader = ImageLoader(url: url, cache: cache)
        self.placeholder = placeholder()
        self.image = image
    }

    var body: some View {
        content
            .onAppear(perform: loader.load)
            .onDisappear(perform: loader.cancel)
    }

    private let placeholder: Placeholder
    private let image: (UIImage) -> Image

    @ViewBuilder
    private var content: some View {
        if let image = loader.image {
            self.image(image)
        } else {
            placeholder
        }
    }
}

//
//  ImageLoader.swift
//  AsyncImage
//
//  Created by Vadym Bulavin on 2/13/20.
//  Copyright © 2020 Vadym Bulavin. All rights reserved.
//

import Combine
import SwiftUI

final class ImageLoader: ObservableObject {
    @Published var image: UIImage?

    private(set) var isLoading = false

    let url: URL
    private var cancellable: AnyCancellable?

    private static let imageProcessingQueue = DispatchQueue(label: "image-processing")

    init(url: URL) {
        self.url = url
    }

    deinit {
        cancellable?.cancel()
    }

    func load() {
        guard !isLoading else {
            return
        }
        cancellable = Environment(\.imageLoadingFactory)
            .wrappedValue
            .image(at: url)
            .handleEvents(receiveSubscription: { [weak self] _ in
                self?.onStart()
            },
                receiveCompletion: { [weak self] _ in
                    self?.onFinish()
                },
                receiveCancel: { [weak self] in
                    self?.onFinish()
                })
            .subscribe(on: Self.imageProcessingQueue)
            .receive(on: DispatchQueue.main)
            .assign(to: \.image, on: self)
    }

    func cancel() {
        cancellable?.cancel()
    }

    private func onStart() {
        isLoading = true
    }

    private func onFinish() {
        isLoading = false
    }

    static func cache(_ image: UIImage, _ url: URL) {
        Environment(\.imageLoadingFactory)
            .wrappedValue.cache(image, url)
    }
}

private struct ImageLoadingFactoryKey: EnvironmentKey {
    static let defaultValue = ImageLoadingFactory()
}

extension EnvironmentValues {
    fileprivate var imageLoadingFactory: ImageLoadingFactory {
        get {
            self[ImageLoadingFactoryKey.self]
        }
        set {
            self[ImageLoadingFactoryKey.self] = newValue
        }
    }
}

private final class ImageLoadingFactory {
    private var processingRequests = [URL: AnyPublisher<UIImage?, Never>]()
    private static let cache = URLCache(
        memoryCapacity: 10_000_000,
        diskCapacity: 1000_000_000,
        directory: URL(fileURLWithPath: NSTemporaryDirectory()).appendingPathComponent("ImageLoadingFactoryCache")
    )
    private static let session = URLSession(
        configuration: {
            let config = URLSessionConfiguration.default
            config.urlCache = cache
            return config
        }()
    )

    func image(at url: URL) -> AnyPublisher<UIImage?, Never> {
        processingRequests[url]
        ?? newRequest(url)
    }

    private func newRequest(_ url: URL) -> AnyPublisher<UIImage?, Never> {
        let newPublisher = Self.session.dataTaskPublisher(for: url)
            .map {
                UIImage(data: $0.data)
            }
            .replaceError(with: nil)
            .handleEvents(
                receiveCompletion: { [weak self] _ in
                    self?.onRequestFinish(url)
                },
                receiveCancel: { [weak self] in
                    self?.onRequestFinish(url)
                }
            )
            .eraseToAnyPublisher()
        processingRequests[url] = newPublisher
        return newPublisher
    }

    private func onRequestFinish(_ url: URL) {
        processingRequests[url] = nil
    }

    func cache(_ image: UIImage, _ url: URL) {
        Self.cache.storeCachedResponse(
            CachedURLResponse(
                response: HTTPURLResponse(
                    url: url,
                    statusCode: 200,
                    httpVersion: nil,
                    headerFields: nil
                )!, data: image.pngData()!
            ),
            for: .init(url: url)
        )
    }
}

//
// Created by Phil on 26.06.2021.
//

import SwiftUI
import SharedMobile

struct SharedImage<Placeholder: View>: View {
    let image: SharedMobile.SharedImage?
    let placeholder: Placeholder
    let aspectRatio: CGFloat?
    let contentMode: ContentMode

    init(
        image: SharedMobile.SharedImage?,
        placeholder: Placeholder,
        aspectRatio: CGFloat? = nil,
        contentMode: ContentMode = .fill
    ) {
        self.image = image
        self.placeholder = placeholder
        self.aspectRatio = aspectRatio
        self.contentMode = contentMode
    }

    init(
        url: NSURL,
        placeholder: Placeholder,
        aspectRatio: CGFloat? = nil,
        contentMode: ContentMode = .fill
    ) {
        self.image = UrlImage(url: url.absoluteString!)
        self.placeholder = placeholder
        self.aspectRatio = aspectRatio
        self.contentMode = contentMode
    }

    var body: some View {
        if let image = image {
            switch image {
            case let image as UrlImage where image.url.toURL() != nil:
                Napier.printUI("AsyncImage")
                AsyncImage(
                    url: image.url.toURL()!,
                    placeholder: {
                        ZStack {
                            placeholder
                            ActivityIndicator()
                        }
                    },
                    image: {
                        buildImage(uiImage: $0)
                    }
                )

            case let image as ImageContainer:
                buildImage(uiImage: image.uiImage)

            default: fatalError()
            }
        } else {
            placeholder
        }
    }

    private func buildImage(uiImage: UIImage) -> some View {
        Image(uiImage: uiImage)
            .resizable()
            .aspectRatio(contentMode: contentMode)
    }
}

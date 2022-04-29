//
// Created by Philip Dukhov on 12/31/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct ProfileImage: View {
    let image: SharedMobile.SharedImage?
    let isOnline: Bool
    let clipCircle: Bool
    let aspectRatio: CGFloat?
    let contentMode: ContentMode
    let initials: String?

    init(
        _ user: User,
        clipCircle: Bool = true,
        aspectRatio: CGFloat? = nil,
        contentMode: ContentMode = .fill
    ) {
        self.init(
            image: user.profileImage(),
            isOnline: user.isOnline,
            clipCircle: clipCircle,
            aspectRatio: aspectRatio,
            contentMode: contentMode,
            initials: user.initials
        )
    }

    init(
        image: SharedMobile.SharedImage?,
        isOnline: Bool,
        clipCircle: Bool = true,
        aspectRatio: CGFloat? = nil,
        contentMode: ContentMode = .fill,
        initials: String? = nil
    ) {
        self.image = image
        self.isOnline = isOnline
        self.clipCircle = clipCircle
        self.aspectRatio = aspectRatio
        self.contentMode = contentMode
        self.initials = initials
    }

    var body: some View {
        SharedImage(
            image: image,
            placeholder: placeholder(),
            aspectRatio: aspectRatio,
            contentMode: contentMode
        ).clipShape(UserCircleShape(isOnlineCircle: false, circlePart: clipCircle ? 1 : 0, isOnlinePart: isOnline ? 1 : 0))
            .overlay(
                UserCircleShape(isOnlineCircle: true, circlePart: clipCircle ? 1 : 0, isOnlinePart: isOnline ? 1 : 0)
                    .foregroundColorKMM(.companion.Green)
            )
            .animation(.default, value: clipCircle)
            .animation(.default, value: isOnline)
    }

    private func placeholder() -> some View {
        GeometryReader { geometry in
            ZStack {
                Rectangle().foregroundColorKMM(.companion.LightGray)
                    .aspectRatio(aspectRatio, contentMode: contentMode)
                if image == nil {
                    initials.map { initials in
                        Text(initials)
                            .font(.system(size: geometry.size.height > geometry.size.width ? geometry.size.width * 0.4 : geometry.size.height * 0.4))
                            .foregroundColor(.white)
                    }
                }
            }
        }
    }
}

private struct UserCircleShape: Shape {
    let isOnlineCircle: Bool
    var circlePart: CGFloat
    var isOnlinePart: CGFloat

    var animatableData: AnimatablePair<CGFloat, CGFloat>  {
        get { AnimatablePair(circlePart, isOnlinePart) }
        set {
            circlePart = newValue.first
            isOnlinePart = newValue.second
        }
    }

    func path(in rect: CGRect) -> SwiftUI.Path {
        Path { path in
            let side = min(rect.width, rect.height)
            let bigRadius = side / 2 * circlePart
            let size = CGSize(
                width: (rect.width - side) * (1 - circlePart) + side,
                height: (rect.height - side) * (1 - circlePart) + side
            )
            let circleRect = CGRect(
                origin: CGPoint(
                    x: rect.midX - size.width / 2,
                    y: rect.midY - size.height / 2
                ),
                size: size
            )
            // distance from circle center
            let x = bigRadius * 0.67
            let y = sqrt(bigRadius * bigRadius - x * x)

            let smallCenter = CGPoint(
                x: circleRect.maxX - bigRadius + x,
                y: circleRect.maxY - bigRadius + y
            )
            let smallRadius = (circleRect.maxY - smallCenter.y) * isOnlinePart * 1.2
            if isOnlineCircle {
                let radius = smallRadius / 1.2
                path.addRoundedRect(
                    in: .init(
                        origin: .init(
                            x: smallCenter.x - radius,
                            y: smallCenter.y - radius
                        ),
                        size: .init(
                            size: radius * 2
                        )
                    ),
                    cornerSize: CGSize(size: radius)
                )
                return
            }
            if smallRadius < 1 {
                path.addRoundedRect(
                    in: circleRect,
                    cornerSize: CGSize(size: bigRadius)
                )
            } else {
                // angle from small circle start to center
                let alpha = 4 * asin(smallRadius / 2 / bigRadius)
                // angle from 90 to small circle start
                let beta = asin(x / bigRadius)

                func subCircleCenter(xMultiplier: CGFloat, yMultiplier: CGFloat) -> CGPoint {
                    .init(
                        x: circleRect.midX + (circleRect.width / 2 - bigRadius) * xMultiplier,
                        y: circleRect.midY + (circleRect.height / 2 - bigRadius) * yMultiplier
                    )
                }

                path.addArc(
                    center: subCircleCenter(xMultiplier: 1, yMultiplier: 1),
                    radius: bigRadius,
                    startAngle: Angle.radians(.pi / 2 - beta + alpha / 2),
                    endAngle: Angle.radians(.pi / 2),
                    clockwise: false
                )
                path.addLine(to: .init(x: circleRect.minX + bigRadius, y: circleRect.maxY))
                path.addArc(
                    center: subCircleCenter(xMultiplier: -1, yMultiplier: 1),
                    radius: bigRadius,
                    startAngle: Angle.radians(.pi / 2),
                    endAngle: Angle.radians(.pi),
                    clockwise: false
                )
                path.addLine(to: .init(x: circleRect.minX, y: circleRect.minY + bigRadius))
                path.addArc(
                    center: subCircleCenter(xMultiplier: -1, yMultiplier: -1),
                    radius: bigRadius,
                    startAngle: Angle.radians(.pi),
                    endAngle: Angle.radians(.pi * 3 / 2),
                    clockwise: false
                )
                path.addLine(to: .init(x: circleRect.maxX - bigRadius, y: circleRect.minY))
                path.addArc(
                    center: subCircleCenter(xMultiplier: 1, yMultiplier: -1),
                    radius: bigRadius,
                    startAngle: Angle.radians(.pi * 3 / 2),
                    endAngle: Angle.radians(0),
                    clockwise: false
                )
                path.addLine(to: .init(x: circleRect.maxX, y: circleRect.maxY - bigRadius))
                path.addArc(
                    center: subCircleCenter(xMultiplier: 1, yMultiplier: 1),
                    radius: bigRadius,
                    startAngle: Angle.radians(0),
                    endAngle: Angle.radians(.pi / 2 - beta - alpha / 2),
                    clockwise: false
                )
                path.addArc(
                    center: smallCenter,
                    radius: smallRadius,
                    startAngle: Angle.radians(3 * .pi / 2 + (.pi / 2 - beta - alpha / 4)),
                    endAngle: Angle.radians(3 * .pi / 2 - (.pi / 2 + beta - alpha / 4)),
                    clockwise: true
                )
            }
        }
    }
}

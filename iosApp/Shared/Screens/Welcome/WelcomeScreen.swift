//
//  WelcomeScreen.swift
//  Well
//
//  Created by Philip Dukhov on 4/27/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct WelcomeScreen: View {
    let state: WelcomeFeature.State
    let listener: (WelcomeFeature.Msg) -> Void

    @State
    var selection: Int = 0

    var body: some View {
        VStack(spacing: 0) {
            topImagesView
            welcomeInfoView
        }
    }

    private let cornerRadius: CGFloat = 20

    var topImagesView: some View {
        GeometryReader { geometry in
            let offset = geometry.safeAreaInsets.top
            let size = CGSize(
                width: geometry.size.width,
                height: max(geometry.size.width * 1084 / 929, geometry.size.height) + offset + cornerRadius
            )
            TabView(selection: $selection.animation()) {
                ForEachIndexed(state.descriptions) { i, _ in
                    Image("welcome/welcome_\(i)")
                        .resizable()
                        .scaledToFill()
                        .clipped()
                        .offset(y: -offset)
                }
            }.tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
                .overlay(GradientView(gradient: .welcome))
                .frame(size: size)
                .offset(y: -offset)
        }
    }

    var welcomeInfoView: some View {
        VStack(spacing: 0) {
            Text(state.title)
                .textStyle(.h4)
                .foregroundColorKMM(.companion.DarkBlue)
                .padding(.top)
            AutoLayoutTextPageView(
                Array(state.descriptions.enumerated()),
                id: \.offset,
                selection: $selection.animation()
            ) { text in
                Text(text.element)
                    .textStyle(.body1)
                    .multilineTextAlignment(.center)
                    .padding()
            }.foregroundColor(.black)
                .font(.callout)
            let isLast = state.descriptions.indices.last == selection
            Button {
                withAnimation {
                    if !isLast {
                        selection += 1
                    } else {
                        listener(WelcomeFeature.MsgContinue())
                    }
                }
            } label: {
                Text("Next")
            }.buttonStyle(ActionButtonStyle(style: .onWhite))
                .padding(.horizontal)
            Button {
                listener(WelcomeFeature.MsgContinue())
            } label: {
                Text("Skip all")
                    .padding()
                    .opacity(isLast ? 0 : 1)
            }
        }
            .background(
                SwiftUI.Color.white
                    .edgesIgnoringSafeArea(.all)
                    .cornerRadius(cornerRadius, corners: [.topLeft, .topRight])
            )
    }
}

private struct AutoLayoutTextPageView<
                                     Data: RandomAccessCollection,
                                     ID: Hashable,
                                     Content: View
                                     >: View {
    let data: Data
    let content: (Data.Element) -> Content
    let id: KeyPath<Data.Element, ID>
    let selection: Binding<ID>

    public init(_ data: Data, id: KeyPath<Data.Element, ID>, selection: Binding<ID>, @ViewBuilder content: @escaping (Data.Element) -> Content) {
        self.data = data
        self.content = content
        self.id = id
        self.selection = selection
    }

    var body: some View {
        ZStack {
            ForEach(data, id: id) {
                content($0)
                    .padding(.bottom, 39)
                    .layoutPriority(1)
                    .hidden()
            }
            TabView(selection: selection) {
                ForEach(data, id: id) {
                    content($0)
                        .offset(y: -39 / 2)
                }
            }
                .tabViewStyle(PageTabViewStyle(indexDisplayMode: .always))
                .indexViewStyle(PageIndexViewStyle(backgroundDisplayMode: .always))
        }
    }
}

extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {

    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> SwiftUI.Path {
        let path = UIBezierPath(roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}

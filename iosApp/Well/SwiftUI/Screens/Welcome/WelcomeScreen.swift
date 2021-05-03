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
    
    @State
    var topSelection: Int = 0
    
    var body: some View {
        let texts = [
            "Our app provides urologists globally of performing mentored urological",
            "Our app provides urologists globally of performing mentored urological Our app provides urologists globally of performing mentored urological",
            "Our app provides urologists globally of performing mentored urological",
            "Our app provides urologists globally of performing mentored urological",
        ]
        ZStack {
            TabView(selection: $topSelection) {
                ForEach(Array(texts.enumerated()), id: \.offset) { text in
                    Rectangle()
                        .foregroundColor(text.offset % 2 == 0 ? .yellow : .gray)
                }
            }
            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
            .allowsHitTesting(false)
            VStack {
                Spacer()
                    .fillMaxHeight()
                VStack {
                    Text("Welcome to WELL app")
                        .font(.largeTitle)
                    AutoLayoutTextPageView(
                        Array(texts.enumerated()),
                        id: \.offset,
                        selection: $selection
                    ) { text in
                        Text(text.element)
                            .multilineTextAlignment(.center)
                            .padding(.vertical)
                    }.foregroundColor(.black)
                    .font(.callout)
                    let isLast = texts.indices.last == selection
                    Button(action: {
                        withAnimation {
                            if !isLast {
                                selection += 1
                            } else {
                                listener(WelcomeFeature.MsgContinue())
                            }
                        }
                    }) {
                        Text("Next")
                            .frame(height: 57)
                            .fillMaxWidth()
                            .background(ColorConstants.Green.toColor())
                            .clipShape(Capsule())
                    }
                    Button(action: {
                        listener(WelcomeFeature.MsgContinue())
                    }) {
                        Text("Skip all")
                            .padding()
                            .opacity(isLast ? 0 : 1)
                    }
                }
                .padding()
                .background(
                    SwiftUI.Color.white
                        .edgesIgnoringSafeArea(.all)
                        .cornerRadius(20, corners: [.topLeft, .topRight])
                )
            }
        }
        .onChange(of: selection, perform: { selection in
            withAnimation {
                topSelection = selection
            }
        })
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
        clipShape( RoundedCorner(radius: radius, corners: corners) )
    }
}


struct RoundedCorner: Shape {
    
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners
    
    func path(in rect: CGRect) -> SwiftUI.Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}

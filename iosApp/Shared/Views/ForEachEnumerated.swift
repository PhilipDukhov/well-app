//
// Created by Phil on 17.05.2021.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct ForEachIndexed<Data: RandomAccessCollection, Content: View, ID: Hashable>: View {
    let data: [EnumeratedSequence<Data>.Element]
    let content: (Int, Data.Element) -> Content
    let id: KeyPath<EnumeratedSequence<Data>.Element, ID>

    init(_ data: Data, @ViewBuilder content: @escaping (Int, Data.Element) -> Content) where ID == Int {
        self.data = Array(data.enumerated())
        self.content = content
        self.id = \.offset
    }
    
//    init(_ data: Data, @ViewBuilder content: @escaping (Int, Data.Element) -> Content) where Data.Element: Identifiable, ID == Data.Element.ID {
//        self.data = Array(data.enumerated())
//        self.content = content
//        self.id = \.element.id
//    }

    var body: some View {
        ForEach(data, id: id) { element in
            content(element.offset, element.element)
        }
    }
}

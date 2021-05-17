//
// Created by Phil on 17.05.2021.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct ForEachEnumerated<Data: RandomAccessCollection, Content: View>: View {
    var data: [EnumeratedSequence<Data>.Element]
    var content: (Int, Data.Element) -> Content

    init(_ data: Data, @ViewBuilder content: @escaping (Int, Data.Element) -> Content) {
        self.data = Array(data.enumerated())
        self.content = content
    }

    var body: some View {
        ForEach(data, id: \.offset) { element in
            content(element.offset, element.element)
        }
    }
}

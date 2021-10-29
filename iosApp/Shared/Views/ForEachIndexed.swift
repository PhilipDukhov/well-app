//
// Created by Phil on 17.05.2021.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct ForEachIndexed<Data: RandomAccessCollection, Content: View, ID: Hashable>: View, DynamicViewContent where Data.Index: Hashable {
    var data: [(Data.Index, Data.Element)] {
        forEach.data
    }
    
    let forEach: ForEach<[(Data.Index, Data.Element)], ID, Content>
    
    init(
        _ data: Data,
        @ViewBuilder content: @escaping (Data.Index, Data.Element) -> Content
    ) where ID == Data.Index {
        forEach = ForEach(
            Array(zip(data.indices, data)),
            id: \.0
        ) { i, _ in
            content(i, data[i])
        }
    }
    
    init(
        _ data: Data,
        id: KeyPath<Data.Element, ID>,
        @ViewBuilder content: @escaping (Data.Index, Data.Element) -> Content
    ) {
        forEach = ForEach(
            Array(zip(data.indices, data)),
            id: (\.1 as KeyPath<(Data.Index, Data.Element), Data.Element>).appending(path: id)
        ) { i, _ in
            content(i, data[i])
        }
    }
    
    var body: some View {
        forEach
    }
}

//
//  AvailabilitiesList.swift
//  Well
//
//  Created by Phil on 27.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = CurrentUserAvailabilitiesListFeature

struct AvailabilitiesList: View {
    var selectedItem: CurrentUserAvailabilitiesListFeature.StateCalendarItem?

    let allAvailabilities: [Availability]
    let onSelect: (Availability) -> Void
    let onCreate: (CurrentUserAvailabilitiesListFeature.StateCalendarItem) -> Void
    
    private static let spacing: CGFloat = 10
    private static let cellsCount = Feature.State.companion.availabilityCellsCount.toInt()

    var body: some View {
        CalculateCellLayoutInfo(
            cellsCount: Self.cellsCount,
            spacing: Self.spacing
        ) { cellLayoutInfo in
            LazyVGrid(
                columns: Array(repeating: GridItem(.fixed(cellLayoutInfo.width)), count: Self.cellsCount),
                alignment: .trailing,
                spacing: Self.spacing
            ) {
                ForEach(selectedItem?.availabilities ?? allAvailabilities, id: \.self) { availability in
                    Cell(
                        firstRowText: selectedItem.map { _ in nil } ?? availability.startDay.localizedDayAndShortMonth(),
                        secondRowText: availability.intervalString,
                        layoutInfo: cellLayoutInfo,
                        onClick: {
                            onSelect(availability)
                        }
                    )
                }
                selectedItem.map { selectedItem in
                    !selectedItem.canCreateAvailability ? nil :
                    Cell(
                        layoutInfo: cellLayoutInfo,
                        onClick: {
                            onCreate(selectedItem)
                        }
                    ) {
                        Image(systemName: "plus")
                    }
                }
            }
        }
    }
}

private struct CalculateCellLayoutInfo<Content: View>: View {
    let cellsCount: Int
    let spacing: CGFloat
    @ViewBuilder
    let content: (CellLayoutInfo) -> Content
    
    @State
    private var aspectRatio: CGFloat?
    
    var body: some View {
        GeometryReader { geometry in
            let width = ((geometry.size.width - spacing * (CGFloat(cellsCount) - 1)) / CGFloat(cellsCount)).rounded()
            if let aspectRatio = aspectRatio {
                content(.init(aspectRatio: aspectRatio, width: width))
            } else {
                Cell(firstRowText: " ", secondRowText: " ", layoutInfo: .init(aspectRatio: nil, width: width), onClick: {})
                    .background(
                        GeometryReader { backgroundGeometry in
                            Rectangle().foregroundColor(.clear)
                                .onAppear {
                                    aspectRatio = backgroundGeometry.size.aspectRatio
                                }
                        }
                    )
            }
        }
    }
}

private struct CellLayoutInfo {
    let aspectRatio: CGFloat?
    let width: CGFloat
}

private struct Cell<Content: View>: View {
    let layoutInfo: CellLayoutInfo
    let onClick: () -> Void
    @ViewBuilder
    let content: () -> Content
    
    init(
        layoutInfo: CellLayoutInfo,
        onClick: @escaping () -> Void,
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.layoutInfo = layoutInfo
        self.onClick = onClick
        self.content = content
    }
    
    init(
        firstRowText: String?,
        secondRowText: String,
        layoutInfo: CellLayoutInfo,
        onClick: @escaping () -> Void
    ) where Content == VStack<TupleView<(Text?, Text)>> {
        self.init(
            layoutInfo: layoutInfo,
            onClick: onClick,
            content: {
                VStack(spacing: 0) {
                    firstRowText.map(Text.init)
                    Text(secondRowText)
                }
            }
        )
    }

    var body: some View {
        Button(action: onClick) {
            content().textStyle(.body2)
                .foregroundColorKMM(.companion.DarkGrey)
                .padding(10)
                .minimumScaleFactor(0.01)
                .frame(width: layoutInfo.width)
                .aspectRatio(layoutInfo.aspectRatio, contentMode: .fit)
                .backgroundColorKMM(.companion.LightBlue15)
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .onTapGesture(perform: onClick)
        }
    }
}

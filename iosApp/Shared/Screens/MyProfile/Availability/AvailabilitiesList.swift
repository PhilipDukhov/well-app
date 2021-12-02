//
//  AvailabilitiesList.swift
//  Well
//
//  Created by Phil on 27.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = AvailabilitiesCalendarFeature

struct AvailabilitiesList: View {
    var selectedItem: CalendarInfoFeatureStateItem<Availability>?

    let allAvailabilities: [Availability]
    let onSelect: (Availability) -> Void
    let onCreate: (CalendarInfoFeatureStateItem<Availability>) -> Void
    let canCreateAvailability: (CalendarInfoFeatureStateItem<Availability>) -> Bool

    private static let spacing: CGFloat = 10
    private static let cellsCount = Feature.State.companion.availabilityCellsCount.toInt()

    var body: some View {
        CalculateCellLayoutInfo(
            cellsCount: Self.cellsCount,
            spacing: Self.spacing
        ) { cellLayoutInfo in
            ScrollView {
                LazyVGrid(
                    columns: Array(repeating: GridItem(.fixed(cellLayoutInfo.width)), count: Self.cellsCount),
                    alignment: .trailing,
                    spacing: Self.spacing
                ) {
                    ForEach(selectedItem?.events as? [Availability] ?? allAvailabilities, id: \.self) { availability in
                        Cell(
                            firstRowText: selectedItem.map { _ in nil } ?? availability.startDay.localizedDayAndShortMonth(),
                            secondRowText: availability.intervalString,
                            layoutInfo: cellLayoutInfo,
                            onClick: {
                                onSelect(availability)
                            }
                        )
                    }
                    if let selectedItem = selectedItem, canCreateAvailability(selectedItem) {
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
}

private struct CalculateCellLayoutInfo<Content: View>: View {
    let cellsCount: Int
    let spacing: CGFloat
    @ViewBuilder
    let content: (CellLayoutInfo) -> Content
    
    @State
    private var height: CGFloat?
    
    var body: some View {
        GeometryReader { geometry in
            let cellLayoutInfo = CellLayoutInfo(
                width: ((geometry.size.width - spacing * (CGFloat(cellsCount) - 1)) / CGFloat(cellsCount)).roundedScreenScaled(),
                height: height
            )
            if height != nil {
                content(cellLayoutInfo)
            } else {
                Cell(firstRowText: " ", secondRowText: " ", layoutInfo: cellLayoutInfo, onClick: {})
                    .background(
                        GeometryReader { backgroundGeometry in
                            Rectangle().foregroundColor(.clear)
                                .onAppear {
                                    let minHeight = backgroundGeometry.size.height
                                    let itemsCount = ((geometry.size.height + spacing) / (minHeight + spacing) + 0.5).rounded(.down) - 0.5
                                    height = ((geometry.size.height + spacing) / itemsCount - spacing).roundedScreenScaled()
                                }
                        }
                    )
            }
        }
    }
}

private struct CellLayoutInfo {
    let width: CGFloat
    let height: CGFloat?
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
                .frame(width: layoutInfo.width, height: layoutInfo.height)
                .backgroundColorKMM(.companion.LightBlue15)
                .clipShape(Shapes.medium)
                .onTapGesture(perform: onClick)
        }
    }
}

//
//  RequestConsultationView.swift
//  Well
//
//  Created by Phil on 28.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = RequestConsultationFeature

struct RequestConsultationOverlay: View {
    let state: RequestConsultationFeature.State
    
    var body: some View {
        if isVisible {
            InactiveOverlay {
                ZStack {
                    switch state.status {
                    case is Feature.StateStatusProcessing, is Feature.StateStatusLoading:
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .companion.Green))

                    case is Feature.StateStatusBooked:
                        Image(systemName: "checkmark")
                            .font(.system(size: 60, weight: .black))
                            .foregroundColorKMM(.companion.Green)
                            .padding().background(RoundedRectangle(cornerRadius: 16).foregroundColorKMM(.companion.White))

                    case let failed as Feature.StateStatusBookingFailed:
                        VStack {
                            Text(Feature.Strings.shared.bookingFailed)
                            Text(failed.reason)
                        }.padding().background(RoundedRectangle(cornerRadius: 16).foregroundColorKMM(.companion.White))

                    case is Feature.StateStatusLoading, is Feature.StateStatusLoaded:
                        EmptyView()
                    default:
                        EmptyView()
                    }
                }.padding()
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .foregroundColor(.white)
                    )
            }.animation(.default, value: state.status)
        }
    }

    private var isEmptyContent: Bool {
        switch state.status {
        case
            is Feature.StateStatusProcessing,
            is Feature.StateStatusLoading,
            is Feature.StateStatusLoaded:

            return true

        default:
            return false
        }
    }

    private var isVisible: Bool {
        switch state.status {
        case
            is Feature.StateStatusProcessing,
            is Feature.StateStatusBooked,
            is Feature.StateStatusBookingFailed:

            return true

        default:
            return false
        }
    }
}

struct RequestConsultationView: View {
    let state: RequestConsultationFeature.State
    let listener: (RequestConsultationFeature.Msg) -> Void
    
    var body: some View {
        VStack {
            content
        }
    }
    
    @ViewBuilder
    var content: some View {
        Spacer().frame(height: 16)
        Text(Feature.Strings.shared.title)
            .textStyle(.subtitle2)
        if state.status is Feature.StateStatusLoading {
            ProgressView()
        } else {
            VStack(spacing: 52) {
                Availabilities(
                    availabilities: state.availabilitiesByDay,
                    onBookNow: {
                        listener(Feature.MsgBook(availability: $0))
                    }
                )
            }
        }
    }
}

private struct Availabilities: View {
    let availabilities: [KotlinPair<LocalDate, NSArray>]
    let onBookNow: (Availability) -> Void
    
    @State
    private var selectedDayIndex = 0
    
    @State
    private var selectedTimeIndex = -1
    
    var body: some View {
        VStack(spacing: 28) {
            ScrollViewReader { scrollView in
                BookRow(
                    items: availabilities.map { $0.first! },
                    id: \.self,
                    selectedIndex: $selectedDayIndex,
                    itemText: { $0.localizedDayAndShortMonth(separator: "\n") },
                    textStyle: .body2,
                    aspectRatio: 1,
                    parentPart: 0.2
                ).onChange(of: selectedDayIndex) { _ in
                    selectedTimeIndex = -1
                }.onChange(of: selectedTimeIndex) { _ in
                    withAnimation {
                        scrollView.scrollTo(selectedDayIndex)
                    }
                }
            }
            Divider().padding(.horizontal)
            BookRow(
                items: availabilities[selectedDayIndex].second as! [Availability],
                id: \.startTime,
                selectedIndex: $selectedTimeIndex,
                itemText: { $0.startTime.toString() },
                textStyle: .body1,
                aspectRatio: 2,
                parentPart: 0.33
            )
        }
        Button(
            item: selectedTimeIndex,
            disabled: { $0 < 0 },
            action: { unwrappedSelectedTimeIndex in
                onBookNow(availabilities[selectedDayIndex].second![unwrappedSelectedTimeIndex] as! Availability)
            }
        ) {
            Text(Feature.Strings.shared.bookNow)
        }.buttonStyle(ActionButtonStyle(style: .onWhite)).padding(.horizontal)
        // to apply padding by verticalArrangement
        Spacer().height(0)
    }
}

private struct BookRow<T, ID: Hashable>: View {
    let items: [T]
    let id: KeyPath<T, ID>
    @Binding
    var selectedIndex: Int

    let itemText: (T) -> String
    let textStyle: TextStyle
    let aspectRatio: Double
    let parentPart: CGFloat
    
    @State
    var height: CGFloat?
    
    var body: some View {
        let width: CGFloat = (UIScreen.main.bounds.width * parentPart).rounded()
        let itemSize = CGSize(width: width, height: (width / aspectRatio).rounded())
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                let shape = RoundedRectangle(cornerRadius: 14)
                ForEachIndexed(items, id: id) { i, item in
                    let selected = i == selectedIndex
                    Button(action: {
                        withAnimation {
                            selectedIndex = i
                        }
                    }) {
                        Text(itemText(item))
                            .frame(size: itemSize)
                            .textStyle(textStyle)
                            .multilineTextAlignment(.center)
                            .foregroundColorKMM(selected ? .companion.White : .companion.DarkGrey)
                    }
                    .background {
                        if selected {
                            shape.foregroundColorKMM(.companion.Green)
                        } else {
                            shape.stroke(lineWidth: 2)
                                .foregroundColorKMM(.companion.LightGray)
                                .padding(1)
                        }
                    }
                }
            }.padding(.horizontal)
        }
    }
}

//    itemText: (T) -> String,
//    textStyle: TextStyle,
//    aspectRatio: Float,
//    fillParentMaxWidthPart: Float,
//) {
//    LazyRow(
//        state = state,
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        contentPadding = PaddingValues(horizontal = 16.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        itemsIndexed(items) { i, item ->
//            val selected = i == selectedIndex
//            val shape = RoundedCornerShape(14.dp)
//            Box(
//                contentAlignment = Alignment.Center,
//                modifier = Modifier
//                    .fillParentMaxWidth(fillParentMaxWidthPart)
//                    .aspectRatio(aspectRatio)
//                    .clip(shape)
//                    .borderKMM(
//                        if (selected) 0.dp else 2.dp,
//                        color = Color.LightGray,
//                        shape = shape,
//                    )
//                    .backgroundKMM(
//                        if (selected) Color.Green else Color.Transparent,
//                    )
//                    .clickable {
//                        Napier.wtf("clickable $i")
//                        setSelectedIndex(i)
//                    }
//            ) {
//                Text(
//                    itemText(item),
//                    color = (if (selected) Color.White else Color.DarkGrey).toColor(),
//                    textAlign = TextAlign.Center,
//                    style = textStyle,
//                )
//            }
//        }
//    }
//}

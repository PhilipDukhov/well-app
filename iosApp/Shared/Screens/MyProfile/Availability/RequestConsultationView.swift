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
                    case is Feature.StateStatusProcessing:
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
                .progressViewStyle(CircularProgressViewStyle(tint: .companion.Green))
        } else if state.availabilitiesByDay.isEmpty {
            Text(Feature.Strings.shared.hasNoConsultations)
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
    let onBookNow: (BookingAvailability) -> Void
    
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
                items: availabilities[selectedDayIndex].second as! [BookingAvailability],
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
                onBookNow(availabilities[selectedDayIndex].second![unwrappedSelectedTimeIndex] as! BookingAvailability)
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
                ForEachIndexed(items) { i, item in
                    let text = itemText(item)
                    SelectableButton(
                        selected: i == selectedIndex,
                        onClick: {
                            withAnimation {
                                selectedIndex = i
                            }
                        },
                        content: Text(text)
                            .frame(size: itemSize)
                            .textStyle(textStyle)
                            .multilineTextAlignment(.center)
                    )
                    .id(text)
                }
            }.padding(.horizontal)
        }
    }
}

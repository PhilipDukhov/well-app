//
//  CalendarScreen.swift
//  Well
//
//  Created by Phil on 01.12.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = CalendarFeature

struct CalendarScreen: View {
    let state: CalendarFeature.State
    let listener: (CalendarFeature.Msg) -> Void

    @State
    private var dialogMeeting: MeetingViewModel?

    var body: some View {
        VStack {
            CalendarMonthView(
                state: state.infoState,
                title: {
                    CalendarTitleView(state: state, showNextMonth: showNextMonth, showPrevMonth: showPrevMonth)
                },
                colors: CalendarMonthViewColors.companion.mainCalendar,
                onSelect: {
                    listener(Feature.MsgCompanion.shared.SelectDate(selectedDate: $0.date))
                },
                showNextMonth: showNextMonth,
                showPrevMonth: showPrevMonth
            )
            MeetingsList(
                meetings: state.selectedItemMeetings.map { [ $0] } ?? state.upcomingMeetings,
                onSelectMeeting: {
                    dialogMeeting = $0
                },
                onStartCall: {
                    listener(Feature.MsgStartCall(meeting: $0))
                },
                onSelectUser: {
                    listener(Feature.MsgOpenUserProfile(meeting: $0))
                }
            )
            Spacer()
        }.padding(.horizontal)
            .fillMaxSize()
            .background(GradientView(gradient: .main).ignoresSafeArea(edges: .top))

    }

    func showNextMonth() {
        listener(Feature.MsgCompanion.shared.NextMonth)
    }

    func showPrevMonth() {
        listener(Feature.MsgCompanion.shared.PrevMonth)
    }
}

private struct CalendarTitleView: View {
    let state: Feature.State

    let showNextMonth: () -> Void
    let showPrevMonth: () -> Void

    @State
    private var currentMonthNameSize: CGSize?
    private let titleId = "titleId"

    var body: some View {
        VStack {
            ZStack {
                GeometryReader { geometry in
                    ScrollViewReader { scrollView in
                        ScrollView(.horizontal) {
                            HStack(spacing: 25) {
                                HStack {
                                    Spacer()
                                    otherMonthTitle(state.prevMonthName, singleLetter: false)
                                }.fillMaxWidth().frame(minWidth: geometry.size.width / 2)
                                currentMonthText
                                    .id(titleId)
                                    .sizeReader {
                                        currentMonthNameSize = $0
                                    }
                                HStack {
                                    otherMonthTitle(state.nextMonthName, singleLetter: false)
                                    Spacer()
                                }.fillMaxWidth().frame(minWidth: geometry.size.width / 2)
                            }
                            .onAppear {
                                scrollView.scrollTo(titleId, anchor: .center)
                            }
                            .onChange(of: state.currentMonthName) { _ in
                                scrollView.scrollTo(titleId, anchor: .center)
                            }
                        }
                    }
                }.frame(height: currentMonthNameSize?.height)
                HStack(spacing: 0) {
                    Button(action: showPrevMonth) {
                        otherMonthTitle(state.prevMonthName, singleLetter: true)
                            .fillMaxWidth()
                    }
                    currentMonthText
                        .frame(size: currentMonthNameSize)
                    Button(action: showNextMonth) {
                        otherMonthTitle(state.nextMonthName, singleLetter: true)
                            .fillMaxWidth()
                    }
                }.background(Color.black.opacity(0.01)).opacity(0.1)
            }
            Spacer().frame(height: 18)
            Text(state.year.toString())
                .textStyle(.body2)
        }.foregroundColor(.white).padding(.vertical, 30)
    }

    var currentMonthText: some View {
        Text(state.currentMonthName)
            .textStyle(.h3)
    }

    func otherMonthTitle(_ title: String, singleLetter: Bool) -> some View {
        Text(
            singleLetter ?
            title.first?.toString() ?? ""
            : title
        )
            .opacity(0.2)
            .textStyle(.h5)
    }
}

private struct MeetingsList: View {
    let meetings: [Feature.StateDayMeetings]
    let onSelectMeeting: (MeetingViewModel) -> Void
    let onStartCall: (MeetingViewModel) -> Void
    let onSelectUser: (MeetingViewModel) -> Void

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 15) {
                ForEach(meetings, id: \.day) { dayMeetings in
                    Text(dayMeetings.day.localizedRelatedDescription())
                        .textStyle(.body1)
                        .foregroundColor(.white)
                    ForEach(dayMeetings.meetings) { meeting in
                        MeetingCard(
                            meeting: meeting,
                            onSelect: {
                                onSelectMeeting(meeting)
                            },
                            onStartCall: {
                                onStartCall(meeting)
                            },
                            onSelectUser: {
                                onSelectUser(meeting)
                            }
                        )
                    }
                }
            }.padding(.vertical, 15).padding(.trailing)
        }
    }
}

private struct MeetingCard: View {
    let meeting: MeetingViewModel

    let onSelect: () -> Void
    let onStartCall: () -> Void
    let onSelectUser: () -> Void

    var body: some View {
        ZStack(alignment: .trailing) {
            HStack {
                VStack(alignment: .leading, spacing: 0) {
                    let ongoing = meeting.status == .ongoing
                    Text(ongoing ? Feature.Strings.shared.now : meeting.startTime.toString())
                        .strikethrough(meeting.status == .past)
                        .foregroundColorKMM(ongoing ? .companion.Green : .companion.Black)
                        .textStyle(.caption)

                    Text(meeting.title)
                        .textStyle(.subtitle2)
                        .foregroundColor(.black)
                        .padding(.top, 3)
                        .padding(.bottom, 9)
                    if let user = meeting.user {
                        Button(action: onSelectUser) {
                            HStack {
                                ProfileImage(user)
                                    .frame(size: 20)
                                Spacer().frame(width: 7)
                                Text("with \(user.fullName)")
                                    .textStyle(.caption)
                                    .foregroundColorKMM(.companion.LightBlue)
                            }
                        }
                    }
                }
                Spacer().fillMaxWidth()
            }
            if meeting.status  == .ongoing {
                Button(action: onStartCall) {
                    Image(systemName: "phone.fill")
                        .font(.system(size: 20))
                        .foregroundColorKMM(.companion.Green)
                        .padding()
                }
            }
        }.padding(15).padding(.leading, 12)
            .background(Shapes.medium.foregroundColor(.white))
            .onTapGesture(perform: onSelect)
    }
}

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
        VStack(spacing: 0) {
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
                onDeleteMeeting: {
                    listener(Feature.MsgUpdateMeetingState(meeting: $0, state: $1))
                },
                onStartCall: {
                    listener(Feature.MsgStartCall(meeting: $0))
                },
                onSelectUser: {
                    listener(Feature.MsgOpenUserProfile(meeting: $0))
                },
				onDeleteRequest: {
					listener(Feature.MsgDeleteRequest(meeting: $0))
				},
				onUpdateState: {
					listener(Feature.MsgUpdateMeetingState(meeting: $0, state: $1))
				}
			)
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
    let onDeleteMeeting: (MeetingViewModel, Meeting.StateCanceled) -> Void
    let onStartCall: (MeetingViewModel) -> Void
    let onSelectUser: (MeetingViewModel) -> Void
	let onDeleteRequest: (MeetingViewModel) -> Void
    let onUpdateState: (MeetingViewModel, Meeting.State) -> Void

    var body: some View {
        ScrollView(showsIndicators: false) {
            LazyVStack(spacing: 15) {
                ForEach(meetings, id: \.day) { dayMeetings in
                    Text(dayMeetings.day.localizedRelatedDescription())
                        .textStyle(.body1)
                        .foregroundColor(.white)
                    ForEach(dayMeetings.meetings) { meeting in
                        if meeting.waitingExpertResolution {
                            RequestedMeetingCard(
                                meeting: meeting,
                                onSelectUser: {
                                    onSelectUser(meeting)
                                },
                                onUpdateState: {
                                    onUpdateState(meeting, $0)
                                }
                            )
                        } else {
                            ConfirmedMeetingCard(
                                meeting: meeting,
                                onSelect: {
                                    onSelectMeeting(meeting)
                                },
                                onStartCall: {
                                    onStartCall(meeting)
                                },
                                onSelectUser: {
                                    onSelectUser(meeting)
                                },
								onDeleteRequest: {
									onDeleteRequest(meeting)
								},
                                onDelete: { state in
                                    onDeleteMeeting(meeting, state)
								}
                            )
                        }
                    }
                }
            }.padding(.vertical, 15)
        }
    }
}

private struct ConfirmedMeetingCard: View {
    let meeting: MeetingViewModel

    let onSelect: () -> Void
    let onStartCall: () -> Void
    let onSelectUser: () -> Void
	let onDeleteRequest: () -> Void
    let onDelete: (Meeting.StateCanceled) -> Void

    @State
    private var deletionReasonDialogShowing = false

    var body: some View {
        let isOngoing = meeting.status == .ongoing
        ZStack(alignment: isOngoing ? .trailing : .topTrailing) {
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
                        .padding(.bottom, meeting.rejectionReason == nil ? 9 : 0)

                    meeting.rejectionReason.map {
                        Text($0)
                            .textStyle(.subtitle2)
                            .foregroundColor(.black)
                            .padding(.top, 3)
                            .padding(.bottom, 9)
                    }

                    Button(action: onSelectUser) {
                        HStack {
                            ProfileImage(meeting.otherUser)
                                .frame(size: 20)
                            Spacer().frame(width: 7)
                            Text("with \(meeting.otherUser.fullName)")
                                .textStyle(.caption)
                                .foregroundColorKMM(.companion.LightBlue)
                        }
                    }
                }
            }.fillMaxWidth(alignment: .leading).padding(15).padding(.leading, 12)
            if isOngoing {
                Button(action: onStartCall) {
                    Image(systemName: "phone.fill")
                        .font(.system(size: 20))
                        .foregroundColorKMM(.companion.Green)
                        .padding()
                }
            } else {
                Button {
                    if meeting.rejectionReason != nil {
                        onDelete(Meeting.StateCanceled(reason: ""))
                    } else {
                        deletionReasonDialogShowing = true
                    }
                } label: {
                    Image(systemName: "trash.fill")
                        .font(.system(size: 20))
                        .foregroundColorKMM(.companion.RadicalRed)
                        .padding()
                }
                .textFieldAlertController(
                    isPresented: $deletionReasonDialogShowing,
                    info: .init(
                        title: Feature.Strings.shared.deletionTitle,
                        placeholder: Feature.Strings.shared.deletionLabel
                    ),
                    alertConfirmed: {
                        onDelete(Meeting.StateCanceled(reason: $0))
                    }
                )
            }
        }
        .background(Shapes.medium.foregroundColor(.white))
        .onTapGesture(perform: onSelect)
    }
}

private struct RequestedMeetingCard: View {
    let meeting: MeetingViewModel

    let onSelectUser: () -> Void
    let onUpdateState: (Meeting.State) -> Void

    @State
    private var rejectionReasonDialogShowing = false

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 10) {
                if meeting.isExpert {
                    HStack {
                        Button(action: onSelectUser) {
                            Text(meeting.otherUser.fullName)
                                .textStyle(.subtitle2)
                                .foregroundColorKMM(.companion.Green)
                        }
                        Text(Feature.Strings.shared.needsYourHelp)
                            .textStyle(.subtitle2)
                    }
                } else {
                    Text(meeting.title)
                        .textStyle(.subtitle2)
                        .foregroundColor(.black)
                }
                Text("\(Feature.Strings.shared.bookingTime): \(meeting.startTime)")
                    .textStyle(.caption)
                HStack {
                    Button(action: {
                        onUpdateState(Meeting.StateConfirmed.shared)
                    }) {
                        Text(Feature.Strings.shared.confirm)
                            .textStyle(.subtitle2)
                            .foregroundColorKMM(.companion.MediumBlue)
                    }
                    Button(action: {
                        rejectionReasonDialogShowing = true
                    }) {
                        Text(Feature.Strings.shared.reject)
                            .textStyle(.subtitle2)
                            .foregroundColorKMM(.companion.Pink)
                    }
                    .textFieldAlertController(
                        isPresented: $rejectionReasonDialogShowing,
                        info: .init(
                            title: Feature.Strings.shared.rejectionTitle,
                            placeholder: Feature.Strings.shared.rejectionLabel
                        ),
                        alertConfirmed: {
                            onUpdateState(Meeting.StateRejected(reason: $0))
                        }
                    )
                }
            }.layoutPriority(1)
            Spacer().fillMaxWidth()
        }.padding(15).padding(.leading, 12)
            .background(Shapes.medium.foregroundColor(.white))
    }
}

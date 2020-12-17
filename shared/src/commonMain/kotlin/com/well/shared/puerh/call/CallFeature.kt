package com.well.shared.puerh.call

//import com.well.serverModels.User
//import com.well.shared.puerh.call.CallFeature.State.Status.*

//object CallFeature {
//    fun callingInitialState(user: User): State = State(user, Calling)
//    fun incomingInitialState(user: User): State = State(user, Incoming)
//
//    fun initialCallingEffects(): Set<Eff> = setOf(Eff.Initiate)
//
//    data class State(
//        val user: User,
//        val status: Status,
//    ) {
//        enum class Status {
//            Calling,
//            Incoming,
//            Connecting,
//            Ongoing,
//        }
//    }
//
//    sealed class Msg {
//        object Accept : Msg()
//        object End : Msg()
//    }
//
//    sealed class Eff {
//        object Initiate : Eff()
//        object Accept : Eff()
//        object End : Eff()
//    }
//
//    fun reducer(msg: Msg, state: State): Pair<State, Set<Eff>> = when (msg) {
//        Msg.Accept -> state.copy(status = Connecting) to setOf(Eff.Accept)
//        Msg.End -> state to setOf(Eff.End)
//    }
//
//}
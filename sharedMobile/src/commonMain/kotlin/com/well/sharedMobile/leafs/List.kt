package com.well.sharedMobile.leafs
//
//import oolong.effect.none
//
//object List {
//
//    data class Model(
//        val count: Int = 0
//    )
//
//    sealed class Msg {
//        object Increment : Msg()
//        object Decrement : Msg()
//    }
//
//    data class Props(
//        val count: Int,
//        val onIncrement: () -> Msg,
//        val onDecrement: () -> Msg,
//        val test: Boolean
//    )
//
//    val init1 = {
//        Model() to none<Msg>()
//    }
//
//    val update = { msg: Msg, model: Model ->
//        when (msg) {
//            Msg.Increment -> model.copy(count = model.count + 1)
//            Msg.Decrement -> model.copy(count = model.count - 1)
//        } to none<Msg>()
//    }
//
//    val view = { model: Model ->
//        Props(
//            model.count,
//            { Msg.Increment },
//            { Msg.Decrement },
//            false
//        )
//    }
//
//}
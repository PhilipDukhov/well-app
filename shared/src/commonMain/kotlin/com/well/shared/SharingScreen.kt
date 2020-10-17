package com.well.shared


//import oolong.effect.none
//
//
//object SharingScreen {
//    data class Model(
//        val imageURL: String? = null
//    )
//
//    sealed class Msg {
//        class UploadData(val data: ByteArray) : Msg()
//    }
//
//    data class Props(
//        val count: Int,
//        val onIncrement: () -> Msg,
//        val onDecrement: () -> Msg,
//        val test: Boolean
//    )
//
//    val init = {
//        Model() to none<Msg>()
//    }
//
//    val update = { msg: Msg, model: Model ->
//        when (msg) {
//            is Msg.UploadData -> model.copy(count = model.count + 1)
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
//    val render = { props: Props, dispatch: Dispatch<Msg> ->
//
//    }
//
//}
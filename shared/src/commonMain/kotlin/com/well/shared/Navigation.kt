package com.well.shared

import oolong.Dispatch
import oolong.next.bimap
import oolong.Effect
import oolong.dispatch.contramap

object Navigation {

    sealed class Model {
        // Delegates
        data class List(val model: com.well.shared.List.Model) : Model()
        data class Detail(val model: com.well.shared.Detail.Model) : Model()
    }

    sealed class Msg {
        // Delegates
        data class List(val msg: com.well.shared.List.Msg) : Msg()
        data class Detail(val msg: com.well.shared.Detail.Msg) : Msg()
        // Navigation
        data class SetScreen(val next: Pair<Model, Effect<Msg>>): Msg()
    }

    sealed class Props {
        // Delegates
        data class List(val props: com.well.shared.List.Props) : Props()
        data class Detail(val props: com.well.shared.Detail.Props) : Props()
    }

    val init: () -> Pair<Model, Effect<Msg>> = {
        bimap(List.init(), Model::List, Msg::List)
    }

    val update: (Msg, Model) -> Pair<Model, Effect<Msg>> = { msg, model ->
        when (msg) {
            is Msg.List -> {
                bimap(
                    List.update(msg.msg, (model as Model.List).model),
                    Model::List,
                    Msg::List
                )
            }
            is Msg.Detail -> {
                bimap(
                    Detail.update(msg.msg, (model as Model.Detail).model),
                    Model::Detail,
                    Msg::Detail
                )
            }
            is Msg.SetScreen -> {
                msg.next
            }
        }
    }

    val view: (Model) -> Props = { model ->
        when (model) {
            is Model.List -> {
                Props.List(List.view(model.model))
            }
            is Model.Detail -> {
                Props.Detail(Detail.view(model.model))
            }
        }
    }

//    val render: (Props, Dispatch<Msg>) -> Any? = { props, dispatch ->
//        when (props) {
//            is Props.List -> {
//                List.render(props.props, contramap(dispatch, Msg::List))
//            }
//            is Props.Detail -> {
//                Detail.render(props.props, contramap(dispatch, Msg::Detail))
//            }
//        }
//    }
}
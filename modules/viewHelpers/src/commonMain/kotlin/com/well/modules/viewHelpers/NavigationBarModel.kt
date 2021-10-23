package com.well.modules.viewHelpers

data class NavigationBarModel<Msg>(
    val title: String,
    val leftItem: Item<Msg>? = null,
    val rightItem: Item<Msg>? = null,
) {
    data class Item<Msg>(
        val content: Content,
        val enabled: Boolean = true,
        val msg: Msg?,
    ) {
        companion object {
            fun <Msg>activityIndicator() = Item<Msg>(content = Content.ActivityIndicator, msg = null)
        }

        constructor(
            text: String,
            enabled: Boolean = true,
            msg: Msg,
        ) : this(Content.Text(text), enabled, msg)

        constructor(
            icon: Content.Icon.Icon,
            enabled: Boolean = true,
            msg: Msg,
        ) : this(Content.Icon(icon), enabled, msg)

        sealed class Content {
            data class Text(val text: String) : Content()
            object ActivityIndicator : Content()
            data class Icon(val icon: Icon) : Content() {
                enum class Icon {
                    Back,
                }
            }
        }
    }
}
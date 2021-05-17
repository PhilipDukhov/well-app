package com.well.sharedMobile.puerh.myProfile

data class UIPreviewField(
    val title: String = "",
    val content: Content,
) {
    constructor(
        title: String,
        text: String,
    ) : this(title, Content.Text(text))

    constructor(
        title: String,
        text: String,
        icon: Icon,
        isLink: Boolean = false,
    ) : this(title, Content.TextAndIcon(text, icon, isLink))

    constructor(
        title: String,
        set: List<String>,
    ) : this(title, Content.List(set))

    sealed class Content {
        data class Text(val text: String) : Content()
        data class TextAndIcon(
            val text: String,
            val icon: Icon,
            val isLink: Boolean,
        ) : Content()

        data class List(val list: kotlin.collections.List<String>) : Content()
        data class Button<Msg>(val title: String, val msg: Msg) : Content()
    }

    enum class Icon {
        Location,
        Publications,
        Twitter,
        Doximity,
    }
}
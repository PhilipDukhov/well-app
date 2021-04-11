package com.well.sharedMobile.puerh.myProfile

import com.well.modules.models.enumValueOfSpacedUppercase
import com.well.modules.models.spacedUppercaseEnumValues
import com.well.modules.models.spacedUppercaseName
import com.well.modules.utils.base.UrlUtil

data class UIEditingField<C> internal constructor(
    val placeholder: String,
    val content: C,
    val updateMsg: (C) -> MyProfileFeature.Msg,
) where C : UIEditingField.Content {
    companion object {
        internal inline fun <reified T : Enum<T>> createSingleSelectionList(
            placeholder: String,
            singleSelection: T?,
            crossinline updateValue: (T) -> MyProfileFeature.Msg,
        ) = UIEditingField(
            placeholder,
            Content.List.createSingleSelection(
                singleSelection = singleSelection,
            ),
        ) {
            updateValue(enumValueOfSpacedUppercase(it.selection.first()))
        }

        internal inline fun <reified T : Enum<T>> createMultipleSelectionList(
            placeholder: String,
            selection: Set<T>,
            crossinline updateValue: (List<T>) -> MyProfileFeature.Msg,
        ) = UIEditingField(
            placeholder,
            Content.List.createMultipleSelection(
                selection
            ),
        ) {
            updateValue(it.selection.map(::enumValueOfSpacedUppercase))
        }
    }

    sealed class Content(open val icon: Icon? = null) {
        open fun valid(): Boolean = true

        companion object {
            internal fun Text(
                textNullable: String?,
                icon: Icon? = null,
            ) = Text(text = textNullable ?: "", nullable = true, icon = icon)

            internal fun Text(
                textNonNullable: String,
            ) = Text(text = textNonNullable, nullable = false)

            internal fun Email(emailNullable: String?) = Content.Email(emailNullable ?: "")
        }

        data class Text internal constructor(
            val text: String,
            val nullable: Boolean = true,
            override val icon: Icon? = null
        ) : Content(icon) {
            override fun toString(): String = text

            override fun valid(): Boolean =
                if (nullable) true else text.isNotBlank()

            @Suppress("unused")
            fun doCopy(text: String) = copy(text = text)
        }

        data class Email internal constructor(val email: String) : Content() {
            override fun toString(): String = email

            override fun valid(): Boolean =
                Regex(
                    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
                ).matches(email)
        }

        data class List internal constructor(
            val selection: Set<String>,
            val list: kotlin.collections.List<String>,
            val multipleSelectionAvailable: Boolean,
        ) : Content() {
            constructor(
                singleSelection: String?,
                list: kotlin.collections.List<String>,
            ) : this(
                selection = if (singleSelection != null) setOf(singleSelection) else setOf(),
                list = list,
                multipleSelectionAvailable = false
            )

            override fun toString(): String =
                selection.sorted().joinToString(", ")

            companion object {
                internal inline fun <reified T : Enum<T>> createSingleSelection(
                    singleSelection: T?,
                ) = List(
                    singleSelection = singleSelection?.spacedUppercaseName(),
                    list = spacedUppercaseEnumValues<T>().sorted(),
                )

                internal inline fun <reified T : Enum<T>> createMultipleSelection(
                    selection: Set<T>,
                ) = List(
                    selection = selection.mapTo(HashSet()) { it.spacedUppercaseName() },
                    list = spacedUppercaseEnumValues<T>().sorted(),
                    multipleSelectionAvailable = true
                )
            }

            @Suppress("unused")
            fun doCopy(selection: Set<String>) = copy(selection = selection)
        }

        enum class Icon {
            Location,
        }
    }
}
package com.well.sharedMobile.puerh.πModels

import com.well.sharedMobile.utils.countryCodes.countryCodesList
import com.well.sharedMobile.utils.countryCodes.emojiFlagForCountryCode
import com.well.sharedMobile.utils.countryCodes.nameForCountryCode
import com.well.modules.models.spacedUppercaseName

data class UIEditingField<Content, Msg> internal constructor(
    val placeholder: String,
    val content: Content,
    val updateMsg: (Content) -> Msg,
) where Content : UIEditingField.Content {
    companion object {
        internal inline fun <reified T : Enum<T>, Msg> createSingleSelectionList(
            placeholder: String,
            singleSelection: T?,
            crossinline updateValue: (T?) -> Msg,
        ) = UIEditingField(
            placeholder,
            Content.List.createSingleSelection(
                singleSelection = singleSelection,
            ),
        ) {
            updateValue(it.selectedItems.firstOrNull())
        }

        internal inline fun <reified T : Enum<T>, Msg> createMultipleSelectionList(
            placeholder: String,
            selection: Set<T>,
            crossinline updateValue: (Set<T>) -> Msg,
        ) = UIEditingField(
            placeholder,
            Content.List.createMultipleSelection(
                selection
            ),
        ) {
            updateValue(it.selectedItems)
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

        @Suppress("DataClassPrivateConstructor")
        data class List<Item> private constructor(
            internal val selectedItems: Set<Item>,
            private val items: Set<Item>,
            private val descriptor: (Item) -> String,
            private val sortBy: (Item, String) -> String = { _, description -> description },
            val multipleSelectionAvailable: Boolean,
        ) : Content() {
            val itemDescriptions: kotlin.collections.List<String>
            val selectionIndices: Set<Int>
            internal val itemsList: kotlin.collections.List<Item>

            init {
                val sortedItemsAndDescriptions = items
                    .map { it to descriptor(it) }
                    .sortedBy { sortBy(it.first, it.second) }
                itemsList = sortedItemsAndDescriptions.map { it.first }
                itemDescriptions = sortedItemsAndDescriptions.map { it.second }
                selectionIndices = selectedItems.map { itemsList.indexOf(it) }.toSet()
            }

            internal constructor(
                singleSelection: Item?,
                items: Set<Item>,
                descriptor: (Item) -> String,
                sortBy: (Item, String) -> String = { _, description -> description },
            ) : this(
                selectedItems = setOfNotNull(singleSelection),
                items = items,
                descriptor = descriptor,
                sortBy = sortBy,
                multipleSelectionAvailable = false,
            )

            override fun toString(): String =
                items.filterIndexed { index, _ -> selectionIndices.contains(index) }
                    .joinToString(", ") { descriptor(it) }

            companion object {
                internal fun countryCodesList(selectedCountryCode: String?) =
                    List(
                        singleSelection = selectedCountryCode,
                        items = countryCodesList(),
                        descriptor = { "${emojiFlagForCountryCode(it)} ${nameForCountryCode(it)}".trim() },
                        sortBy = { code, _ -> nameForCountryCode(code) },
                    )

                internal fun createSingleStingSelection(
                    singleSelection: String?,
                    items: Set<String>,
                ) = List(
                    singleSelection = singleSelection,
                    items = items,
                    descriptor = { it },
                )

                internal inline fun <reified T : Enum<T>> createSingleSelection(
                    singleSelection: T?,
                ) = List(
                    singleSelection = singleSelection,
                    items = enumValues<T>().toSet(),
                    descriptor = { enumValue: T -> enumValue.spacedUppercaseName() },
                )

                internal inline fun <reified T : Enum<T>> createMultipleSelection(
                    selection: Set<T>,
                ) = List(
                    selectedItems = selection,
                    items = enumValues<T>().toSet(),
                    descriptor = { enumValue: T -> enumValue.spacedUppercaseName() },
                    multipleSelectionAvailable = true,
                )
            }

            @Suppress("unused")
            fun doCopy(selectionIndices: Set<Int>) = copy(selectedItems = itemsList.slice(selectionIndices).toSet())
        }

        enum class Icon {
            Location,
        }
    }
}
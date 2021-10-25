package com.well.modules.utils.viewUtils

import com.well.modules.utils.kotlinUtils.spacedUppercaseName
import com.well.modules.utils.viewUtils.countryCodes.countryCodesList
import com.well.modules.utils.viewUtils.countryCodes.emojiFlagForCountryCode
import com.well.modules.utils.viewUtils.countryCodes.nameForCountryCode

data class UIEditingField<Content, Msg>  constructor(
    val placeholder: String,
    val content: Content,
    val updateMsg: (Content) -> Msg,
) where Content : UIEditingField.Content {
    companion object {
        inline fun <reified T : Enum<T>, Msg> createSingleSelectionList(
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

         inline fun <reified T : Enum<T>, Msg> createMultipleSelectionList(
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

    sealed class Content {
        open fun valid(): Boolean = true

        companion object {
            fun textNullable(
                textNullable: String?,
            ) = Text(text = textNullable ?: "", nullable = true)

            fun textNonNullable(
                textNonNullable: String,
            ) = Text(text = textNonNullable, nullable = false)

            fun Email(emailNullable: String?) = Content.Email(emailNullable ?: "")
        }

        data class Text constructor(
            val text: String,
            val nullable: Boolean = true,
        ) : Content() {
            override fun toString(): String = text

            override fun valid(): Boolean =
                if (nullable) true else text.isNotBlank()

            @Suppress("unused")
            fun doCopy(text: String) = copy(text = text)
        }

        data class Email constructor(val email: String) : Content() {
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
        data class List<Item>  constructor(
             val selectedItems: Set<Item>,
            private val items: Set<Item>,
            private val descriptor: (Item) -> String,
            private val sortBy: (Item, String) -> String = { _, description -> description },
            val multipleSelectionAvailable: Boolean,
        ) : Content() {
            val itemDescriptions: kotlin.collections.List<String>
            val selectionIndices: Set<Int>
            val itemsList: kotlin.collections.List<Item>

            fun doCopy(selectionIndices: Set<Int>) = copy(selectedItems = itemsList.slice(selectionIndices).toSet())

            init {
                val sortedItemsAndDescriptions = items
                    .map { it to descriptor(it) }
                    .sortedBy { sortBy(it.first, it.second) }
                itemsList = sortedItemsAndDescriptions.map { it.first }
                itemDescriptions = sortedItemsAndDescriptions.map { it.second }
                selectionIndices = selectedItems.map { itemsList.indexOf(it) }.toSet()
            }

            constructor(
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
                fun countryCodesList(selectedCountryCode: String?) =
                    List(
                        singleSelection = selectedCountryCode,
                        items = countryCodesList(),
                        descriptor = { "${emojiFlagForCountryCode(it)} ${nameForCountryCode(it)}".trim() },
                        sortBy = { code, _ -> nameForCountryCode(code) },
                    )

                fun createSingleStingSelection(
                    singleSelection: String?,
                    items: Set<String>,
                ) = List(
                    singleSelection = singleSelection,
                    items = items,
                    descriptor = { it },
                )

                inline fun <reified T : Enum<T>> createSingleSelection(
                    singleSelection: T?,
                ) = List(
                    singleSelection = singleSelection,
                    items = enumValues<T>().toSet(),
                    descriptor = { enumValue: T -> enumValue.spacedUppercaseName() },
                )

                 inline fun <reified T : Enum<T>> createMultipleSelection(
                    selection: Set<T>,
                ) = List(
                    selectedItems = selection,
                    items = enumValues<T>().toSet(),
                    descriptor = { enumValue: T -> enumValue.spacedUppercaseName() },
                    multipleSelectionAvailable = true,
                )
            }
        }
    }
}
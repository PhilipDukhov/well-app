package com.well.androidApp.ui.customViews

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class BoldUpperCaseTextView(context: Context, attrs: AttributeSet?) :
    AppCompatTextView(context, attrs) {
    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text == null) {
            super.setText(null, type)
            return
        }
        val string = SpannableString(text)
        text
            .withIndex()
            .filter { it.value.isUpperCase() }
            .forEach {
                string.setSpan(
                    StyleSpan(Typeface.BOLD),
                    it.index,
                    it.index + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        super.setText(string, type)
    }
}

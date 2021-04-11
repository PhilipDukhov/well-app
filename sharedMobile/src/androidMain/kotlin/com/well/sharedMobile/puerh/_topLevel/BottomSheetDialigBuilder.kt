package com.well.sharedMobile.puerh._topLevel

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import com.well.modules.utils.ui.toDp

data class BottomSheetDialogBuilder(val context: Context) {
    private val linearLayout = LinearLayoutCompat(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = 10.toDp(context)
        }
    }
    private val bottomSheetDialog = BottomSheetDialog(context).apply {
        setContentView(linearLayout)
    }

    fun add(action: Action) {
        val textView = MaterialTextView(context)
        textView.text = action.title
        textView.minHeight = 50.toDp(context)
        textView.gravity = Gravity.CENTER_VERTICAL
//        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(operation.drawableId, 0, 0, 0)
        textView.setOnClickListener {
            dismiss()
            action.block.invoke()
        }
        linearLayout.addView(textView)
    }

    fun show() = bottomSheetDialog.show()

    fun dismiss() = bottomSheetDialog.dismiss()
}
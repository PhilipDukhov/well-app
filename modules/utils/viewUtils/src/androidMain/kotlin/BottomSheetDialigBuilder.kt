package com.well.modules.utils.viewUtils

import com.well.modules.utils.viewUtils.ui.toDp
import android.content.Context
import android.view.Gravity
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView

data class BottomSheetDialogBuilder(val context: Context) {
    private val linearLayout = LinearLayoutCompat(context).apply {
        orientation = LinearLayoutCompat.VERTICAL
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
        textView.setOnClickListener {
            dismiss()
            action.action.invoke()
        }
        linearLayout.addView(textView)
    }

    fun show() = bottomSheetDialog.show()

    fun dismiss() = bottomSheetDialog.dismiss()
}
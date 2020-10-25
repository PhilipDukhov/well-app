package com.well.androidApp.ui.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.well.serverModels.Path

class DrawingView(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {
    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    var paths: List<Path>? = null
        set(value) {
            field = value
            val diff = pathViews.count() - (value?.count() ?: 0)
            when {
                diff > 0 -> {
                    pathViews.subList(
                        pathViews.count() - diff,
                        pathViews.count()
                    ).forEach(this::removeView)
                    pathViews = pathViews.dropLast(diff)
                }
                diff < 0 -> pathViews = pathViews + List(-diff) {
                    val pathView = PathView(context)
                    pathView.layoutParams = LayoutParams(
                        MATCH_PARENT,
                        MATCH_PARENT,
                    )
                    addView(pathView)
                    requestLayout()
                    pathView
                }
            }
            if (value == null) return
            pathViews.zip(value).forEach {
                it.first.path = it.second
            }
        }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        (0 until childCount).forEach {
            getChildAt(it).apply {
                layout(t, l, r, b)
            }
        }
    }

    private var pathViews = listOf<PathView>()
}

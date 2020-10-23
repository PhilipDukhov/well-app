package com.well.androidApp.ui.customViews

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.well.androidApp.ui.MainActivity

open class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {
    val activity: MainActivity?
        get() = super.getActivity() as? MainActivity
}

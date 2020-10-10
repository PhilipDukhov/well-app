package com.well.androidApp.ui.customViews

import androidx.fragment.app.Fragment
import com.well.androidApp.ui.MainActivity

open class BaseFragment: Fragment() {
    val activity: MainActivity?
        get() = super.getActivity() as? MainActivity
}
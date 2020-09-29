package com.well.shared

import com.github.aakira.napier.Napier

class Greeting {
    fun greeting(): String {
        Napier.e("Hello napier shared")
        return "Hello, ${Platform().platform}!"
    }
}

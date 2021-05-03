package com.well.sharedMobile.puerh.login

enum class SocialNetwork {
    Apple,
    Facebook,
    Google,
    Twitter,
    ;

    companion object {
        val allCases = values().toList()
    }
}

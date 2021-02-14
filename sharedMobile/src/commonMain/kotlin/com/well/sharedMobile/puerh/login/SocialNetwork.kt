package com.well.sharedMobile.puerh.login

enum class SocialNetwork {
    Facebook,
    Google,
//    Apple,
//    Twitter,
    ;

    companion object {
        val allCases = values().toList()
    }
}

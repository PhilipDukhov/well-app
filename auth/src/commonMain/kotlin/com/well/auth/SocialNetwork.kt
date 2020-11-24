package com.well.auth

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

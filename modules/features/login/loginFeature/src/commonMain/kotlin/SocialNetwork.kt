package com.well.modules.features.login.loginFeature

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
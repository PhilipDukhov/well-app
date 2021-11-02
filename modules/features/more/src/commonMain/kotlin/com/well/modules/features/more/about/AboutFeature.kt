package com.well.modules.features.more.about

import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.viewUtils.sharedImage.SharedImage
import com.well.modules.utils.viewUtils.sharedImage.UrlImage

object AboutFeature {
    data class State(val termsOpened: Boolean = false) {
        val teamMembers = listOf(
            TeamMember(name = "Ralph Clayman", position = "MD, Co-Founder", twitter = "RalphVClayman"),
            TeamMember(name = "Zham Okhunov", position = "MD, Co-Founder", twitter = "zhamokhunov"),
            TeamMember(name = "Jaime Landman", position = "MD, Board Member", twitter = "jaimelandmanuci"),
        )
        val text = """
            Mission statement
            The WELL app provides urologists globally with an inexpensive and easily accessible means of performing mentored urological procedures in any environment. It is our intention that this app serves as a resource that helps level the playing field globally for all minimally-invasive procedures. 
        """.trimIndent()
        val terms = """
            App Privacy Policy

            WELL app respects the privacy of our users. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you visit our mobile application. Please read this Privacy Policy carefully. IF YOU DO NOT AGREE WITH THE TERMS OF THIS PRIVACY POLICY, PLEASE DO NOT ACCESS THE APPLICATION.

            We reserve the right to make changes to this Privacy Policy at any time and for any reason. We will alert you about any changes by updating the “Last updated” date of this Privacy Policy. You are encouraged to periodically review this Privacy Policy to stay informed of updates. 

            Personal Data

            Demographic and other personally identifiable information 
        """.trimIndent()

        data class TeamMember(
            val name: String,
            val position: String,
            val image: SharedImage,
            internal val twitter: String
        ) {
            constructor(
                name: String,
                position: String,
                twitter: String
            ) : this(
                name,
                position,
                UrlImage("https://well-images.s3.us-east-2.amazonaws.com/appImages/${name.filter { !it.isWhitespace() }}.png"),
                twitter
            )
        }

        companion object {
            const val title = "About"
        }
    }

    sealed class Msg {
        data class OpenTwitter(val teamMember: State.TeamMember) : Msg()
        object OpenPrivacyPolicy : Msg()
//        object OpenSponsors : Msg()
        object Back : Msg()
    }

    sealed interface Eff {
        data class OpenLink(val link: String) : Eff
//        data class Push(val screen: MoreScreenState) : Eff
        object Back : Eff
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                Msg.Back -> {
                    if (state.termsOpened) {
                        return@state state.copy(termsOpened = false)
                    } else {
                        return@eff Eff.Back
                    }
                }
                is Msg.OpenTwitter -> {
                    return@eff Eff.OpenLink("https://twitter.com/${msg.teamMember.twitter}")
                }
//                Msg.OpenSponsors -> {
//                    return@eff Eff.Push(MoreScreenState.Sp)
//                }
                Msg.OpenPrivacyPolicy -> {
                    return@state state.copy(termsOpened = true)
                }
            }
        })
    }.withEmptySet()
}
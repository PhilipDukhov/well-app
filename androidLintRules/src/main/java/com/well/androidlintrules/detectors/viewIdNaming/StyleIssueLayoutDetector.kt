package com.well.androidlintrules.detectors.viewIdNaming

import com.well.androidlintrules.Issues
import com.well.androidlintrules.Issues.ViewIdNamingStyle
import com.well.androidlintrules.helping.BaseLayoutDetector
import com.well.androidlintrules.helping.isLowerCamelCase
import com.well.androidlintrules.helping.toLowerCamelCase
import org.w3c.dom.Attr

class StyleIssueLayoutDetector : BaseLayoutDetector() {
    override fun getApplicableAttributes(): Collection<String>? = listOf("id")

    override val issue: Issues = ViewIdNamingStyle

    override fun interestingPart(attribute: Attr) =
        attribute.value.takeLastWhile { it != '/' }

    override fun hasIssue(interestingPart: String) =
        !interestingPart.isLowerCamelCase()

    override fun fixIssue(interestingPart: String) =
        interestingPart.toLowerCamelCase()
}

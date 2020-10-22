package com.well.androidlintrules.helping

import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.XmlContext
import com.well.androidlintrules.Issues
import org.w3c.dom.Attr

abstract class BaseLayoutDetector : LayoutDetector() {
    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        if (hasIssue(interestingPart(attribute))) {
            context.report(
                issue = issue.issueContainer.issue,
                location = context.getLocation(attribute),
                message = issue.issueContainer.explanation,
                quickfixData = buildFix(attribute)
            )
        }
    }

    private fun buildFix(attribute: Attr): LintFix {
        val interestingPart = interestingPart(attribute)
        val fixed = fixIssue(interestingPart)
        return fix()
            .name(issue.issueContainer.fixDisplayName)
            .replace()
            .text(interestingPart)
            .with(fixed)
            .build()
    }

    abstract fun interestingPart(attribute: Attr): String
    abstract fun hasIssue(interestingPart: String): Boolean
    abstract fun fixIssue(interestingPart: String): String
    abstract val issue: Issues
}
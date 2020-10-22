package com.well.androidlintrules

import com.well.androidlintrules.helping.IssueContainer
import com.well.androidlintrules.detectors.viewIdNaming.StyleIssueLayoutDetector

enum class Issues(val issueContainer: IssueContainer) {
    ViewIdNamingStyle(
        IssueContainer(
            id = "ViewIdNamingStyle",
            briefDescription = "View ids should be in camel case",
            explanation = """
                |View ids should be in camel case
            """.trimMargin(),
            fixDisplayName = "Convert to camel case",
            detectorClass = StyleIssueLayoutDetector::class.java
        )
    ),
}

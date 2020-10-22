package com.well.androidlintrules.helping

import com.android.tools.lint.detector.api.*

data class IssueContainer(
    val id: String,
    val briefDescription: String,
    val explanation: String,
    val fixDisplayName: String,
    val detectorClass: Class<out Detector?>
) {
    val issue: Issue = Issue.create(
        id = id,
        briefDescription = briefDescription,
        explanation = explanation,
        category = Category.CORRECTNESS,
        priority = 5,
        severity = Severity.WARNING,
        implementation = Implementation(
            detectorClass,
            Scope.RESOURCE_FILE_SCOPE
        )
    )
}

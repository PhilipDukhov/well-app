package com.well.androidlintrules.helping

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue
import com.well.androidlintrules.Issues

@Suppress("unused")
class LintRegistry : IssueRegistry() {
    override val issues: List<Issue> = Issues.values().map { it.issueContainer.issue }
    override val api: Int = com.android.tools.lint.detector.api.CURRENT_API
}

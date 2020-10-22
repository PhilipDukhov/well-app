package com.well.androidlintrules.rules

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintResult
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.well.androidlintrules.Issues

fun String.lintAsLayoutFile(vararg issues: Issues): TestLintResult =
    TestLintTask
        .lint()
        .files(LintDetectorTest.xml("res/layout/dummy_layout.xml", this))
        .allowDuplicates()
        .issues(*issues.map { it.issueContainer.issue }.toTypedArray())
        .allowMissingSdk()
        .run()

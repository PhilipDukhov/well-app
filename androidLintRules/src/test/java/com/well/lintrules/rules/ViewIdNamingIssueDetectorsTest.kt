package com.well.androidlintrules.rules

import com.well.androidlintrules.Issues.ViewIdNamingStyle
import io.kotest.core.spec.style.FreeSpec
import org.intellij.lang.annotations.Language

@Suppress("unused")
class ViewIdNamingIssueDetectorsTest : FreeSpec({
       "should suggest style" {
        @Language("XML")
        val fileContent = """
        <LinearLayout
            xmlns:android="http://schemas.android.com/apk/res/android"
            android:id="@+id/layout_test" >
        </LinearLayout>        
        """.trimIndent()

        fileContent.lintAsLayoutFile(ViewIdNamingStyle)
            .expectFixDiffs(
                """
                Fix for res/layout/dummy_layout.xml line 3: Convert to camel case:
                @@ -3 +3
                -     android:id="@+id/layout_test" >
                +     android:id="@+id/layoutTest" >
            """.trimIndent()
            )
    }
})

package com.well.modules.androidUi.composableScreens.userChat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Filled.DoubleCheck: ImageVector
    get() {
        if (_doubleCheck != null) {
            return _doubleCheck!!
        }
        _doubleCheck = materialIcon(name = "Filled.Check") {
            materialPath {
                moveTo(6.0f, 16.17f)
                lineTo(1.83f, 12.0f)
                lineToRelative(-1.42f, 1.41f)
                lineTo(6.0f, 19.0f)
                lineTo(18.0f, 7.0f)
                lineToRelative(-1.41f, -1.41f)
                close()
            }
            materialPath {
                moveTo(10.59f, 17.59f)
                lineToRelative(1.41f, 1.41f)
                lineTo(24.0f, 7.0f)
                lineToRelative(-1.41f, -1.41f)
                close()
            }
        }
        return _doubleCheck!!
    }

@Suppress("ObjectPropertyName")
private var _doubleCheck: ImageVector? = null
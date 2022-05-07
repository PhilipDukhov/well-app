package com.well.modules.atomic

import platform.darwin.NSObject

open class NSObjectCloseableContainer : NSObject(), CloseableContainer {
    private val closeableContainer = CloseableContainerImpl()

    override fun addCloseableChild(closeable: Closeable) =
        closeableContainer.addCloseableChild(closeable)

    override fun close() {
        closeableContainer.close()
    }
}
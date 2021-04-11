package com.well.modules.napier

import platform.Foundation.*

actual class ConsoleAntilog actual constructor(
    private val defaultTag: String,
) : Antilog() {
    companion object {
        private const val CALL_STACK_INDEX = 8
    }

    var crashAssert = false

    private val dateFormatter = NSDateFormatter().apply {
        dateFormat = "MM-dd HH:mm:ss.SSS"
    }

    private val tagMap: HashMap<Napier.Level, String> = hashMapOf(
        Napier.Level.VERBOSE to "VERBOSE",
        Napier.Level.DEBUG to "DEBUG",
        Napier.Level.INFO to "INFO",
        Napier.Level.WARNING to "WARN",
        Napier.Level.ERROR to "ERROR",
        Napier.Level.ASSERT to "ASSERT",
    )

    override fun performLog(
        priority: Napier.Level,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        if (priority == Napier.Level.ASSERT) {
            assert(crashAssert) { buildLog(priority, tag, message) }
        } else {
            println(buildLog(priority, tag, message))
        }
    }

    fun setTag(
        level: Napier.Level,
        tag: String
    ) {
        tagMap[level] = tag
    }

    fun setDateFormatterString(formatter: String) {
        dateFormatter.dateFormat = formatter
    }

    private fun getCurrentTime() = dateFormatter.stringFromDate(NSDate())

    private fun buildLog(
        priority: Napier.Level,
        tag: String?,
        message: String?
    ): String {
        return "${getCurrentTime()} ${tagMap[priority]} ${tag ?: performTag(defaultTag)} - $message"
    }

    // find stack trace
    private fun performTag(tag: String): String {
        val thread = NSThread.callStackSymbols

        return if (thread.size >= CALL_STACK_INDEX) {
            createStackElementTag(thread[CALL_STACK_INDEX] as String)
        } else {
            tag
        }
    }

    internal fun createStackElementTag(string: String): String {
        var tag = string
        tag = tag.substringBeforeLast('$')
        tag = tag.substringBeforeLast('(')
        tag = tag.substring(tag.lastIndexOf(".", tag.lastIndexOf(".") - 1) + 1)
        tag = tag.replace("$", "")
        tag = tag.replace("COROUTINE", "")
        return tag
    }
}
package io.genstrings.common

import io.genstrings.model.FormatArg
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private const val encodedRawText = """\?\@\'\"\n\t"""

private val decodedRawText = buildString {
    append('?')
    append('@')
    append('\'')
    append('"')
    appendLine()
    append('\t')
}

private const val encodedFormatArgs = "%1\$s times %2\$f is (not really) %99\$f"

private val decodedFormatArgs = "{1} times {2} is (not really) {99}" to listOf(
    FormatArg(1, "s"),
    FormatArg(2, "f"),
    FormatArg(99, "f"),
)

class ProcessingTest {

    @Test
    fun `decode android escape sequences`() {
        val output = encodedRawText.decodeRawAndroidString()
        assertEquals(decodedRawText, output)
    }

    @Test
    fun `decode android format args`() {
        val output = encodedFormatArgs.decodeAndroidFormatArgs()
        assertEquals(decodedFormatArgs, output)
    }
}
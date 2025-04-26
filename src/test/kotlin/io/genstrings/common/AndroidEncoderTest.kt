package io.genstrings.common

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private val decoded = buildString {
    append('?')
    append('@')
    append('\'')
    append('"')
    appendLine()
    append('\t')
}

private const val encoded = """\?\@\'\"\n\t"""

class AndroidEncoderTest {

    @Test
    fun `decode xml with escape sequences`() {
        val output = AndroidEncoder.decode(encoded)
        assertEquals(decoded, output)
    }
}
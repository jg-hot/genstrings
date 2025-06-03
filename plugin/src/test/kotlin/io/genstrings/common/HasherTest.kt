package io.genstrings.common

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private const val randomString = "xei1Shah6iech4ahtuquuushe5tungohthoNePhaemohXee8Eish5YeiX5piedeigiegh6eisae6shahjiesieQu7Mo8zeem7Ahp0ool3chaiTh7ohShovie7xeig1ee"

private const val sha256Sum = "1cfe094334f66e9161c59b299e9ad0b23c4d9ec0c6e24cb27147b31ca5394634"

// currently you will need to run :plugin:test --rerun and check the HTML report manually
class HasherTest {

    @Test
    fun `sha256 hash random string`() {
        println("running")
        val hash = Hasher.sha256Sum(randomString)
        assertEquals(sha256Sum, hash)
    }
}

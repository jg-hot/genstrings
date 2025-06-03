package io.genstrings.common

import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
object Hasher {

    fun sha256Sum(value: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .toHexString()
    }
}

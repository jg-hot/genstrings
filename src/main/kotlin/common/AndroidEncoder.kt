package io.genstrings.common

// https://developer.android.com/guide/topics/resources/string-resource#escaping_quotes
//
// CURRENTLY NOT SUPPORTED:
// * U+XXXX Unicode characters e.g. \uXXXX
// * Single quotes enclosed in double quotes ("This'll work", for example)
// * Whitespace preservation: e.g. <string>" &#32; &#8200; &#8195;"</string> or <string> \u0032 \u8200 \u8195</string>
object AndroidEncoder {
    fun decode(text: String) = text
        .replace("\\@", "@")
        .replace("\\?", "?")
        .replace("\\n", "\n")
        .replace("\\t", "\t")
        .replace("\\'", "'")
        .replace("\\\"", "\"")
}

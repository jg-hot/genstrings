package io.genstrings

import io.genstrings.command.templateImpl
import kotlin.io.path.Path

fun main(args: Array<String>) {
    val command = args.getOrNull(0)
    when (command) {
        "template" -> template(args.drop(1))
        else -> printUsage()
    }
}

private fun template(args: List<String>) {
    val sourcePath = args.getOrNull(0)
        ?.let { Path(it) }
    if (sourcePath == null) {
        printUsage()
        return
    }
    templateImpl(sourcePath)
}

private fun printUsage() {
    println("Usage: genstrings template {source-path}")
}

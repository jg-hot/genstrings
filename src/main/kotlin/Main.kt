package io.genstrings

import io.genstrings.command.template
import io.genstrings.command.translate
import kotlin.io.path.Path

fun main(args: Array<String>) {
    val command = args.getOrNull(0)
    when (command) {
        "template" -> runTemplate(args.drop(1))
        "translate" -> runTranslate(args.drop(1))
        else -> printUsage()
    }
}

private fun runTemplate(args: List<String>) {
    val sourcePath = args.getOrNull(0)
        ?.let { Path(it) }
    if (sourcePath == null) {
        printUsage()
        return
    }
    template(sourcePath)
}

private fun runTranslate(args: List<String>) {
    val templatePath = args.getOrNull(0)
        ?.let { Path(it) }
    if (templatePath == null) {
        printUsage()
        return
    }
    translate(templatePath)
}

private fun printUsage() {
    println("Usage: genstrings template {source-path}")
}

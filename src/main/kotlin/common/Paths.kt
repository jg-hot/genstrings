package io.genstrings.common

import io.genstrings.model.Language
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

fun resolveTemplatePath(sourcePath: Path): Path {
    val dir = generateSequence(sourcePath) { it.parent }
        .firstOrNull { it.name == "res" }
        ?.parent
        ?.resolve("strings")
        ?: throw IOException(
            "Source file not contained in a `res` directory. Unsure where to place template file."
        )
    val name = sourcePath.name.replace(".xml", ".yaml")
    return dir / name
}

fun resolveTranslationListPath(templatePath: Path, language: Language) : Path {
    val name = "${templatePath.nameWithoutExtension}-${language.locale}.yaml"
    return templatePath.parent / name
}

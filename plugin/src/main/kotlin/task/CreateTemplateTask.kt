package io.genstrings.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.nio.file.Files
import java.nio.file.Path

abstract class CreateTemplateTask : DefaultTask() {

    private lateinit var templatePath: Path

    @Option(option = "path", description = "Path to existing strings.xml file")
    fun setTemplatePath(value: String) {
        templatePath = project.layout.projectDirectory.file(value).asFile.toPath()
    }

    @TaskAction
    fun run() {
        if (!::templatePath.isInitialized) {
            throw IllegalArgumentException("Path to strings.xml must be specified with --path")
        }
        if (!Files.exists(templatePath)) {
            throw IllegalArgumentException("Strings.xml file not found at: $templatePath")
        }
    }
}

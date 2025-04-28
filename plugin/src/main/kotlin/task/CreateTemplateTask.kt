package io.genstrings.task

import io.genstrings.action.CreateTemplateAction
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.nio.file.Files
import java.nio.file.Path

abstract class CreateTemplateTask : DefaultTask() {

    private lateinit var sourcePath: Path

    @Option(option = "path", description = "Path to existing strings.xml file")
    @Suppress("unused")
    fun setTemplatePath(value: String) {
        sourcePath = project.layout.projectDirectory.file(value).asFile.toPath()
    }

    @TaskAction
    fun run() {
        if (!::sourcePath.isInitialized) {
            throw IllegalArgumentException("Path to strings.xml must be specified with --path")
        }
        if (!Files.exists(sourcePath)) {
            throw IllegalArgumentException("Strings.xml file not found at: $sourcePath")
        }
        CreateTemplateAction(sourcePath).run()
    }
}

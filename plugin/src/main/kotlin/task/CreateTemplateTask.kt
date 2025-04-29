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

    // TODO: make main description shorter (one line) as it shows in ./gradlew tasks
    init {
        group = "Genstrings"
        description = """
            Create a template strings.yaml from existing strings.xml
            
            * The new strings.yaml will be registered in the build and will be the new source
              of truth for the strings in that file
              
            * The existing strings.xml will be overwritten on the next regular build via
              :genstringsUpdate
              
            * Before running another build, sync your comments and other metadata between
              strings.xml -> strings.yaml and double check strings.yaml for correctness
              
            * Ensure the existing file contains *only* <string> tags, as other resource
              types (e.g. <color>) will not be preserved
        """.trimIndent()
    }

    @TaskAction
    fun run() {
        if (!::sourcePath.isInitialized) {
            throw IllegalArgumentException("Path to strings.xml must be specified with --path")
        }
        if (!Files.exists(sourcePath)) {
            throw IllegalArgumentException("Strings.xml file not found at: $sourcePath")
        }
        CreateTemplateAction(sourcePath).execute()
    }
}

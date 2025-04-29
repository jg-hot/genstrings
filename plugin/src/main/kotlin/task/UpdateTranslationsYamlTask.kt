package io.genstrings.task

import io.genstrings.action.UpdateTranslationsYamlAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


abstract class UpdateTranslationsYamlTask : DefaultTask() {

    @get:InputFiles
    abstract val sourceYamlFiles: ConfigurableFileCollection

    // TODO: is this the right specifier here?
    @get:OutputDirectory
    abstract val translationsDir: DirectoryProperty

    init {
        group = "Genstrings"
        description = "Updates translations .yaml" // TODO: update description
    }

    // TODO: iterate langauge and run one action per language and source yaml file
    @TaskAction
    fun run() {
        sourceYamlFiles.forEach { sourceYamlFile ->
            UpdateTranslationsYamlAction(
                templatePath = sourceYamlFile.toPath(),
                outputDir = translationsDir.get().asFile.toPath(),
            ).execute()
        }
    }
}

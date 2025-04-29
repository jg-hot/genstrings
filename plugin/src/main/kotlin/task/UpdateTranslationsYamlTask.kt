package io.genstrings.task

import io.genstrings.action.UpdateTranslationsYamlAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class UpdateTranslationsYamlTask : DefaultTask() {

    @get:InputFiles
    abstract val sourceYamlFiles: ConfigurableFileCollection

    @get:Input
    abstract val locales: SetProperty<String>

    // TODO: is this the right specifier here?
    @get:OutputDirectory
    abstract val translationsDir: DirectoryProperty

    init {
        group = "Genstrings"
        description = "Update translation .yaml files to remove out-of-date or deleted strings"
    }

    @TaskAction
    fun run() {
        sourceYamlFiles.forEach { sourceYamlFile ->
            UpdateTranslationsYamlAction(
                templatePath = sourceYamlFile.toPath(),
                locales = locales.get(),
                outputDir = translationsDir.get().asFile.toPath(),
            ).execute()
        }
    }
}

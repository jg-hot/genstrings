package io.genstrings.task

import io.genstrings.action.UpdateTranslationsYamlAction
import io.genstrings.model.Language
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

abstract class UpdateTranslationsYamlTask : DefaultTask() {

    @get:InputFiles
    abstract val sourceYamlFiles: ConfigurableFileCollection

    @get:Input
    abstract val languages: ListProperty<Language>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val translationsDir: DirectoryProperty

    init {
        group = "Genstrings"
        description = "Update translation .yaml files to remove out-of-date or deleted strings"
    }

    @TaskAction
    fun run() {
        val outputDir = translationsDir.get().asFile.toPath()

        // .yaml files for languages no longer specified in plugin will be deleted
        val updatedFileNames = mutableListOf<String>()

        sourceYamlFiles.forEach { sourceYamlFile ->
            val templatePath = sourceYamlFile.toPath()

            UpdateTranslationsYamlAction(
                templatePath = templatePath,
                languages = languages.get(),
                outputDir = outputDir,
            ).execute()

            updatedFileNames += languages.get().map { language ->
                "${templatePath.nameWithoutExtension}-${language.locale}.yaml"
            }
        }

        // cleanup files that weren't updated
        outputDir.listDirectoryEntries().filterNot {
            it.name in updatedFileNames
        }.forEach {
            Files.delete(it)
        }
    }
}

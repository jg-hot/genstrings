package io.genstrings.task

import io.genstrings.action.TranslateAction
import io.genstrings.model.Language
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

abstract class TranslateTask : DefaultTask() {

    @get:InputFiles
    abstract val configFile: RegularFileProperty

    @get:InputFiles
    abstract val sourceYamlFiles: ConfigurableFileCollection

    @get:Input
    abstract val languages: SetProperty<Language>

    init {
        group = "Genstrings"
    }

    @TaskAction
    fun run() {
        TranslateAction(
            configFile = configFile.get().asFile.toPath(),
            templateFiles = sourceYamlFiles.toList().map { it.toPath() },
            languages = languages.get().toList().sortedBy {
                it.locale
            }
        ).execute()
    }
}

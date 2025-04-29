package io.genstrings.task

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
        println("config file: ${configFile.get().asFile.absolutePath}")
        languages.get().forEach {
            println("translate language: $it")
        }
    }
}

package io.genstrings.task

import io.genstrings.action.ProcessStringsYamlToXmlAction
import io.genstrings.model.Language
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class ProcessStringsYamlToXmlTask : DefaultTask() {

    @get:InputFiles
    abstract val sourceYamlFiles: ConfigurableFileCollection

    @get:InputFiles
    abstract val translationYamlFiles: ConfigurableFileCollection

    @get:Input
    abstract val languages: ListProperty<Language>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        group = "Genstrings"
        description = "Generate source and translated strings.xml files from strings.yaml"
    }

    // TODO: run action once per source yaml file, including list of languages property
    @TaskAction
    fun run() {
        sourceYamlFiles.forEach { sourceYamlFile ->
            ProcessStringsYamlToXmlAction(
                templatePath = sourceYamlFile.toPath(),
                languages = languages.get(),
                outputDir = outputDir.get().asFile.toPath(),
            ).execute()
        }
    }
}

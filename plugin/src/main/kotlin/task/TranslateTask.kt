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
import org.gradle.api.tasks.options.Option

abstract class TranslateTask : DefaultTask() {

    @get:InputFiles
    abstract val configFile: RegularFileProperty

    @get:InputFiles
    abstract val sourceYamlFiles: ConfigurableFileCollection

    @get:Input
    abstract val languages: SetProperty<Language>

    private var stringNames: List<String>? = null

    private var retranslate: Boolean = false

    @Option(
        option = "string",
        description = "Specify individual strings by name. If not set, all out-of-date strings will be translated.",
    )
    fun setStringNames(value: List<String>) {
        stringNames = value
    }

    @Option(
        option = "update",
        description = "Re-translate strings that already have an up-to-date translation. Defaults to false."
    )
    fun setRetranslate(value: Boolean) {
        retranslate = value
    }

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
            },
            stringNames = stringNames?.toSet(),
            retranslate = retranslate,
        ).execute()
    }
}

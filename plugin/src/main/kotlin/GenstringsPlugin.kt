package io.genstrings

import com.android.build.api.variant.AndroidComponentsExtension
import io.genstrings.common.AndroidLocaleQualifier
import io.genstrings.plugin.GenstringsExtension.Companion.registerExtension
import io.genstrings.task.CreateTemplateTask
import io.genstrings.task.ProcessStringsYamlToXmlTask
import io.genstrings.task.TranslateTask
import io.genstrings.task.UpdateTranslationsYamlTask
import io.genstrings.util.cap
import org.gradle.api.Plugin
import org.gradle.api.Project

// TODO support variant specific source sets in addition to `main`
class GenstringsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val androidComponents = target.extensions.getByType(AndroidComponentsExtension::class.java)
        val extension = target.registerExtension().apply {
            configFileImpl.convention(
                target.layout.projectDirectory
                    .dir(".genstrings")
                    .file("config.yaml")
            )
        }
        val languages = target.providers.provider {
            extension.languages.map {
                it.toLanguage()
            }
        }

        val stringsDir = target.layout
            .projectDirectory
            .dir("src/main/strings")

        val translationsDir = stringsDir
            .dir("translations")

        val sourceYamlFiles = stringsDir.asFileTree.matching {
            it.include("*.yaml")
            it.exclude("translations/")
        }

        val translationYamlFiles = stringsDir.dir("translations").asFileTree.matching {
            it.include("*.yaml")
        }

        target.tasks.register(
            "genstringsCreateTemplate", CreateTemplateTask::class.java
        )

        val updateTask = target.tasks.register(
            "updateTranslationsYaml", UpdateTranslationsYamlTask::class.java
        ) { task ->
            task.sourceYamlFiles.from(sourceYamlFiles)
            task.languages.set(languages)
            task.translationsDir.set(translationsDir)
        }

        androidComponents.onVariants { variant ->
            val processTaskName = "process${variant.name.cap()}StringsYamlToXml"

            val outputDir = target.layout.buildDirectory
                .dir("genstrings/${variant.name}")

            val processTask = target.tasks.register(
                processTaskName, ProcessStringsYamlToXmlTask::class.java
            ) { task ->
                task.sourceYamlFiles.from(sourceYamlFiles)
                task.translationYamlFiles.from(translationYamlFiles)
                task.languages.set(languages)
                task.outputDir.set(outputDir)
                task.dependsOn(updateTask)
            }
            variant.sources.res?.addGeneratedSourceDirectory(
                processTask,
                ProcessStringsYamlToXmlTask::outputDir,
            )
        }

        target.tasks.register(
            "genstringsTranslate", TranslateTask::class.java
        ) { task ->
            task.description = "Generate translations for all languages"
            task.configFile.set(extension.configFile)
            task.sourceYamlFiles.from(sourceYamlFiles)
            task.languages.set(languages)
            task.dependsOn(updateTask)
        }
        extension.languages.all { entry ->
            val language = target.providers.provider {
                setOf(entry.toLanguage())
            }
            val taskName = buildString {
                append("genstringsTranslate")
                AndroidLocaleQualifier.parse(entry.name).toString().split("-").forEach {
                    append(it.cap())
                }
            }
            target.tasks.register(
                taskName, TranslateTask::class.java
            ) { task ->
                task.description = "Generate translations for ${entry.description}"
                task.configFile.set(extension.configFile)
                task.sourceYamlFiles.from(sourceYamlFiles)
                task.languages.set(language)
                task.dependsOn(updateTask)
            }
        }
    }
}

package io.genstrings.plugin

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import java.io.File
import javax.inject.Inject

open class GenstringsExtension @Inject constructor(
    objects: ObjectFactory,
) {
    internal val configFileImpl = objects.fileProperty()

    internal val languages = objects.domainObjectContainer(LanguageHandler::class.java)

    var configFile: File
        get() = configFileImpl.get().asFile
        set(value) {
            configFileImpl.set(value)
        }

    fun languages(action: Action<NamedDomainObjectContainer<LanguageHandler>>) {
        action.execute(languages)
    }

    companion object {
        internal fun Project.registerExtension() =
            extensions.create("genstrings", GenstringsExtension::class.java)
    }
}

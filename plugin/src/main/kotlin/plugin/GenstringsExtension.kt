package io.genstrings.plugin

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class GenstringsExtension @Inject constructor(
    objects: ObjectFactory,
) {
    internal val languages = objects.domainObjectContainer(LanguageHandler::class.java)

    fun languages(action: Action<NamedDomainObjectContainer<LanguageHandler>>) {
        action.execute(languages)
    }

    companion object {
        internal fun Project.registerExtension() =
            extensions.create("genstrings", GenstringsExtension::class.java)
    }
}

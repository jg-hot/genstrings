package io.genstrings.plugin

import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class LanguageHandler @Inject constructor(
    private val name: String,
    objects: ObjectFactory,
) : Named {
    override fun getName() = name

    private val descriptionImpl: Property<String> = objects.property(String::class.java)

    var description: String?
        get() = descriptionImpl.orNull
        set(value) {
            descriptionImpl.set(value)
        }
}

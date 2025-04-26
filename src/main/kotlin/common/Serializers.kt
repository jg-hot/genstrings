package io.genstrings.common

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNamingStrategy

object Serializers {
    val yaml by lazy {
        val config = YamlConfiguration(
            encodeDefaults = false,
            yamlNamingStrategy = YamlNamingStrategy.SnakeCase,
        )
        Yaml(configuration = config)
    }
}

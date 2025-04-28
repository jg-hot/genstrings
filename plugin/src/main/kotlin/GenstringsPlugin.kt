package io.genstrings

import io.genstrings.task.CreateTemplateTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GenstringsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("genstringsCreateTemplate", CreateTemplateTask::class.java) {
            it.group = "Genstrings"
            it.description = """
                Create a template strings.yaml from existing strings.xml
                
                * The new strings.yaml will be registered in the build and will be the new source
                  of truth for the strings in that file
                  
                * The existing strings.xml will be overwritten on the next regular build via
                  :genstringsUpdate
                  
                * Before running another build, sync your comments and other metadata between
                  strings.xml -> strings.yaml and double check strings.yaml for correctness
                  
                * Ensure the existing file contains *only* <string> tags, as other resource
                  types (e.g. <color>) will not be preserved
            """.trimIndent()
        }
    }
}

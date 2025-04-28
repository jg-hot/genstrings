package io.genstrings.command

import com.charleskorn.kaml.encodeToStream
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.genstrings.common.Serializers
import io.genstrings.common.decodeAndroidFormatArgs
import io.genstrings.common.decodeRawAndroidString
import io.genstrings.common.resolveTemplatePath
import io.genstrings.model.StringResource
import io.genstrings.model.StringsTemplate
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createParentDirectories

fun template(sourcePath: Path) {

}

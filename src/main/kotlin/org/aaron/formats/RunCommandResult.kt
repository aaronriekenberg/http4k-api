package org.aaron.formats

import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Body
import org.http4k.format.Jackson.auto

data class RunCommandResult(

    @field:JsonProperty("output_lines")
    val outputLines: List<String>,

    @field:JsonProperty("exit_value")
    val exitValue: Int
)

val runCommandResultLens = Body.auto<RunCommandResult>().toLens()

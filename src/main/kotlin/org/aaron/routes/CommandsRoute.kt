package org.aaron.routes

import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import java.io.InputStreamReader

data class RunCommandResult(

    @JsonProperty("output_lines")
    val outputLines: List<String>,

    @JsonProperty("exit_value")
    val exitValue: Int
)

val runCommandResultLens = Body.auto<RunCommandResult>().toLens()

object CommandsRoute {
    operator fun invoke() = "/api/v1/run_command" bind GET to {
        val commandAndArgs = listOf("ls", "-latrh")

        val processBuilder = ProcessBuilder(commandAndArgs)

        processBuilder.redirectErrorStream(true)

        val process = processBuilder.start()

        val exitValue = process.waitFor()

        val outputLines = InputStreamReader(process.inputStream).readLines()

        Response(OK).with(
            runCommandResultLens of
                    RunCommandResult(
                        outputLines = outputLines,
                        exitValue = exitValue,
                    )
        )
    }
}
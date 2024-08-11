package org.aaron.routes

import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import java.io.InputStreamReader
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Semaphore

data class CommandInfoDTO(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("description")
    val description: String,

    @JsonProperty("command")
    val command: String,

    @JsonProperty("args")
    val args: List<String> = listOf(),
)

data class RunCommandResult(
    @JsonProperty("command_info")
    val commandInfo: CommandInfoDTO,

    @JsonProperty("now")
    val now: String,

    @JsonProperty("command_duration_ms")
    val commandDurationMilliseconds: Long,

    @JsonProperty("command_output")
    val commandOutput: String,
)

val allCommandsListResultLens = Body.auto<List<CommandInfoDTO>>().toLens()

val runCommandResultLens = Body.auto<RunCommandResult>().toLens()

object CommandsRoute {
    operator fun invoke() = "/api/v1/commands" bind GET to
            routes(
                "/" bind {
                    Response(OK).with(
                        allCommandsListResultLens of CommandService.allCommands()
                    )
                },
                "/{id}" bind { request ->
                    val id = request.path("id")
                    println("got command id $id")

                    val result = CommandService.runCommand(id = id)

                    if (result == null) {
                        Response(NOT_FOUND)
                    } else {
                        Response(OK).with(
                            runCommandResultLens of result
                        )
                    }

                },
            )
}

val commandsMap = mapOf(
    "w" to CommandInfoDTO(
        id = "w",
        description = "w",
        command = "/usr/bin/w",
    ),
    "sleep" to CommandInfoDTO(
        id = "sleep",
        description = "sleep",
        command = "/usr/bin/sleep",
        args = listOf(
            "0.5",
        ),
    ),
)

object CommandService {

    private val semaphore = Semaphore(10)

    fun allCommands(): List<CommandInfoDTO> = commandsMap.values.toList()

    fun runCommand(id: String?): RunCommandResult? {
        if (id == null) {
            return null
        }

        val commandInfo = commandsMap[id] ?: return null

        semaphore.acquireUninterruptibly()
        try {
            val commandAndArgs = listOf(commandInfo.command) + commandInfo.args

            val processBuilder = ProcessBuilder(commandAndArgs)

            processBuilder.redirectErrorStream(true)

            val startTimeEpochMilli = Instant.now().toEpochMilli()

            val process = processBuilder.start()

            process.waitFor()

            val commandDurationMilliseconds = Instant.now().toEpochMilli() - startTimeEpochMilli

            val outputString = InputStreamReader(process.inputStream)
                .readLines()
                .joinToString(separator = "\n")

            return RunCommandResult(
                commandInfo = commandInfo,
                now = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
                commandOutput = outputString,
                commandDurationMilliseconds = commandDurationMilliseconds,
            )
        } finally {
            semaphore.release()
        }
    }

}

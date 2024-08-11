package org.aaron.routes

import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.TOO_MANY_REQUESTS
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
import java.util.concurrent.TimeUnit

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

data class RunCommandResultDTO(
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

val runCommandResultLens = Body.auto<RunCommandResultDTO>().toLens()

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

                    val result = CommandService.runCommand(id = id)

                    when (result.type) {
                        RunCommandResultType.COMMAND_NOT_FOUND -> Response(NOT_FOUND)
                        RunCommandResultType.TOO_MANY_COMMANDS_RUNNING -> Response(TOO_MANY_REQUESTS)
                        RunCommandResultType.OK -> Response(OK).with(
                            runCommandResultLens of result.runCommandResultDTO!!,
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

enum class RunCommandResultType {
    COMMAND_NOT_FOUND,
    TOO_MANY_COMMANDS_RUNNING,
    OK,
}

data class RunCommandResult(
    val type: RunCommandResultType,
    val runCommandResultDTO: RunCommandResultDTO? = null,
)

object CommandService {

    private val semaphore = Semaphore(10)

    fun allCommands(): List<CommandInfoDTO> = commandsMap.values.toList()

    fun runCommand(id: String?): RunCommandResult {
        if (id == null) {
            return RunCommandResult(
                type = RunCommandResultType.COMMAND_NOT_FOUND,
            )
        }

        val commandInfo = commandsMap[id] ?: return RunCommandResult(
            type = RunCommandResultType.COMMAND_NOT_FOUND,
        )

        if (!semaphore.tryAcquire(200, TimeUnit.MILLISECONDS)) {
            return RunCommandResult(type = RunCommandResultType.TOO_MANY_COMMANDS_RUNNING)
        }

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

            val runCommandResultDTO = RunCommandResultDTO(
                commandInfo = commandInfo,
                now = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
                commandOutput = outputString,
                commandDurationMilliseconds = commandDurationMilliseconds,
            )

            return RunCommandResult(
                type = RunCommandResultType.OK,
                runCommandResultDTO = runCommandResultDTO,
            )
        } finally {
            semaphore.release()
        }
    }

}

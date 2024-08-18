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
    operator fun invoke() = "/commands" bind GET to
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

private val commandsMap = listOf(
    CommandInfoDTO(
        id = "chronyc_sources",
        description = "chronyc sources",
        command = "/usr/bin/chronyc",
        args = listOf(
            "-n",
            "sources",
            "-v",
        ),
    ),
    CommandInfoDTO(
        id = "chronyc_sourcestats",
        description = "chronyc sourcestats",
        command = "/usr/bin/chronyc",
        args = listOf(
            "-n",
            "sourcestats",
            "-v",
        ),
    ),
    CommandInfoDTO(
        id = "df",
        description = "df",
        command = "/usr/bin/df",
        args = listOf("-h"),
    ),
    CommandInfoDTO(
        id = "git_log",
        description = "git log",
        command = "/usr/bin/git",
        args = listOf(
            "log",
            "-1",
        ),
    ),
    CommandInfoDTO(
        id = "ip_addr",
        description = "ip addr",
        command = "/usr/sbin/ip",
        args = listOf(
            "addr",
        ),
    ),
    CommandInfoDTO(
        id = "lscpu",
        description = "lscpu",
        command = "/usr/bin/lscpu",
    ),
    CommandInfoDTO(
        id = "lscpu_e",
        description = "lscpu -e",
        command = "/usr/bin/lscpu",
        args = listOf(
            "-e",
        ),
    ),
    CommandInfoDTO(
        id = "netstat_an",
        description = "netstat -an",
        command = "/usr/bin/netstat",
        args = listOf(
            "-an",
        ),
    ),
    CommandInfoDTO(
        id = "sensors",
        description = "sensors",
        command = "/usr/bin/sensors",
    ),
    CommandInfoDTO(
        id = "top",
        description = "top",
        command = "/usr/bin/top",
        args = listOf(
            "-b",
            "-n1",
        ),
    ),
    CommandInfoDTO(
        id = "top_ores",
        description = "top -o RES",
        command = "/usr/bin/top",
        args = listOf(
            "-b",
            "-n1",
            "-o",
            "RES",
        ),
    ),
    CommandInfoDTO(
        id = "uptime",
        description = "uptime",
        command = "/usr/bin/uptime",
    ),
    CommandInfoDTO(
        id = "vmstat",
        description = "vmstat",
        command = "/usr/bin/vmstat",
    ),
    CommandInfoDTO(
        id = "w",
        description = "w",
        command = "/usr/bin/w",
    ),
).associateBy { it.id }

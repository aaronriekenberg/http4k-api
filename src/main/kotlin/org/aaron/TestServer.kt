package org.aaron

import org.aaron.formats.JacksonMessage
import org.aaron.formats.RunCommandResult
import org.aaron.formats.jacksonMessageLens
import org.aaron.formats.runCommandResultLens
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.OK
import org.http4k.events.*
import org.http4k.filter.ResponseFilters
import org.http4k.format.Jackson
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicLong

// this is our custom event which will be printed in a structured way
data class IncomingHttpRequest(val uri: Uri, val status: Int, val duration: Long) : Event

data class ServerStartedEvent(val port: Int) : Event

val app: HttpHandler = routes(
    "/ping" bind GET to {
        println("in ping thread name = ${Thread.currentThread().name}")
        Response(OK).body("pong")
    },

    "/formats/json/jackson" bind GET to {
        Response(OK).with(jacksonMessageLens of JacksonMessage("Barry", "Hello there!"))
    },

    "/api/v1/run_command" bind GET to {
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
    },
)

fun main() {
    // Stack filters for Events in the same way as HttpHandlers to
    // transform or add metadata to the Events.
    // We use AutoMarshallingEvents (here with Jackson) to
    // handle the final serialisation process.
    val events =
        EventFilters.AddTimestamp()
            .then(EventFilters.AddEventName())
//            .then(EventFilters.AddZipkinTraces())
            .then(addRequestCount())
            .then(AutoMarshallingEvents(Jackson))


    val appWithEvents =
        ResponseFilters.ReportHttpTransaction {
            // to "emit" an event, just invoke() the Events!
            events(
                IncomingHttpRequest(
                    uri = it.request.uri,
                    status = it.response.status.code,
                    duration = it.duration.toMillis(),
                )
            )
        }.then(app)

    val server = appWithEvents.asServer(Undertow(port = 8080)).start()

    events(
        ServerStartedEvent(port = server.port())
    )
}

// here is a new EventFilter that adds custom metadata to the emitted events
fun addRequestCount(): EventFilter {
    val requestCount = AtomicLong(0)
    return EventFilter { next ->
        {
            next(it + ("requestCount" to requestCount.getAndAdd(1)))
        }
    }
}
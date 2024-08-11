package org.aaron

import org.aaron.routes.CommandsRoute
import org.aaron.routes.HealthRoute
import org.aaron.routes.RequestInfoRoute
import org.http4k.core.*
import org.http4k.events.*
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.atomic.AtomicLong

// this is our custom event which will be printed in a structured way
data class IncomingHttpRequest(val uri: Uri, val status: Int, val duration: Long) : Event

data class CatchAllExceptionEvent(val stackTrace: String) : Event

data class ServerStartedEvent(val port: Int) : Event

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

    val app: HttpHandler = routes(
        CommandsRoute(),
        HealthRoute(),
        RequestInfoRoute(),
    )

    val catchAll = ServerFilters.CatchAll { error ->
        val stackTraceAsString = StringWriter().apply {
            error.printStackTrace(PrintWriter(this))
        }.toString()

        events(CatchAllExceptionEvent(stackTrace = stackTraceAsString))

        Response(Status.INTERNAL_SERVER_ERROR)
    }

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
        }.then(catchAll)
            .then(app)

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
package org.aaron.event

import org.aaron.context.requestSharedStateKey
import org.aaron.environment.env
import org.aaron.environment.requestRecordingEnabled
import org.aaron.json.jsonFormat
import org.http4k.core.*
import org.http4k.events.AutoMarshallingEvents
import org.http4k.events.Event
import org.http4k.events.EventFilters
import org.http4k.events.then
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import java.io.PrintWriter
import java.io.StringWriter

// this is our custom event which will be printed in a structured way
data class IncomingHttpRequest(val uri: Uri, val status: Int, val duration: Long, val requestID: Long) : Event

data class CatchAllExceptionEvent(val stackTrace: String) : Event

data class ServerStartedEvent(val version: String, val port: Int, val backendServer: String) : Event

// Stack filters for Events in the same way as HttpHandlers to
// transform or add metadata to the Events.
// We use AutoMarshallingEvents (here with Jackson) to
// handle the final serialisation process.
val events =
    EventFilters.AddTimestamp()
        .then(EventFilters.AddEventName())
//            .then(EventFilters.AddZipkinTraces())
        .then(AutoMarshallingEvents(jsonFormat))


val catchAllFilter = ServerFilters.CatchAll { error ->
    val stackTraceAsString = StringWriter().apply {
        error.printStackTrace(PrintWriter(this))
    }.toString()

    events(CatchAllExceptionEvent(stackTrace = stackTraceAsString))

    Response(Status.INTERNAL_SERVER_ERROR)
}

val recordHttpTransactionFilter =
    if (!requestRecordingEnabled(env)) {
        Filter.NoOp
    } else {
        ResponseFilters.ReportHttpTransaction {
            events(
                IncomingHttpRequest(
                    uri = it.request.uri,
                    status = it.response.status.code,
                    duration = it.duration.toMillis(),
                    requestID = requestSharedStateKey(it.request).requestID,
                )
            )
        }
    }

package org.aaron.event

import org.aaron.context.requestSharedStateKey
import org.aaron.environment.env
import org.aaron.environment.requestRecordingEnabled
import org.aaron.json.jsonFormat
import org.http4k.core.*
import org.http4k.events.*
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import se.ansman.kotshi.JsonSerializable
import java.io.PrintWriter
import java.io.StringWriter

// this is our custom event which will be printed in a structured way
@JsonSerializable
data class IncomingHttpRequest(val uri: Uri, val status: Int, val duration: Long, val requestID: Long) : Event

@JsonSerializable
data class CatchAllExceptionEvent(val stackTrace: String) : Event

@JsonSerializable
data class ServerStartedEvent(val version: String, val port: Int, val backendServer: String) : Event

@JsonSerializable
data class SerializableMetadataEvent(
    val event: Event,
    val metadata: Map<String, Any>,
)

object MoshiMarshallingEvents {
    operator fun invoke() = object : Events {
        override fun invoke(event: Event) {
            when (event) {
                is MetadataEvent -> print(
                    jsonFormat.asFormatString(
                        SerializableMetadataEvent(
                            event = event.event,
                            metadata = event.metadata
                        )
                    )
                )

                else -> print("MoshiMarshallingEvents got unknown event $event")
            }
        }
    }
}

// Stack filters for Events in the same way as HttpHandlers to
// transform or add metadata to the Events.
// We use AutoMarshallingEvents (here with Jackson) to
// handle the final serialisation process.
val events =
    EventFilters.AddTimestamp()
        .then(EventFilters.AddEventName())
//            .then(EventFilters.AddZipkinTraces())
        .then(MoshiMarshallingEvents())


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

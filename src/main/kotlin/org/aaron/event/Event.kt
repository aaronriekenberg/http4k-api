package org.aaron.event

import org.http4k.core.Uri
import org.http4k.events.AutoMarshallingEvents
import org.http4k.events.Event
import org.http4k.events.EventFilters
import org.http4k.events.then
import org.http4k.format.Jackson

// this is our custom event which will be printed in a structured way
data class IncomingHttpRequest(val uri: Uri, val status: Int, val duration: Long, val requestID: Long) : Event

data class CatchAllExceptionEvent(val stackTrace: String) : Event

data class ServerStartedEvent(val port: Int) : Event

// Stack filters for Events in the same way as HttpHandlers to
// transform or add metadata to the Events.
// We use AutoMarshallingEvents (here with Jackson) to
// handle the final serialisation process.
val events =
    EventFilters.AddTimestamp()
        .then(EventFilters.AddEventName())
//            .then(EventFilters.AddZipkinTraces())
        .then(AutoMarshallingEvents(Jackson))

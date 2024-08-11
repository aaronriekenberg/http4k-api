package org.aaron

import org.aaron.context.addRequestSharedState
import org.aaron.context.requestContexts
import org.aaron.context.requestSharedStateKey
import org.aaron.event.CatchAllExceptionEvent
import org.aaron.event.IncomingHttpRequest
import org.aaron.event.ServerStartedEvent
import org.aaron.event.events
import org.aaron.routes.CommandsRoute
import org.aaron.routes.HealthRoute
import org.aaron.routes.RequestInfoRoute
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.io.PrintWriter
import java.io.StringWriter

fun main() {

    val appRoutes = routes(
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
        ServerFilters.InitialiseRequestContext(requestContexts)
            .then(
                addRequestSharedState(),
            ).then(
                ResponseFilters.ReportHttpTransaction {
                    // to "emit" an event, just invoke() the Events!
                    events(
                        IncomingHttpRequest(
                            uri = it.request.uri,
                            status = it.response.status.code,
                            duration = it.duration.toMillis(),
                            requestID = requestSharedStateKey(it.request).requestID,
                        )
                    )
                })
            .then(catchAll)
            .then(appRoutes)

    val server = appWithEvents.asServer(Undertow(port = 8080)).start()

    events(
        ServerStartedEvent(port = server.port())
    )
}


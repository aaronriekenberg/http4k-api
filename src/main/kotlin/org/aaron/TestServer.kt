package org.aaron

import org.aaron.config.port
import org.aaron.context.requestContextFilter
import org.aaron.event.ServerStartedEvent
import org.aaron.event.catchAllFilter
import org.aaron.event.events
import org.aaron.event.recordHttpTransactionFilter
import org.aaron.routes.CommandsRoute
import org.aaron.routes.HealthRoute
import org.aaron.routes.RequestInfoRoute
import org.http4k.core.Method.GET
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.JettyLoom
import org.http4k.server.asServer

fun main() {

    val appRoutes =
        routes(
            "/api/v1" bind GET to routes(
                CommandsRoute(),
                RequestInfoRoute(),
            ),
            HealthRoute(),
        )

    val appWithFilters =
        requestContextFilter()
            .then(
                recordHttpTransactionFilter
            )
            .then(catchAllFilter)
            .then(appRoutes)

    val server = appWithFilters.asServer(JettyLoom(port = port.value)).start()

    events(
        ServerStartedEvent(port = server.port())
    )
}


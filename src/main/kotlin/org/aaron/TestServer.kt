package org.aaron

import org.aaron.context.requestContextFilter
import org.aaron.event.*
import org.aaron.routes.CommandsRoute
import org.aaron.routes.HealthRoute
import org.aaron.routes.RequestInfoRoute
import org.http4k.core.Method.GET
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
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

    val server = appWithFilters.asServer(Undertow(port = 8080)).start()

    events(
        ServerStartedEvent(port = server.port())
    )
}


package org.aaron

import org.aaron.context.requestContextFilter
import org.aaron.environment.port
import org.aaron.environment.version
import org.aaron.event.ServerStartedEvent
import org.aaron.event.catchAllFilter
import org.aaron.event.events
import org.aaron.event.recordHttpTransactionFilter
import org.aaron.routes.CommandsRoute
import org.aaron.routes.HealthRoute
import org.aaron.routes.JVMInfoRoute
import org.aaron.routes.RequestInfoRoute
import org.http4k.core.Method.GET
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Helidon
import org.http4k.server.asServer

fun main() {

    val appRoutes =
        routes(
            "/api/v1" bind GET to routes(
                CommandsRoute(),
                JVMInfoRoute(),
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

    val server = appWithFilters.asServer(
        Helidon(
            port = port.value,
        )
    ).start()

    events(
        ServerStartedEvent(
            version = version.version,
            port = server.port(),
            backendServer = "Helidon",
        )
    )
}


package org.aaron.helidon

import io.helidon.webserver.WebServer
import io.helidon.webserver.http.HttpRouting
import org.http4k.core.HttpHandler
import org.http4k.server.HelidonHandler
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import org.http4k.server.ServerConfig.StopMode.Immediate

class CustomHelidon(val port: Int = 8000) : ServerConfig {
    override val stopMode = Immediate

    override fun toServer(http: HttpHandler): Http4kServer = object : Http4kServer {
        private val server = WebServer.builder()
            .addRouting(HttpRouting.builder().any(HelidonHandler(http)))
            .port(port)
            .connectionOptions { socketOptions ->
                socketOptions.tcpNoDelay(true)
                println("set tcpNoDelay(true)")
            }
            .build()

        override fun start() = apply { server.start() }

        override fun stop() = apply { server.stop() }

        override fun port(): Int = if (port != 0) port else server.port()
    }
}

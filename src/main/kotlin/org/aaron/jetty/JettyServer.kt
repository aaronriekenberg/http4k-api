package org.aaron.jetty

import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.http4k.server.ConnectorBuilder
import org.http4k.server.Jetty
import java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor

fun jettyLoomH2C(port: Int) = Jetty(
    port = port,
    server = Server(QueuedThreadPool().apply {
        virtualThreadsExecutor = newVirtualThreadPerTaskExecutor()
    }).apply {
        addConnector(jettyH2CConnector(port)(this))
    },
)

fun jettyH2CConnector(http2Port: Int): ConnectorBuilder =
    { server: Server ->

        // The HTTP configuration object.
        val httpConfig = HttpConfiguration().apply {
            sendServerVersion = false
        }

        // The ConnectionFactory for HTTP/1.1.
        val http11 = HttpConnectionFactory(httpConfig)

        // The ConnectionFactory for clear-text HTTP/2.
        val h2c = HTTP2CServerConnectionFactory(httpConfig)

        ServerConnector(
            server,
            http11,
            h2c,
        ).apply { port = http2Port }
    }
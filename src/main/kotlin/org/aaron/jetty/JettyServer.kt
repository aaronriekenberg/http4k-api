package org.aaron.jetty

import org.aaron.environment.env
import org.aaron.environment.unixSocketPath
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.unixdomain.server.UnixDomainServerConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.http4k.server.ConnectorBuilder
import org.http4k.server.Jetty
import java.nio.file.Paths
import java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor
import kotlin.io.path.deleteIfExists

fun jettyLoomH2C(port: Int) = Jetty(
    port = port,
    server = Server(QueuedThreadPool().apply {
        virtualThreadsExecutor = newVirtualThreadPerTaskExecutor()
    }).apply {
        addConnector(jettyH2CConnector(port)(this))

        val unixPathString = unixSocketPath(env)
        if (unixPathString != null) {
            addConnector(jettyH2CUnixConnector(unixPathString)(this))
        }
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

typealias UnixConnectorBuilder = (Server) -> UnixDomainServerConnector

fun jettyH2CUnixConnector(pathString: String): UnixConnectorBuilder =
    { server: Server ->

        // The HTTP configuration object.
        val httpConfig = HttpConfiguration().apply {
            sendServerVersion = false
        }

        // The ConnectionFactory for HTTP/1.1.
        val http11 = HttpConnectionFactory(httpConfig)

        // The ConnectionFactory for clear-text HTTP/2.
        val h2c = HTTP2CServerConnectionFactory(httpConfig)

        val path = Paths.get(pathString)

        path.deleteIfExists()

        UnixDomainServerConnector(
            server,
            http11,
            h2c,
        ).apply { unixDomainPath = path }

    }
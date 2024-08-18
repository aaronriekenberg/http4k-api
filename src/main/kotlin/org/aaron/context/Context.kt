package org.aaron.context

import org.http4k.core.Filter
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import java.util.concurrent.atomic.AtomicLong

data class RequestSharedState(val requestID: Long)

private val nextRequestID = AtomicLong(1)

private val requestContexts = RequestContexts()

val requestSharedStateKey = RequestContextKey.required<RequestSharedState>(requestContexts)

private fun addRequestSharedState() = Filter { next ->
    {
        // "modify" the request like any other Lens
        next(it.with(requestSharedStateKey of RequestSharedState(nextRequestID.getAndAdd(1))))
    }
}

fun requestContextFilter(): Filter = ServerFilters.InitialiseRequestContext(requestContexts)
    .then(
        addRequestSharedState(),
    )
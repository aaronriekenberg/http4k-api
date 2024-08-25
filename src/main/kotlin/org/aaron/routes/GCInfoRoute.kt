package org.aaron.routes

import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory

data class GCDTO(

    @JsonProperty("name")
    val name: String,

    @JsonProperty("collection_count")
    val collectionCount: Long,

    @JsonProperty("collection_time_milliseconds")
    val collectionTimeMilliseconds: Long,

    @JsonProperty("memory_pool_names")
    val memoryPoolNames: List<String>
)

data class GCInfoDTO(

    @field:JsonProperty("gcs")
    val gcResponses: List<GCDTO>
)

private fun GarbageCollectorMXBean.toGCDTO(): GCDTO =
    GCDTO(
        name = name,
        collectionCount = collectionCount,
        collectionTimeMilliseconds = collectionTime,
        memoryPoolNames = memoryPoolNames.toList()
    )

val gcInfoDTOLens = Body.auto<GCInfoDTO>().toLens()

object GCInfoRoute {
    operator fun invoke() =
        "/gc_info" bind GET to {
            val gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans()

            val gcDTOs = GCInfoDTO(
                gcResponses = gcMXBeans.map { it.toGCDTO() }
            )

            Response(OK).with(
                gcInfoDTOLens of gcDTOs
            )
        }
}
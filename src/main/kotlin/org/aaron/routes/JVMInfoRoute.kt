package org.aaron.routes

import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import java.lang.management.*
import kotlin.math.round

data class GCDTO(

    @JsonProperty("name")
    val name: String,

    @JsonProperty("collection_count")
    val collectionCount: Long,

    @JsonProperty("collection_time_milliseconds")
    val collectionTimeMilliseconds: Long,

    @JsonProperty("memory_pool_names")
    val memoryPoolNames: List<String>,
)

data class GCInfoDTO(

    @JsonProperty("gcs")
    val gcDTOs: List<GCDTO>,
)

private fun GarbageCollectorMXBean.toGCDTO() =
    GCDTO(
        name = name,
        collectionCount = collectionCount,
        collectionTimeMilliseconds = collectionTime,
        memoryPoolNames = memoryPoolNames.toList()
    )

fun buildGCInfoDTO(): GCInfoDTO =
    GCInfoDTO(
        gcDTOs = ManagementFactory.getGarbageCollectorMXBeans()
            .map { it.toGCDTO() }
    )

data class MemoryUsageDTO(

    @field:JsonProperty("committed")
    val committed: String,

    @field:JsonProperty("init")
    val init: String,

    @field:JsonProperty("max")
    val max: String,

    @field:JsonProperty("used")
    val used: String,
)

private fun Long.bytesToMiB(): String {
    return if (this < 0) {
        "$this"
    } else {
        var mb = this / 1024.0 / 1024.0
        mb = round(mb * 100) / 100.0
        "${mb} MiB"
    }
}

fun MemoryUsage.toMemoryUsageDTO(): MemoryUsageDTO =
    MemoryUsageDTO(
        committed = committed.bytesToMiB(),
        init = init.bytesToMiB(),
        max = max.bytesToMiB(),
        used = used.bytesToMiB(),
    )

data class MemoryInfoDTO(

    @field:JsonProperty("heap_memory_usage")
    val heapMemoryUsage: MemoryUsageDTO,

    @field:JsonProperty("non_heap_memory_usage")
    val nonHeapMemoryUsage: MemoryUsageDTO,
)

fun buildMemoryInfoDTO(): MemoryInfoDTO {
    val memoryMXBean = ManagementFactory.getMemoryMXBean()
    return MemoryInfoDTO(
        heapMemoryUsage = memoryMXBean.heapMemoryUsage.toMemoryUsageDTO(),
        nonHeapMemoryUsage = memoryMXBean.nonHeapMemoryUsage.toMemoryUsageDTO(),
    )
}

data class OSInfoDTO(

    @JsonProperty("name")
    val name: String,

    @JsonProperty("arch")
    val arch: String,

    @JsonProperty("version")
    val version: String,

    @JsonProperty("available_processors")
    val availableProcessors: Int,

    @JsonProperty("load_average")
    val loadAverage: Double,
)

private fun OperatingSystemMXBean.toOSInfoDTO() = OSInfoDTO(
    name = name,
    arch = arch,
    version = version,
    availableProcessors = availableProcessors,
    loadAverage = systemLoadAverage,
)

data class ThreadDTO(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("state")
    val state: Thread.State,
)

private fun ThreadInfo.toThreadDTO() = ThreadDTO(
    id = threadId,
    name = threadName,
    state = threadState,
)

data class ThreadInfoDTO(

    @JsonProperty("thread_count")
    val threadCount: Int,

    @JsonProperty("peak_thread_count")
    val peakThreadCount: Int,

    @JsonProperty("total_started_thread_count")
    val totalStartedThreadCount: Long,

    @JsonProperty("threads")
    val threads: List<ThreadDTO>,
)

private fun ThreadMXBean.toThreadInfoDTO() =
    ThreadInfoDTO(
        threadCount = threadCount,
        peakThreadCount = peakThreadCount,
        totalStartedThreadCount = totalStartedThreadCount,
        threads = getThreadInfo(allThreadIds)
            .filterNotNull()
            .map { it.toThreadDTO() }
            .sortedBy { it.id }
    )

data class JVMInfoDTO(
    @JsonProperty("gc_info")
    val gcInfo: GCInfoDTO,

    @JsonProperty("memory_info")
    val memoryInfo: MemoryInfoDTO,

    @JsonProperty("os_info")
    val osInfo: OSInfoDTO,

    @JsonProperty("thread_info")
    val threadInfoDTO: ThreadInfoDTO,
)

val jvmInfoDTOLens = Body.auto<JVMInfoDTO>().toLens()

object JVMInfoRoute {
    operator fun invoke() =
        "/jvm_info" bind GET to {

            val jvmInfoDTO = JVMInfoDTO(
                gcInfo = buildGCInfoDTO(),
                memoryInfo = buildMemoryInfoDTO(),
                osInfo = ManagementFactory.getOperatingSystemMXBean().toOSInfoDTO(),
                threadInfoDTO = ManagementFactory.getThreadMXBean().toThreadInfoDTO(),
            )

            Response(OK).with(
                jvmInfoDTOLens of jvmInfoDTO
            )
        }
}
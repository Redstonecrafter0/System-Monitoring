package net.redstonecraft.systemmonitoring

import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val cpu: List<CpuData>,
    val memory: MemoryData,
    val memoryInfo: List<MemoryInfo>,
    val storage: StorageData,
    val nio: List<NetworkData>,
    val gpu: List<GpuData>,
    val os: String,
    val power: List<PowerData>,
    val uptime: Long
)

@Serializable
data class CpuData(
    val core: Int,
    val usage: Double,
    val clock: Long,
    val maxClock: Long,
    val voltage: Double,
    val temp: Double
)

@Serializable
data class MemoryData(
    val available: Long,
    val used: Long,
    val free: Long,
    val percent: Double
)

@Serializable
data class MemoryInfo(
    val available: Long,
    val bank: String,
    val manufacturer: String,
    val type: String,
    val clock: Long
)

@Serializable
data class StorageData(
    val disks: List<DiskData>,
    val openFDs: Long,
    val maxFDs: Long
)

@Serializable
data class DiskData(
    val name: String,
    val read: Long,
    val write: Long,
    val size: Long,
    val partitions: List<PartitionData>
)

@Serializable
data class PartitionData(
    val volume: String,
    val name: String,
    val available: Long,
    val used: Long,
    val free: Long,
    val percent: Double,
    val type: String
)

@Serializable
data class NetworkData(
    val deviceName: String,
    val upload: Long,
    val download: Long,
    val ipv4: List<String>,
    val ipv6: List<String>,
    val mac: String
)

@Serializable
data class GpuData(
    val name: String,
    val memory: Long,
    val info: String
)

@Serializable
data class PowerData(
    val charging: Boolean,
    val plugged: Boolean,
    val capacity: Int,
    val capacityRemaining: Int,
    val chargePercent: Double,
    val health: Double,
    val wattage: Double,
    val cycles: Int,
    val name: String,
    val temp: Double,
    val chem: String,
    val remaining: Long
)

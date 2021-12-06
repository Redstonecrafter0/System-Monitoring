package net.redstonecraft.systemmonitoring

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import oshi.SystemInfo
import oshi.hardware.PowerSource
import java.util.*

object SystemMonitoring {


    val ws = mutableListOf<DefaultWebSocketSession>()

    private val lastReadsDisk = mutableMapOf<String, Pair<Long, Long>>()
    private val lastWritesDisk = mutableMapOf<String, Pair<Long, Long>>()

    private val lastRecvN = mutableMapOf<String, Pair<Long, Long>>()
    private val lastSentN = mutableMapOf<String, Pair<Long, Long>>()

    var prevTicks: Array<LongArray>? = null

    var lastData = generateReport()

    private val timer = Timer()

    private fun generateReport(): Data {
        val info = SystemInfo()
        if (prevTicks == null) {
            prevTicks = info.hardware.processor.processorCpuLoadTicks
        }
        val cpuLoad = info.hardware.processor.getProcessorCpuLoadBetweenTicks(prevTicks)
        prevTicks = info.hardware.processor.processorCpuLoadTicks
        val cpuClock = info.hardware.processor.currentFreq
        return Data(
            cpu = info.hardware.processor.logicalProcessors.map {
                CpuData(
                    core = it.processorNumber,
                    usage = cpuLoad[it.processorNumber],
                    clock = cpuClock[it.processorNumber],
                    maxClock = info.hardware.processor.maxFreq,
                    voltage = info.hardware.sensors.cpuVoltage,
                    temp = info.hardware.sensors.cpuTemperature
                )
            },
            memory = MemoryData(
                available = info.hardware.memory.total,
                used = info.hardware.memory.total - info.hardware.memory.available,
                free = info.hardware.memory.available,
                percent = (info.hardware.memory.total - info.hardware.memory.available) / info.hardware.memory.total.toDouble()
            ),
            memoryInfo = info.hardware.memory.physicalMemory.map {
                MemoryInfo(
                    available = it.capacity,
                    bank = it.bankLabel,
                    manufacturer = it.manufacturer,
                    type = it.memoryType,
                    clock = it.clockSpeed
                )
            },
            storage = StorageData(
                disks = info.hardware.diskStores.map {
                    it.updateAttributes()
                    if (it.serial !in lastReadsDisk) {
                        lastReadsDisk[it.serial] = it.readBytes to it.timeStamp
                    }
                    val lastRead = lastReadsDisk.put(it.serial, it.readBytes to it.timeStamp)!!
                    if (it.serial !in lastWritesDisk) {
                        lastWritesDisk[it.serial] = it.writeBytes to it.timeStamp
                    }
                    val lastWrite = lastWritesDisk.put(it.serial, it.writeBytes to it.timeStamp)!!
                    DiskData(
                        name = it.model,
                        size = it.size,
                        read = try {
                            (it.readBytes - lastRead.first) / (it.timeStamp - lastRead.second)
                        } catch (e: ArithmeticException) {
                            0
                        },
                        write = try {
                            (it.writeBytes - lastRead.first) / (it.timeStamp - lastWrite.second)
                        } catch (e: ArithmeticException) {
                            0
                        },
                        partitions = it.partitions.map { p ->
                            val fs = info.operatingSystem.fileSystem.fileStores.firstOrNull { s -> s.mount == p.mountPoint }
                            PartitionData(
                                volume = fs?.mount ?: "N/A",
                                name = fs?.label ?: "N/A",
                                available = fs?.totalSpace ?: 0,
                                used = (fs?.totalSpace ?: 0) - (fs?.freeSpace ?: 0),
                                free = fs?.freeSpace ?: 0,
                                percent = if (fs == null) .0 else (fs.totalSpace - fs.freeSpace) / fs.totalSpace.toDouble(),
                                type = fs?.type ?: "N/A"
                            )
                        }
                    )
                },
                openFDs = info.operatingSystem.fileSystem.openFileDescriptors,
                maxFDs = info.operatingSystem.fileSystem.maxFileDescriptors
            ),
            nio = info.hardware.networkIFs.map {
                it.updateAttributes()
                if (it.macaddr !in lastRecvN) {
                    lastRecvN[it.macaddr] = it.bytesRecv to it.timeStamp
                }
                val lastRecv = lastRecvN.put(it.macaddr, it.bytesRecv to it.timeStamp)!!
                if (it.macaddr !in lastSentN) {
                    lastSentN[it.macaddr] = it.bytesSent to it.timeStamp
                }
                val lastSent = lastSentN.put(it.macaddr, it.bytesSent to it.timeStamp)!!
                NetworkData(
                    deviceName = it.displayName,
                    download = try {
                        (it.bytesRecv - lastRecv.first) / (it.timeStamp - lastRecv.second)
                    } catch (e: ArithmeticException) {
                        0
                    },
                    upload = try {
                        (it.bytesSent - lastSent.first) / (it.timeStamp - lastSent.second)
                    } catch (e: ArithmeticException) {
                        0
                    },
                    ipv4 = it.iPv4addr.toList(),
                    ipv6 = it.iPv6addr.toList(),
                    mac = it.macaddr
                )
            },
            gpu = info.hardware.graphicsCards.map {
                GpuData(
                    name = it.name,
                    memory = it.vRam,
                    info = "${it.vendor} ${it.deviceId} ${it.versionInfo}"
                )
            },
            os = "${info.operatingSystem.family} ${info.operatingSystem.manufacturer} ${info.operatingSystem.versionInfo.version}${if (info.operatingSystem.versionInfo.codeName == null) " " else " " + info.operatingSystem.versionInfo.codeName + " "}${info.operatingSystem.versionInfo.buildNumber}",
            uptime = info.operatingSystem.systemUptime,
            power = info.hardware.powerSources.map {
                it.updateAttributes()
                PowerData(
                    charging = it.isCharging,
                    plugged = it.isPowerOnLine,
                    capacity = when (it.capacityUnits) {
                        PowerSource.CapacityUnits.MAH -> (it.maxCapacity * it.voltage).toInt()
                        PowerSource.CapacityUnits.MWH -> it.maxCapacity
                        else -> 0
                    },
                    capacityRemaining = when (it.capacityUnits) {
                        PowerSource.CapacityUnits.MAH -> (it.currentCapacity * it.voltage).toInt()
                        PowerSource.CapacityUnits.MWH -> it.currentCapacity
                        else -> 0
                    },
                    chargePercent = it.remainingCapacityPercent,
                    health = it.maxCapacity / it.designCapacity.toDouble(),
                    wattage = it.amperage * it.voltage,
                    cycles = it.cycleCount,
                    name = it.deviceName,
                    temp = it.temperature,
                    chem = it.chemistry,
                    remaining = it.timeRemainingInstant.toLong()
                )
            }
        )
    }

    init {
        timer.schedule(object : TimerTask() {
            override fun run() {
                lastData = generateReport()
                val data = Json.encodeToString(lastData)
                runBlocking {
                    ws.forEach { it.send(data) }
                }
            }
        }, 0, 1000)
    }

}

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.img
import kotlinx.html.js.p
import kotlinx.html.js.span
import kotlinx.html.style
import org.w3c.dom.get
import kotlin.math.roundToInt
import kotlin.math.roundToLong

fun updateCpuInfo(data: CpuData) {
    val element = document.getElementById("cpu-${data.core}")
    if (element != null) {
        element.setAttribute("to", (data.usage).toString())
        val e = element.getElementsByClassName("cpu-rad-progressbar")[0]
        e?.setAttribute("data-core", data.core.toString())
        e?.setAttribute("data-freq", (data.clock / 1000000L).toString())
        e?.setAttribute("data-power", data.wattage.toString())
    } else {
        document.getElementById("cores")!!.append {
            div(classes = "core") {
                style = "--value: ${(data.usage * 100).roundToInt()}"
                id = "cpu-${data.core}"
                div(classes = "cpu-rad-progressbar")
            }
        }
    }
}

fun updateMemory(data: MemoryData) {
    document.getElementById("memory")?.setAttribute("style", "--used: ${data.percent}")
    document.getElementById("memory-max")?.textContent = data.available.formatBytes()
    document.getElementById("memory-current")?.textContent = data.used.formatBytes()
}

fun updateMemoryInfo(data: MemoryInfo, index: Int) {
    val element = document.getElementById("memory-$index")
    if (element == null) {
        document.getElementById("memory-info")?.append {
            div(classes = "memory-info") {
                id = "memory-$index"
                img(src = "img/memory.svg")
                p(classes = "type") {
                    +data.type
                }
                p(classes = "bank") {
                    +data.bank.last().toString()
                }
                p(classes = "memory-cap") {
                    +data.available.formatBytes()
                }
                p(classes = "clock") {
                    +((data.clock / 100000.0).roundToLong() / 10).toString()
                }
                p(classes = "manufacturer") {
                    +data.manufacturer
                }
            }
        }
    }
}

fun updateGpuData(data: GpuData, index: Int) {
    val element = document.getElementById("gpu-$index")
    if (element == null) {
        document.getElementById("gpu")?.append {
            div(classes = "gpu") {
                id = "gpu-$index"
                img(src = "img/gpu.svg")
                span(classes = "gpu-name") {
                    +data.name
                    attributes["data-driver"] = data.driver
                }
                span(classes = "clock") {
                    +(data.clock / 1000000L).toString()
                }
                span(classes = "clock clock-memclock") {
                    +(data.memoryClock / 1000000L).toString()
                }
                span(classes = "temp") {
                    +data.temp.toString()
                }
                span(classes = "load") {
                    span(classes = "bar")
                }
                span(classes = "memory") {
                    span(classes = "bar")
                }
                span(classes = "power") {
                    +data.wattage.toString()
                }
            }
        }
    } else {
        element.getElementsByClassName("clock")[0]?.textContent = (data.clock / 1000000L).toString()
        element.getElementsByClassName("clock")[1]?.textContent = (data.memoryClock / 1000000L).toString()
        element.getElementsByClassName("temp")[0]?.textContent = data.temp.toString()
        element.getElementsByClassName("load")[0]?.setAttribute("style", "--value: ${data.usage / 100}")
        element.getElementsByClassName("memory")[0]?.setAttribute("style", "--value: ${data.memory.percent}")
        element.getElementsByClassName("power")[0]?.textContent = data.wattage.toString()
    }
}

fun updateMedia(data: UIMediaInfo) {
    Media.instance.update(data)
    if (data.on) {
        document.getElementById("uptime")?.setAttribute("style", "top: 60px")
        document.getElementById("grid")?.setAttribute("style", "height: calc(100vh - 60px); top: 60px")
    } else {
        document.getElementById("uptime")?.removeAttribute("style")
        document.getElementById("grid")?.removeAttribute("style")
    }
}

fun updateDisk(data: DiskData, index: Int) {
    val element = document.getElementById("disk_$index")
    if (element == null) {
        document.getElementById("io")?.append {
            div(classes = "drive") {
                id = "disk_$index"
                img(src = "img/hard_drive.svg")
                div(classes = "name") {
                    +data.name
                }
                div(classes = "size") {
                    +data.size.formatBytes()
                }
                div(classes = "partitions") {
                    for (part in data.partitions) {
                        div(classes = "partition") {
                            attributes["style"] = "--used: ${part.used.toDouble() / part.available.toDouble()}"
                            div(classes = "used-percent")
                            div(classes = "text") {
                                +"${part.volume} (${part.name}) ${part.used.formatBytes()}/${part.available.formatBytes()} ${part.type}"
                            }
                        }
                    }
                }
            }
        }
    } else {
        for ((i, part) in data.partitions.withIndex()) {
            element.getElementsByClassName("text")[i]?.textContent = "${part.volume} (${part.name}) ${part.used.formatBytes()}/${part.available.formatBytes()} ${part.type}"
            element.getElementsByClassName("partition")[i]?.setAttribute("style", "--used: ${part.used.toDouble() / part.available.toDouble()}")
        }
    }
}

import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.img
import kotlinx.html.js.p
import kotlinx.html.style
import org.w3c.dom.get
import kotlin.math.roundToInt
import kotlin.math.roundToLong

fun updateCpuInfo(data: CpuData) {
    val element = document.getElementById("cpu-${data.core}")
    if (element != null) {
        element.setAttribute("to", (data.usage).toString())
        element.getElementsByClassName("clock")[0]!!.textContent = (data.clock / 1000000F).toString()
    } else {
        document.getElementById("cores")!!.append {
            div(classes = "core") {
                style = "--value: ${(data.usage * 100).roundToInt()}"
                id = "cpu-${data.core}"
                div {
                    p(classes = "core-display") {
                        +data.core.toString()
                    }
                    div(classes = "rad-progressbar")
                    p(classes = "clock") {
                        +(data.clock / 1000000F).toString()
                    }
                }
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
                img(src = "memory.svg")
                p(classes = "type") {
                    +data.type
                }
                p(classes = "bank") {
                    +data.bank
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

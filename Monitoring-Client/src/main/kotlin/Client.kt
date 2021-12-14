import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.*
import kotlin.math.roundToInt
import kotlin.random.Random

var lastChange = currentTimeMillis()

data class Point(val x: Int, val y: Int, val vx: Int, val vy: Int) {

    fun tick(w: Int, h: Int): Point {
        var x = x
        var vx = vx
        var y = y
        var vy = vy
        if (x !in 0..w) {
            vx *= -1
        }
        if (y !in 0..h) {
            vy *= -1
        }
        x += vx
        y += vy
        return Point(x, y, vx, vy)
    }

}

data class Line(val p1: Point, val p2: Point, val hue: Int) {

    fun tick(w: Int, h: Int): Line = Line(p1.tick(w, h), p2.tick(w, h), hue.let {
        var n = it + 1
        if (n > 360) {
            n = 0
        }
        n
    })

}

fun random(from: Int, to: Int): Int = Random.nextInt(from, to)

class Background(
    private var lastFrame: Long = currentTimeMillis(),
    private val canvas: HTMLCanvasElement,
    private val ctx: CanvasRenderingContext2D = canvas.getContext("2d") as CanvasRenderingContext2D,
    private val lines: MutableList<Line> = mutableListOf(
        Line(
            Point(random(0, window.innerWidth), random(0, window.innerHeight), random(8, 10), random(8, 10)),
            Point(random(0, window.innerWidth), random(0, window.innerHeight), random(-10, -8), random(-10, -8)),
            0
        )
    )
) {

    private fun init() {
        canvas.width = canvas.clientWidth
        canvas.height = canvas.clientHeight
    }

    init {
        canvas.addEventListener("resize") { init() }
        init()
    }

    fun tick() {
        if (currentTimeMillis() - lastFrame > 30) {
            lines += lines.last().tick(canvas.width, canvas.height)

            ctx.clearRect(.0, .0, canvas.width.toDouble(), canvas.height.toDouble())
            lines.forEachIndexed { index, it -> drawLine(it, index / lines.size.toDouble()) }

            if (lines.size > 100) {
                lines.removeFirst()
            }

            lastFrame = currentTimeMillis()
        }
    }

    private fun drawLine(line: Line, alpha: Double) {
        ctx.globalAlpha = alpha
        ctx.strokeStyle = "hsl(${line.hue}, 75%, 50%)"
        ctx.beginPath()
        ctx.moveTo(line.p1.x.toDouble(), line.p1.y.toDouble())
        ctx.lineTo(line.p2.x.toDouble(), line.p2.y.toDouble())
        ctx.stroke()
    }

}

fun main() {
    var ws = WebSocket("ws://localhost:1234/api/ws/data")
    ws.onclose = {
        window.setTimeout({
            try {
                ws = WebSocket("ws://localhost:1234/api/ws/data")
            } catch (e: Throwable) {
            }
        }, 10000)
        null
    }
    var animate: (Double) -> Unit = {}
    val background = Background(canvas = document.getElementById("background") as HTMLCanvasElement)
    animate = { _ ->
        document.getElementsByClassName("core").asList().forEach {
            val delta = (currentTimeMillis() - lastChange) / 2000.0
            it.setAttribute("style", "--value: ${(lerp(delta, ((it.getAttribute("style")?.substring(9)?.toDoubleOrNull() ?: .0)), ((it.getAttribute("to")?.toDoubleOrNull() ?: .0) * 100))).roundToInt()}")
        }
        background.tick()
        window.requestAnimationFrame(animate)
    }
    window.requestAnimationFrame(animate)
    ws.onmessage = {
        val data = Json.decodeFromString<Data>(it.data as String)
        lastChange = currentTimeMillis()
        data.cpu.forEach { updateCpuInfo(it) }
        updateMemory(data.memory)
        data.memoryInfo.forEachIndexed { index, memoryInfo -> updateMemoryInfo(memoryInfo, index) }
        data.gpu.forEachIndexed { index, gpuData -> updateGpuData(gpuData, index) }
        updateMedia(data.media)
        document.getElementById("cpu-temp")?.textContent = ((data.cpu[0].temp * 10).roundToInt() / 10F).toString()

        document.getElementById("os")?.textContent = data.os
        document.getElementById("uptime")?.textContent = data.uptime.formatTime()
        document.getElementById("hide")?.className = "unhide"
        null
    }
}

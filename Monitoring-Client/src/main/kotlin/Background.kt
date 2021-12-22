import kotlinx.browser.window
import kotlinx.html.currentTimeMillis
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.random.Random

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

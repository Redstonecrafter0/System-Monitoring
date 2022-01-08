import kotlinx.browser.window
import kotlinx.html.currentTimeMillis
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

class Overlay(
    private val canvas: HTMLCanvasElement,
    private val ctx: CanvasRenderingContext2D = canvas.getContext("2d") as CanvasRenderingContext2D,
    private val width: Double = window.innerWidth.toDouble(),
    private val height: Double = window.innerHeight.toDouble()
) {

    init {
        canvas.width = width.toInt()
        canvas.height = height.toInt()
    }

    fun tick() {
        val offset = (Long.MAX_VALUE - currentTimeMillis()) / 10 % 360
        ctx.clearRect(.0, .0, width, height)
        val total = width + width + height + height
        val off = offset / 360.0
        drawLinearGradient(.0, .0, .0, height, off, (height / total) + off)
        drawLinearGradient(.0, height, width, height, (height / total) + off, ((height + width) / total) + off)
        drawLinearGradient(width, height, width, .0, ((height + width) / total) + off, ((height + width + height) / total) + off)
        drawLinearGradient(width, .0, .0, .0, ((height + width + height) / total) + off, 1.0 + off)
    }

    private fun drawLinearGradient(x0: Double, y0: Double, x1: Double, y1: Double, from: Double, to: Double) {
        val grad = ctx.createLinearGradient(x0, y0, x1, y1)
        val range = (from * 360).toInt()..(to * 360).toInt()
        val size = range.count()
        for ((i, j) in (range).zip(0..size)) {
            grad.addColorStop(((j / size.toDouble())), "hsl($i, 100%, 50%)")
        }
        ctx.strokeStyle = grad
        ctx.lineWidth = 3.0
        ctx.beginPath()
        ctx.moveTo(x0, y0)
        ctx.lineTo(x1, y1)
        ctx.stroke()
        ctx.closePath()
    }

}

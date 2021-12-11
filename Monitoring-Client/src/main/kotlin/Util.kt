import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun lerp(t: Double, v0: Double, v1: Double): Double {
    return v0 + min(1.0, max(t, .0)) * (v1 - v0)
}

fun Long.signum(): Int {
    return (this shr 63 or (-this ushr 63)).toInt()
}

fun Long.formatBytes(): String {
    if (this < 1024) {
        return "$this B"
    }
    var value = this
    var ci = 0
    var i = 40
    while (i >= 0 && this > 0xfffccccccccccccL shr i) {
        value = value shr 10
        ci++
        i -= 10
    }
    value *= this.signum().toLong()
    return "${(value / 1024.0 * 100).roundToInt() / 100F}${"KMGTPE"[ci]}B"
}

fun Long.formatTime(): String {
    val second: Long = this % 60
    val minute: Long = this / 60 % 60
    val hour: Long = this / (60 * 60) % 24
    val day: Long = this / (60 * 60 * 24)
    return "$day:${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:${second.toString().padStart(2, '0')}"
}

fun HTMLElement.addEventListener(s: String, function: (Event) -> Unit) {
    addEventListener(s, function, null)
}

operator fun Long.plus(string: String): String = this.toString() + string

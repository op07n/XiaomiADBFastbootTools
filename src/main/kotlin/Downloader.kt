import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

class Downloader(val link: String, val target: File) {

    val url = URL(link)
    val size = url.openConnection().contentLengthLong.toFloat()
    val file = File(target, link.substringAfterLast('/'))
    var complete = false
    var startTime = 0L

    fun start() {
        complete = false
        startTime = System.currentTimeMillis()
        FileOutputStream(file).channel.transferFrom(
            Channels.newChannel(url.openStream()),
            0,
            Long.MAX_VALUE
        )
        complete = true
    }

    fun getProgress(): Float = (file.length() / size) * 100f

    fun getSpeed(): Float = file.length() / ((System.currentTimeMillis() - startTime) / 1000.0f)
}
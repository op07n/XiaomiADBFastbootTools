import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

class Downloader(val link: String, val target: File) {

    private val url = URL(link)
    private val size = url.openConnection().contentLengthLong.toFloat()
    private var startTime = 0L
    var complete = false

    fun start() {
        complete = false
        startTime = System.currentTimeMillis()
        FileOutputStream(target).channel.transferFrom(
            Channels.newChannel(url.openStream()),
            0,
            Long.MAX_VALUE
        )
        complete = true
    }

    fun getProgress(): Float = (target.length() / size) * 100f

    fun getSpeed(): Float = target.length() / ((System.currentTimeMillis() - startTime) / 1000.0f)
}
import javafx.application.Platform
import javafx.scene.control.ProgressIndicator
import java.io.File
import java.util.*
import kotlin.concurrent.thread

object Flasher : Command() {

    lateinit var progressIndicator: ProgressIndicator

    fun exec(image: File?, vararg args: String) {
        pb.redirectErrorStream(true)
        progressIndicator.isVisible = true
        outputTextArea.text = ""
        thread(true, true) {
            args.forEach {
                val bits = it.split(' ').toMutableList()
                bits[0] = prefix + bits[0]
                proc = pb.command(bits + image?.absolutePath).start()
                Scanner(proc.inputStream, "UTF-8").useDelimiter("").use { scanner ->
                    while (scanner.hasNext()) {
                        val next = scanner.next()
                        Platform.runLater {
                            outputTextArea.appendText(next)
                        }
                    }
                }
                proc.waitFor()
            }
            Platform.runLater {
                progressIndicator.isVisible = false
            }
        }
    }
}
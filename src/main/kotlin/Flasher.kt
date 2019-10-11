import javafx.application.Platform
import javafx.scene.control.ProgressIndicator
import java.io.File
import java.util.*
import kotlin.concurrent.thread

object Flasher : Command() {

    lateinit var progressIndicator: ProgressIndicator

    fun exec(image: File?, vararg args: String) {
        pb.redirectErrorStream(true)
        var output = ""
        progressIndicator.isVisible = true
        thread(true, true) {
            args.forEach {
                val bits = it.split(' ').toMutableList()
                bits[0] = prefix + bits[0]
                proc = pb.command(bits + image?.absolutePath).start()
                val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                while (scan.hasNext()) {
                    output += scan.next()
                    Platform.runLater {
                        outputTextArea.text = output
                        outputTextArea.appendText("")
                    }
                }
                scan.close()
                proc.waitFor()
            }
            Platform.runLater {
                progressIndicator.isVisible = false
            }
        }
    }
}
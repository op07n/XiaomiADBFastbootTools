import javafx.application.Platform
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import java.io.File
import java.util.*
import kotlin.concurrent.thread

object Flasher {

    lateinit var progressIndicator: ProgressIndicator
    lateinit var outputTextArea: TextInputControl

    fun exec(image: File?, vararg args: String) {
        Command.pb.redirectErrorStream(true)
        var output = ""
        progressIndicator.isVisible = true
        thread(true, true) {
            args.forEach {
                val bits = it.split(' ').toMutableList()
                bits[0] = Command.prefix + bits[0]
                Command.proc = Command.pb.command(bits + image?.absolutePath).start()
                val scan = Scanner(Command.proc.inputStream, "UTF-8").useDelimiter("")
                while (scan.hasNext()) {
                    output += scan.next()
                    Platform.runLater {
                        outputTextArea.text = output
                        outputTextArea.appendText("")
                    }
                }
                scan.close()
                Command.proc.waitFor()
            }
            Platform.runLater {
                progressIndicator.isVisible = false
            }
        }
    }
}
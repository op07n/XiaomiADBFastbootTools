import javafx.application.Platform
import javafx.scene.control.ProgressIndicator
import java.io.File
import java.util.*
import kotlin.concurrent.thread

open class Flasher : Command() {

    companion object {
        lateinit var progressInd: ProgressIndicator

        fun exec(image: File?, vararg args: String) {
            pb.redirectErrorStream(true)
            var output = ""
            progressInd.isVisible = true
            thread(true, true) {
                args.forEach {
                    val bits = it.split(' ').toMutableList()
                    bits[0] = prefix + bits[0]
                    proc = pb.command(bits + image?.absolutePath).start()
                    val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                    while (scan.hasNext()) {
                        output += scan.next()
                        Platform.runLater {
                            tic.text = output
                            tic.appendText("")
                        }
                    }
                    scan.close()
                    proc.waitFor()
                }
                Platform.runLater {
                    progressInd.isVisible = false
                }
            }
        }
    }
}
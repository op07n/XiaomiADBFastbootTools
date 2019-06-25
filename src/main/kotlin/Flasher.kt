import javafx.application.Platform
import javafx.scene.control.ProgressIndicator
import java.io.File
import java.util.*
import kotlin.concurrent.thread

open class Flasher : Command() {

    companion object {
        lateinit var progressInd: ProgressIndicator

        init {
            pb.redirectErrorStream(true)
        }

        fun exec(image: File?, vararg args: String) {
            tic.text = ""
            progressInd.isVisible = true
            thread(true, true) {
                args.forEach {
                    proc = pb.command((prefix + it).split(' ') + image?.absolutePath).start()
                    val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                    while (scan.hasNext())
                        Platform.runLater {
                            tic.appendText(scan.next())
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
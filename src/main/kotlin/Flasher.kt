import javafx.application.Platform
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

open class Flasher: Command() {

    companion object {
        lateinit var tic: TextInputControl
        lateinit var progressInd: ProgressIndicator

        init {
            pb.redirectErrorStream(true)
        }

        fun exec(image: File?, vararg args: String) {
            tic.text = ""
            progressind.isVisible = true
            thread(true, true) {
                args.forEach {
                    pb.command((prefix + it).split(' ') + image?.absolutePath)
                    try {
                        proc = pb.start()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                        ExceptionAlert(ex)
                    }
                    val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                    while (scan.hasNext())
                        Platform.runLater {
                            tic.appendText(scan.next())
                        }
                    scan.close()
                    proc.waitFor()
                }
                Platform.runLater {
                    progressind.isVisible = false
                }
            }
        }
    }
}
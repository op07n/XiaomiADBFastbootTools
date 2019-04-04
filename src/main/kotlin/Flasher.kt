import javafx.application.Platform
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

class Flasher(tic: TextInputControl, var progressind: ProgressIndicator) : Command(tic) {

    init {
        pb.redirectErrorStream(true)
    }

    fun exec(image: File?, arg: String) {
        tic?.text = ""
        val sb = StringBuffer("")
        val arguments = arg.split(" ").toTypedArray()
        arguments[0] = prefix + arguments[0]
        pb.command(*arguments)
        pb.command().add(image?.absolutePath)
        try {
            proc = pb.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
            ExceptionAlert(ex)
        }
        val scan = Scanner(proc.inputStream).useDelimiter("")
        progressind.isVisible = true
        thread(true, true) {
            while (scan.hasNext()) {
                sb.append(scan.next())
                val line = sb.toString()
                Platform.runLater {
                    tic?.text = line
                }
            }
            scan.close()
            Platform.runLater {
                progressind.isVisible = false
            }
        }
    }

    fun exec(image: File?, vararg args: String) {
        tic?.text = ""
        val sb = StringBuffer("")
        progressind.isVisible = true
        thread(true, true) {
            for (s in args) {
                val arguments = s.split(" ").toTypedArray()
                arguments[0] = prefix + arguments[0]
                pb.command(*arguments)
                pb.command().add(image?.absolutePath)
                try {
                    proc = pb.start()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    ExceptionAlert(ex)
                }
                val scan = Scanner(proc.inputStream).useDelimiter("")
                while (scan.hasNext()) {
                    sb.append(scan.next())
                    val line = sb.toString()
                    Platform.runLater {
                        tic?.text = line
                    }
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
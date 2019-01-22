import javafx.application.Platform
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import java.io.File
import java.io.IOException
import java.util.*

class Flasher(tic: TextInputControl, var progressind: ProgressIndicator) : Command(tic) {

    lateinit var t: Thread

    init {
        pb.redirectErrorStream(true)
    }

    fun exec(image: File?, arg: String) {
        tic?.text = ""
        progressind.isVisible = true
        t = Thread {
            arguments = arg.split(" ").toTypedArray()
            arguments[0] = prefix + arguments[0]
            pb.command(*arguments)
            pb.command().add(image?.absolutePath)
            try {
                proc = pb.start()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            scan = Scanner(proc.inputStream)
            while (scan.hasNext()) {
                val line = scan.nextLine() + System.lineSeparator()
                Platform.runLater {
                    tic?.appendText(line)
                }
            }
            scan.close()
            Platform.runLater {
                progressind.isVisible = false
            }
        }
        t.isDaemon = true
        t.start()
    }

    fun exec(image: File?, vararg args: String) {
        tic?.text = ""
        progressind.isVisible = true
        t = Thread {
            for (s in args) {
                arguments = s.split(" ").toTypedArray()
                arguments[0] = prefix + arguments[0]
                pb.command(*arguments)
                pb.command().add(image?.absolutePath)
                try {
                    proc = pb.start()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                scan = Scanner(proc.inputStream)
                while (scan.hasNext()) {
                    val line = scan.nextLine() + System.lineSeparator()
                    Platform.runLater {
                        tic?.appendText(line)
                    }
                }
                scan.close()
                proc.waitFor()
            }
            Platform.runLater {
                progressind.isVisible = false
            }
        }
        t.isDaemon = true
        t.start()
    }
}
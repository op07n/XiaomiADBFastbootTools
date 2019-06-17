import javafx.application.Platform
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

class ROMFlasher: Flasher() {

    companion object {
        lateinit var progress: ProgressBar
        lateinit var directory: File

        init {
            pb.directory(directory)
            pb.redirectErrorStream(true)
        }

        private fun getCmdCount(file: File): Int = file.readText().split("fastboot").size - 1

        private fun createScript(arg: String) {
            val script = File(directory, "script.${arg.substringAfter('.')}")
            try {
                script.writeText(File(directory, arg).readText().replace("fastboot", "${prefix}fastboot"))
            } catch (ex: IOException) {
                ex.printStackTrace()
                ExceptionAlert(ex)
            }
            script.setExecutable(true, false)
        }

        fun exec(arg: String) {
            tic.text = ""
            val sb = StringBuffer("")
            val script: File
            if ("win" in System.getProperty("os.name").toLowerCase()) {
                script = File(directory, "$arg.bat")
                pb.command("cmd.exe", "/c", script.absolutePath)
            } else {
                createScript(arg)
                script = File(directory, "script.sh")
                pb.command("sh", "-c", script.absolutePath)
            }
            val n = getCmdCount(script)
            try {
                proc = pb.start()
            } catch (ex: IOException) {
                ex.printStackTrace()
                ExceptionAlert(ex)
            }
            scan = Scanner(proc.inputStream).useDelimiter("")
            progress.progress = 0.0
            progressind.isVisible = true
            thread(true, true) {
                while (scan.hasNext()) {
                    sb.append(scan.next())
                    val line = sb.toString()
                    if ("pause" in line)
                        break
                    Platform.runLater {
                        tic.text = line
                        tic.appendText("")
                        progress.progress = 1.0 * (line.toLowerCase().split("finished.").size - 1) / n
                    }
                }
                scan.close()
                Platform.runLater {
                    tic.appendText("\nDone!")
                    progress.progress = 0.0
                    progressind.isVisible = false
                }
                if ("script" in script.name)
                    script.delete()
            }
        }
    }
}

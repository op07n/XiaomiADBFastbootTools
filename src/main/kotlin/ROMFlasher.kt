import javafx.application.Platform
import javafx.scene.control.ProgressBar
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

class ROMFlasher : Flasher() {

    companion object {
        lateinit var progress: ProgressBar
        var directory: File? = null

        private fun getCmdCount(file: File): Int = file.readText().split("fastboot").size - 1

        private fun createScript(arg: String): File {
            val script = File(directory, "script.${arg.substringAfter('.')}")
            try {
                script.writeText(File(directory, arg).readText().replace("fastboot", "${prefix}fastboot"))
            } catch (ex: IOException) {
                ex.printStackTrace()
                ExceptionAlert(ex)
            }
            script.setExecutable(true, false)
            return script
        }

        fun exec(arg: String) {
            pb.directory(directory)
            pb.redirectErrorStream(true)
            output = ""
            progress.progress = 0.0
            progressInd.isVisible = true
            thread(true) {
                val script: File
                if ("win" in System.getProperty("os.name").toLowerCase()) {
                    script = createScript("$arg.bat")
                    pb.command("cmd.exe", "/c", script.absolutePath)
                } else {
                    script = createScript("$arg.sh")
                    pb.command("sh", "-c", script.absolutePath)
                }
                val n = getCmdCount(script)
                proc = pb.start()
                val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                while (scan.hasNext()) {
                    output += scan.next()
                    if ("pause" in output)
                        break
                    Platform.runLater {
                        tic.text = output
                        tic.appendText("")
                        progress.progress = 1.0 * (output.toLowerCase().split("finished.").size - 1) / n
                    }
                }
                scan.close()
                Platform.runLater {
                    tic.appendText("\nDone!")
                    progress.progress = 0.0
                    progressInd.isVisible = false
                }
                if ("script" in script.name)
                    script.delete()
            }
        }
    }
}

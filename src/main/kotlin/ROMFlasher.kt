import javafx.application.Platform
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

object ROMFlasher : Command() {
    var directory: File? = null
    lateinit var progressBar: ProgressBar
    lateinit var progressIndicator: ProgressIndicator

    private fun File.getCmdCount(): Int = this.readText().split("fastboot").size - 1

    private fun createScript(arg: String): File {
        return File(directory, "script.${arg.substringAfter('.')}").apply {
            try {
                writeText(File(directory, arg).readText().replace("fastboot", "${prefix}fastboot"))
            } catch (ex: IOException) {
                ex.printStackTrace()
                ExceptionAlert(ex)
            }
            setExecutable(true, false)
        }
    }

    fun exec(arg: String?) {
        if (arg == null)
            return
        pb.redirectErrorStream(true)
        val sb = StringBuilder()
        progressBar.progress = 0.0
        progressIndicator.isVisible = true
        thread(true, true) {
            val script: File
            if (MainController.win) {
                script = createScript("$arg.bat")
                pb.command("cmd.exe", "/c", script.absolutePath)
            } else {
                script = createScript("$arg.sh")
                pb.command("sh", "-c", script.absolutePath)
            }
            val n = script.getCmdCount()
            proc = pb.start()
            Scanner(proc.inputStream, "UTF-8").useDelimiter("").use { scanner ->
                while (scanner.hasNext()) {
                    sb.append(scanner.next())
                    val full = sb.toString()
                    if ("pause" in full)
                        break
                    Platform.runLater {
                        outputTextArea.text = full
                        outputTextArea.appendText("")
                        progressBar.progress = 1.0 * (full.toLowerCase().split("finished.").size - 1) / n
                    }
                }
            }
            Platform.runLater {
                outputTextArea.appendText("\nDone!")
                progressBar.progress = 0.0
                progressIndicator.isVisible = false
            }
            if ("script" in script.name)
                script.delete()
        }
    }
}

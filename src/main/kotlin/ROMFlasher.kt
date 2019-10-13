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

    fun exec(arg: String?) {
        if (arg == null)
            return
        pb.redirectErrorStream(true)
        var output = ""
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
            val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
            while (scan.hasNext()) {
                output += scan.next()
                if ("pause" in output)
                    break
                Platform.runLater {
                    outputTextArea.text = output
                    outputTextArea.appendText("")
                    progressBar.progress = 1.0 * (output.toLowerCase().split("finished.").size - 1) / n
                }
            }
            scan.close()
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

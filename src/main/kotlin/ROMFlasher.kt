import javafx.application.Platform
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

object ROMFlasher {
    var directory: File? = null

    lateinit var progressBar: ProgressBar
    lateinit var progressIndicator: ProgressIndicator
    lateinit var outputTextArea: TextInputControl

    private fun getCmdCount(file: File): Int = file.readText().split("fastboot").size - 1

    private fun createScript(arg: String): File {
        val script = File(directory, "script.${arg.substringAfter('.')}")
        try {
            script.writeText(File(directory, arg).readText().replace("fastboot", "${Command.prefix}fastboot"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            ExceptionAlert(ex)
        }
        script.setExecutable(true, false)
        return script
    }

    fun exec(arg: String) {
        Command.pb.redirectErrorStream(true)
        var output = ""
        progressBar.progress = 0.0
        progressIndicator.isVisible = true
        thread(true, true) {
            val script: File
            if ("win" in System.getProperty("os.name").toLowerCase()) {
                script = createScript("$arg.bat")
                Command.pb.command("cmd.exe", "/c", script.absolutePath)
            } else {
                script = createScript("$arg.sh")
                Command.pb.command("sh", "-c", script.absolutePath)
            }
            val n = getCmdCount(script)
            Command.proc = Command.pb.start()
            val scan = Scanner(Command.proc.inputStream, "UTF-8").useDelimiter("")
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

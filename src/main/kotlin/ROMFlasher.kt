import javafx.application.Platform
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

class ROMFlasher(val directory: File) : Command() {

    private fun File.getCmdCount(): Int = this.readText().split("fastboot").size - 1

    private fun setupScript(arg: String): File {
        val script: File
        if (MainController.win) {
            script = File(directory, "script.bat").apply {
                try {
                    writeText(File(directory, "$arg.bat").readText().replace("fastboot", "${prefix}fastboot"))
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    ExceptionAlert(ex)
                }
                setExecutable(true, false)
            }
            pb.command("cmd.exe", "/c", script.absolutePath)
        } else {
            script = File(directory, "script.sh").apply {
                try {
                    writeText(File(directory, "$arg.sh").readText().replace("fastboot", "${prefix}fastboot"))
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    ExceptionAlert(ex)
                }
                setExecutable(true, false)
            }
            pb.command("sh", "-c", script.absolutePath)
        }
        return script
    }

    fun flash(arg: String?) {
        if (arg == null)
            return
        pb.redirectErrorStream(true)
        val sb = StringBuilder()
        progressBar.progress = 0.0
        progressIndicator.isVisible = true
        thread(true, true) {
            val script = setupScript(arg)
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

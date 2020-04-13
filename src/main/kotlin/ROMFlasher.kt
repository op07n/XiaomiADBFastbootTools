import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

object ROMFlasher {

    var directory = XiaomiADBFastbootTools.dir
    lateinit var progressBar: ProgressBar
    lateinit var outputTextArea: TextInputControl
    lateinit var progressIndicator: ProgressIndicator

    private suspend fun setupScript(arg: String) = withContext(Dispatchers.IO) {
        if (XiaomiADBFastbootTools.win)
            File(directory, "script.bat").apply {
                try {
                    writeText(File(directory, "$arg.bat").readText().replace("fastboot", "${Command.prefix}fastboot"))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    ex.alert()
                }
                setExecutable(true, false)
            } else
            File(directory, "script.sh").apply {
                try {
                    writeText(File(directory, "$arg.sh").readText().replace("fastboot", "${Command.prefix}fastboot"))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    ex.alert()
                }
                setExecutable(true, false)
            }
    }

    suspend fun flash(arg: String?) {
        if (arg == null)
            return
        withContext(Dispatchers.Main) {
            progressBar.progress = 0.0
            progressIndicator.isVisible = true
            outputTextArea.text = ""
        }
        withContext(Dispatchers.IO) {
            val script = setupScript(arg)
            Scanner(runScript(script, redirectErrorStream = true).inputStream, "UTF-8").useDelimiter("")
                .use { scanner ->
                    val sb = StringBuilder()
                    var full: String
                    val n = script.readText().split("fastboot").size - 1
                    while (scanner.hasNext()) {
                        val next = scanner.next()
                        sb.append(next)
                        full = sb.toString()
                        if ("pause" in full)
                            break
                        withContext(Dispatchers.Main) {
                            outputTextArea.appendText(next)
                            progressBar.progress = 1.0 * (full.toLowerCase().split("finished.").size - 1) / n
                        }
                    }
                }
            script.delete()
        }
        withContext(Dispatchers.Main) {
            outputTextArea.appendText("\nDone!")
            progressBar.progress = 0.0
            progressIndicator.isVisible = false
        }
    }
}

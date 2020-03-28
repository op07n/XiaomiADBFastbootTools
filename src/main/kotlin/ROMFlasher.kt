import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class ROMFlasher(private val directory: File) : Command() {

    private suspend fun setupScript(arg: String): File {
        val script: File
        if (MainController.win) {
            script = File(directory, "script.bat").apply {
                try {
                    writeText(File(directory, "$arg.bat").readText().replace("fastboot", "${prefix}fastboot"))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    ex.alert()
                }
                setExecutable(true, false)
            }
            pb.command("cmd.exe", "/c", script.absolutePath)
        } else {
            script = File(directory, "script.sh").apply {
                try {
                    writeText(File(directory, "$arg.sh").readText().replace("fastboot", "${prefix}fastboot"))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    ex.alert()
                }
                setExecutable(true, false)
            }
            pb.command("sh", "-c", script.absolutePath)
        }
        return script
    }

    suspend fun flash(arg: String?) {
        if (arg == null)
            return
        pb.redirectErrorStream(true)
        val sb = StringBuilder()
        withContext(Dispatchers.Main) {
            progressBar.progress = 0.0
            progressIndicator.isVisible = true
        }
        val script = setupScript(arg)
        val n = script.getCmdCount()
        proc = pb.start()
        Scanner(proc.inputStream, "UTF-8").useDelimiter("").use { scanner ->
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine() + '\n')
                val full = sb.toString()
                if ("pause" in full)
                    break
                withContext(Dispatchers.Main) {
                    outputTextArea.text = full
                    outputTextArea.appendText("")
                    progressBar.progress = 1.0 * (full.toLowerCase().split("finished.").size - 1) / n
                }
            }
        }
        withContext(Dispatchers.Main) {
            outputTextArea.appendText("\nDone!")
            progressBar.progress = 0.0
            progressIndicator.isVisible = false
        }
        if ("script" in script.name)
            script.delete()
    }
}

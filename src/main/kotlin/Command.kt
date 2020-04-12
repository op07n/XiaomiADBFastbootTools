import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

object Command {

    var prefix = ""
    lateinit var outputTextArea: TextInputControl
    lateinit var progressIndicator: ProgressIndicator

    private suspend fun setup(pref: String) {
        prefix = pref
        withContext(Dispatchers.IO) {
            startProcess("${prefix}adb", "--version")
            startProcess("${prefix}fastboot", "--version")
            startProcess("${prefix}adb", "start-server")
        }
    }

    suspend fun check(printErr: Boolean = false): Boolean {
        try {
            setup("")
        } catch (e: Exception) {
            try {
                if (XiaomiADBFastbootTools.win)
                    setup("${XiaomiADBFastbootTools.dir.absolutePath}\\platform-tools\\")
                else setup("${XiaomiADBFastbootTools.dir.absolutePath}/platform-tools/")
            } catch (ex: Exception) {
                if (printErr)
                    ex.printStackTrace()
                return false
            }
        }
        return true
    }

    suspend fun exec(vararg args: MutableList<String>, redirectErrorStream: Boolean = true): String {
        val sb = StringBuilder()
        args.forEach {
            it[0] = prefix + it[0]
            withContext(Dispatchers.IO) {
                Scanner(startProcess(it, redirectErrorStream).inputStream, "UTF-8").useDelimiter("").use { scanner ->
                    while (scanner.hasNextLine())
                        sb.append(scanner.nextLine() + '\n')
                }
            }
        }
        return sb.toString()
    }

    suspend fun exec(vararg args: MutableList<String>, image: File?) {
        withContext(Dispatchers.Main) {
            progressIndicator.isVisible = true
            outputTextArea.text = ""
        }
        args.forEach {
            it[0] = prefix + it[0]
            withContext(Dispatchers.IO) {
                Scanner(startProcess(it + image?.absolutePath, true).inputStream, "UTF-8").useDelimiter("")
                    .use { scanner ->
                        while (scanner.hasNextLine())
                            withContext(Dispatchers.Main) {
                                outputTextArea.appendText(scanner.nextLine() + '\n')
                            }
                    }
            }
        }
        withContext(Dispatchers.Main) {
            progressIndicator.isVisible = false
        }
    }

    suspend fun execDisplayed(vararg args: MutableList<String>, redirectErrorStream: Boolean = true): String {
        val sb = StringBuilder()
        withContext(Dispatchers.Main) {
            outputTextArea.text = ""
        }
        args.forEach {
            it[0] = prefix + it[0]
            withContext(Dispatchers.IO) {
                Scanner(startProcess(it, redirectErrorStream).inputStream, "UTF-8").useDelimiter("").use { scanner ->
                    while (scanner.hasNextLine()) {
                        val next = scanner.nextLine() + '\n'
                        sb.append(next)
                        withContext(Dispatchers.Main) {
                            outputTextArea.appendText(next)
                        }
                    }
                }
            }
        }
        return sb.toString()
    }
}
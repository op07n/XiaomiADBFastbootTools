import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

open class Command {

    var pb: ProcessBuilder = ProcessBuilder().directory(MainController.dir)
    lateinit var proc: Process

    companion object {
        var prefix = ""
        lateinit var outputTextArea: TextInputControl
        lateinit var progressBar: ProgressBar
        lateinit var progressIndicator: ProgressIndicator
    }

    private fun setup(pref: String) {
        prefix = pref
        pb.apply {
            command("${prefix}adb", "--version").start()
            command("${prefix}fastboot", "--version").start()
            command("${prefix}adb", "start-server").start()
        }
    }

    fun check(win: Boolean, printErr: Boolean = false): Boolean {
        try {
            setup("")
        } catch (e: Exception) {
            try {
                if (win)
                    setup("${MainController.dir.absolutePath}\\platform-tools\\")
                else setup("${MainController.dir.absolutePath}/platform-tools/")
            } catch (ex: Exception) {
                if (printErr)
                    ex.printStackTrace()
                return false
            }
        }
        return true
    }

    fun exec(vararg args: String, err: Boolean = true, lim: Int = 0): String {
        pb.redirectErrorStream(err)
        val sb = StringBuilder()
        args.forEach {
            val bits = it.split(' ', limit = lim).toMutableList()
            bits[0] = prefix + bits[0]
            proc = pb.command(bits).start()
            Scanner(proc.inputStream, "UTF-8").useDelimiter("").use { scanner ->
                while (scanner.hasNextLine())
                    sb.append(scanner.nextLine() + '\n')
            }
            proc.waitFor()
        }
        return sb.toString()
    }

    suspend fun exec(vararg args: String, image: File?) {
        pb.redirectErrorStream(true)
        withContext(Dispatchers.Main) {
            progressIndicator.isVisible = true
            outputTextArea.text = ""
        }
        args.forEach {
            val bits = it.split(' ').toMutableList()
            bits[0] = prefix + bits[0]
            proc = pb.command(bits + image?.absolutePath).start()
            Scanner(proc.inputStream, "UTF-8").useDelimiter("").use { scanner ->
                while (scanner.hasNextLine()) {
                    val next = scanner.nextLine() + '\n'
                    withContext(Dispatchers.Main) {
                        outputTextArea.appendText(next)
                    }
                }
            }
            proc.waitFor()
        }
        withContext(Dispatchers.Main) {
            progressIndicator.isVisible = false
        }
    }

    suspend fun execDisplayed(vararg args: String, err: Boolean = true, lim: Int = 0): String {
        pb.redirectErrorStream(err)
        val sb = StringBuilder()
        withContext(Dispatchers.Main) {
            outputTextArea.text = ""
        }
        args.forEach {
            val bits = it.split(' ', limit = lim).toMutableList()
            bits[0] = prefix + bits[0]
            proc = pb.command(bits).start()
            Scanner(proc.inputStream, "UTF-8").useDelimiter("").use { scanner ->
                while (scanner.hasNextLine()) {
                    val next = scanner.nextLine() + '\n'
                    sb.append(next)
                    withContext(Dispatchers.Main) {
                        outputTextArea.appendText(next)
                    }
                }
            }
            proc.waitFor()
        }
        return sb.toString()
    }
}
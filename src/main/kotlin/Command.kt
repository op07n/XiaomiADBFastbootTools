import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

open class Command {

    var pb: ProcessBuilder = ProcessBuilder()
    var prefix = ""
    private val workingDirFile = File(MainController.workingDir)
    lateinit var proc: Process

    companion object {
        lateinit var outputTextArea: TextInputControl
        lateinit var progressBar: ProgressBar
        lateinit var progressIndicator: ProgressIndicator
    }

    init {
        pb.directory(workingDirFile)
    }

    private fun setup(pref: String): Boolean {
        prefix = pref
        if (prefix == "" || File(prefix + "adb").exists())
            return try {
                pb.apply {
                    command("${prefix}adb", "--version").start()
                    command("${prefix}fastboot", "--version").start()
                    command("${prefix}adb", "start-server").start()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        return false
    }

    fun check(win: Boolean): Boolean {
        return if (win) {
            setup("${MainController.workingDir}\\bin\\") || setup("${MainController.workingDir}\\") || setup("")
        } else {
            setup("${MainController.workingDir}/bin/") || setup("${MainController.workingDir}/") || setup("")
        }
    }

    fun exec(vararg args: String, err: Boolean = true, lim: Int = 0): String {
        pb.directory(workingDirFile)
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

    fun exec(vararg args: String, image: File?) {
        pb.redirectErrorStream(true)
        progressIndicator.isVisible = true
        outputTextArea.text = ""
        GlobalScope.launch(Dispatchers.IO) {
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
    }

    fun execDisplayed(vararg args: String, err: Boolean = true, lim: Int = 0): String {
        pb.directory(workingDirFile)
        pb.redirectErrorStream(err)
        val sb = StringBuilder()
        outputTextArea.text = ""
        args.forEach {
            val bits = it.split(' ', limit = lim).toMutableList()
            bits[0] = prefix + bits[0]
            proc = pb.command(bits).start()
            Scanner(proc.inputStream, "UTF-8").useDelimiter("").use { scanner ->
                while (scanner.hasNextLine()) {
                    val next = scanner.nextLine() + '\n'
                    sb.append(next)
                    outputTextArea.appendText(next)
                }
            }
            proc.waitFor()
        }
        return sb.toString()
    }
}
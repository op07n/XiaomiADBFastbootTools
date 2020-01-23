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
    lateinit var proc: Process

    companion object {
        var prefix = ""
        val userdir = File(System.getProperty("user.dir"))
        lateinit var outputTextArea: TextInputControl
        lateinit var progressBar: ProgressBar
        lateinit var progressIndicator: ProgressIndicator
    }

    fun setup(pref: String): Boolean {
        pb.directory(userdir)
        prefix = pref
        return try {
            pb.apply {
                command("${prefix}adb", "--version").start()
                command("${prefix}fastboot", "--version").start()
                command("${prefix}adb", "start-server").start()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exec(vararg args: String, err: Boolean = true, lim: Int = 0): String {
        pb.directory(userdir)
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
        pb.directory(userdir)
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
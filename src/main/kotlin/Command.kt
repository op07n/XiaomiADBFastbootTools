import javafx.scene.control.TextInputControl
import java.io.File
import java.util.*

open class Command {

    var pb: ProcessBuilder = ProcessBuilder()
    lateinit var proc: Process
    private val userdir = File(System.getProperty("user.dir"))

    companion object {
        var prefix = ""
        lateinit var outputTextArea: TextInputControl
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

    fun exec(vararg args: String, lim: Int = 0, err: Boolean = true): String {
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

    fun exec_displayed(vararg args: String, lim: Int = 0, err: Boolean = true): String {
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
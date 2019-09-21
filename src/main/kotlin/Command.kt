import javafx.scene.control.TextInputControl
import java.io.File
import java.util.*

object Command {

    var pb: ProcessBuilder = ProcessBuilder()
    var prefix = ""
    lateinit var proc: Process
    lateinit var outputTextArea: TextInputControl
    private val userdir = File(System.getProperty("user.dir"))

    fun setup(pref: String): Boolean {
        pb.directory(userdir)
        prefix = pref
        return try {
            pb.command("${prefix}adb", "--version").start()
            pb.command("${prefix}fastboot", "--version").start()
            pb.command("${prefix}adb", "start-server").start()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exec(vararg args: String, lim: Int = 0, err: Boolean = true): String {
        pb.directory(userdir)
        pb.redirectErrorStream(err)
        var output = ""
        args.forEach {
            val bits = it.split(' ', limit = lim).toMutableList()
            bits[0] = prefix + bits[0]
            proc = pb.command(bits).start()
            val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
            while (scan.hasNextLine())
                output += scan.nextLine() + '\n'
            scan.close()
            proc.waitFor()
        }
        return output
    }

    fun exec_displayed(vararg args: String, lim: Int = 0, err: Boolean = true): String {
        pb.directory(userdir)
        pb.redirectErrorStream(err)
        var output = ""
        outputTextArea.text = ""
        args.forEach {
            val bits = it.split(' ', limit = lim).toMutableList()
            bits[0] = prefix + bits[0]
            proc = pb.command(bits).start()
            val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
            var next: String
            while (scan.hasNextLine()) {
                next = scan.nextLine() + '\n'
                output += next
                outputTextArea.appendText(next)
            }
            scan.close()
            proc.waitFor()
        }
        return output
    }
}
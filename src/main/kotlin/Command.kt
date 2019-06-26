import javafx.scene.control.TextInputControl
import java.io.File
import java.util.*

open class Command {

    companion object {
        var pb: ProcessBuilder = ProcessBuilder()
        var prefix = ""
        var output = ""
        lateinit var tic: TextInputControl
        lateinit var proc: Process
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
            output = ""
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
            output = ""
            tic.text = ""
            args.forEach {
                val bits = it.split(' ', limit = lim).toMutableList()
                bits[0] = prefix + bits[0]
                proc = pb.command(bits).start()
                val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                var next: String
                while (scan.hasNextLine()) {
                    next = scan.nextLine() + '\n'
                    output += next
                    tic.appendText(next)
                }
                scan.close()
                proc.waitFor()
            }
            return output
        }
    }
}
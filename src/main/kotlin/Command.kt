import javafx.scene.control.TextInputControl
import java.io.File
import java.io.IOException
import java.util.*

open class Command {

    //TODO

    companion object {
        var pb: ProcessBuilder = ProcessBuilder()
        var prefix = ""
        lateinit var tic: TextInputControl
        lateinit var proc: Process
        lateinit var output: String

        init {
            pb.directory(File(System.getProperty("user.dir")))
        }

        fun exec(vararg args: String, lim: Int = 0, err: Boolean = true): String {
            pb.redirectErrorStream(err)
            output = ""
            args.forEach {
                pb.command((prefix + it).split(' ', limit = lim))
                try {
                    proc = pb.start()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    ExceptionAlert(ex)
                }
                val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                while (scan.hasNext())
                    output += scan.next()
                scan.close()
                proc.waitFor()
            }
            return output
        }

        fun exec_displayed(vararg args: String, lim: Int = 0, err: Boolean = true): String {
            pb.redirectErrorStream(err)
            output = ""
            tic.text = ""
            args.forEach {
                pb.command((prefix + it).split(' ', limit = lim))
                try {
                    proc = pb.start()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    ExceptionAlert(ex)
                }
                val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                var next: String
                while (scan.hasNext()) {
                    next = scan.next()
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
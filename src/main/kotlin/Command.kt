import javafx.scene.control.TextInputControl
import java.io.File
import java.io.IOException
import java.util.*

open class Command() {

    protected var pb: ProcessBuilder = ProcessBuilder()
    protected var tic: TextInputControl? = null
    protected var prefix: String
    protected lateinit var proc: Process
    protected lateinit var output: String

    init {
        pb.directory(File(System.getProperty("user.home"), "xaft_tmp"))
        if ("win" in System.getProperty("os.name").toLowerCase())
            prefix = System.getProperty("user.home") + "/xaft_tmp/"
        else prefix = "./"
    }

    constructor(control: TextInputControl) : this() {
        tic = control
    }

    fun init(arg: String, lim: Int) {
        val arguments = arg.split(" ", limit = lim).toTypedArray()
        arguments[0] = prefix + arguments[0]
        pb.command(*arguments)
        try {
            proc = pb.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
            ExceptionAlert(ex)
        }
        val scan = Scanner(proc.inputStream)
        var line: String
        while (scan.hasNext()) {
            line = scan.nextLine() + System.lineSeparator()
            if ("fastboot format" in line)
                continue
            output += line
            tic?.appendText(line)
        }
        scan.close()
    }

    fun exec(arg: String, lim: Int = 0): String {
        pb.redirectErrorStream(true)
        output = ""
        tic?.text = ""
        init(arg, lim)
        return output
    }

    fun exec(arg: String, err: Boolean): String {
        val arguments = arg.split(" ").toTypedArray()
        arguments[0] = prefix + arguments[0]
        pb.command(*arguments)
        pb.redirectErrorStream(false)
        output = ""
        tic?.text = ""
        try {
            proc = pb.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
            ExceptionAlert(ex)
        }
        val scan = if (err)
            Scanner(proc.errorStream)
        else Scanner(proc.inputStream)
        var line: String
        while (scan.hasNext()) {
            line = scan.nextLine() + System.lineSeparator()
            output += line
            tic?.appendText(line)
        }
        scan.close()
        return output
    }

    fun exec(vararg args: String, lim: Int = 0): String {
        pb.redirectErrorStream(true)
        output = ""
        tic?.text = ""
        for (s in args) {
            init(s, lim)
            proc.waitFor()
        }
        return output
    }
}

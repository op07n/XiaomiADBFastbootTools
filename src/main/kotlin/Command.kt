import javafx.scene.control.TextInputControl
import java.io.File
import java.io.IOException
import java.util.*

open class Command() {

    protected var pb: ProcessBuilder = ProcessBuilder()
    protected var tic: TextInputControl? = null
    protected var prefix: String
    protected lateinit var proc: Process
    protected lateinit var scan: Scanner
    protected lateinit var output: String
    protected lateinit var arguments: Array<String>

    init {
        pb.directory(File(System.getProperty("user.home") + "/temp"))
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            prefix = System.getProperty("user.home") + "/temp/"
        else prefix = "./"
    }

    constructor(control: TextInputControl) : this() {
        tic = control
    }

    fun init(arg: String) {
        arguments = arg.split(" ").toTypedArray()
        arguments[0] = prefix + arguments[0]
        pb.command(*arguments)
        try {
            proc = pb.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        scan = Scanner(proc.inputStream)
        var line: String
        while (scan.hasNext()) {
            line = scan.nextLine() + System.lineSeparator()
            if (line.contains("fastboot format"))
                continue
            output += line
            tic?.appendText(line)
        }
        scan.close()
    }

    fun exec(arg: String): String {
        pb.redirectErrorStream(true)
        output = ""
        tic?.text = ""
        init(arg)
        return output
    }

    fun exec(arg: String, err: Boolean): String {
        arguments = arg.split(" ").toTypedArray()
        arguments[0] = prefix + arguments[0]
        pb.command(*arguments)
        pb.redirectErrorStream(false)
        output = ""
        tic?.text = ""
        try {
            proc = pb.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        if (err)
            scan = Scanner(proc.errorStream)
        else scan = Scanner(proc.inputStream)
        var line: String
        while (scan.hasNext()) {
            line = scan.nextLine() + System.lineSeparator()
            output += line
            tic?.appendText(line)
        }
        scan.close()
        return output
    }

    fun exec(vararg args: String): String {
        pb.redirectErrorStream(true)
        output = ""
        tic?.text = ""
        for (s in args) {
            init(s)
            proc.waitFor()
        }
        return output
    }
}

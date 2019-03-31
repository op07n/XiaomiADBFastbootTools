import javafx.application.Platform
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextInputControl
import java.io.*
import java.util.*

class ROMFlasher(
    var progress: ProgressBar,
    var progressind: ProgressIndicator,
    var tic: TextInputControl,
    var directory: File
) {

    private val pb: ProcessBuilder = ProcessBuilder()
    private lateinit var proc: Process
    private lateinit var scan: Scanner

    init {
        pb.directory(File(System.getProperty("user.dir") + "/xaft_tmp"))
        pb.redirectErrorStream(true)
    }

    private fun getCmdCount(file: File): Int {
        try {
            scan = Scanner(FileReader(file))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            ExceptionAlert(e)
        }
        var cnt = 0
        while (scan.hasNext())
            if ("fastboot" in scan.nextLine())
                cnt++
        scan.close()
        return cnt
    }

    private fun createScript(arg: String) {
        try {
            scan = Scanner(FileReader(File(directory, "$arg.sh")))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            ExceptionAlert(e)
        }
        var content = ""
        while (scan.hasNext()) {
            content += scan.nextLine().replace("fastboot", "./fastboot") + System.lineSeparator()
        }
        scan.close()
        val script = File(directory, "script.sh")
        try {
            val fw = FileWriter(script)
            fw.write(content)
            fw.flush()
            fw.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
            ExceptionAlert(ex)
        }
        script.setExecutable(true, false)
    }

    fun exec(arg: String) {
        tic.text = ""
        val sb = StringBuffer("")
        val script: File
        if ("win" in System.getProperty("os.name").toLowerCase()) {
            script = File(directory, "$arg.bat")
            pb.command("cmd.exe", "/c", script.absolutePath)
        } else {
            createScript(arg)
            script = File(directory, "script.sh")
            pb.command("sh", "-c", script.absolutePath)
        }
        val n = getCmdCount(script)
        try {
            proc = pb.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
            ExceptionAlert(ex)
        }
        scan = Scanner(proc.inputStream).useDelimiter("")
        progress.progress = 0.0
        progressind.isVisible = true
        val t = Thread {
            while (scan.hasNext()) {
                sb.append(scan.next())
                val line = sb.toString()
                if ("pause" in line)
                    break
                Platform.runLater {
                    tic.text = line
                    tic.appendText("")
                    progress.progress = 1.0 * (line.toLowerCase().split("finished.").size - 1) / n
                }
            }
            scan.close()
            Platform.runLater {
                tic.appendText("\nDone!")
                progress.progress = 0.0
                progressind.isVisible = false
            }
        }
        t.isDaemon = true
        t.start()
    }

}

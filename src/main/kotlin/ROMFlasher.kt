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

    var pb: ProcessBuilder = ProcessBuilder()
    lateinit var proc: Process
    lateinit var scan: Scanner
    lateinit var t: Thread

    init {
        pb.directory(File(System.getProperty("user.home") + "/temp"))
        pb.redirectErrorStream(true)
    }

    private fun getCmdCount(file: File): Int {
        try {
            scan = Scanner(FileReader(file))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        var cnt = 0
        while (scan.hasNext())
            if (scan.nextLine().contains("fastboot"))
                cnt++
        scan.close()
        return cnt
    }

    private fun createScript(arg: String) {
        try {
            scan = Scanner(FileReader(File(directory, "$arg.sh")))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
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
        }
        script.setExecutable(true, false)
    }

    fun exec(arg: String) {
        tic.text = ""
        val script: File
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
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
        }
        scan = Scanner(proc.inputStream)
        progressind.isVisible = true
        t = Thread {
            while (scan.hasNext()) {
                val line = scan.nextLine() + System.lineSeparator()
                if (line.contains("pause"))
                    break
                Platform.runLater {
                    tic.appendText(line)
                    if (line.contains("Finished.") || line.contains("finished."))
                        progress.progress += 1.0 / n
                }
            }
            scan.close()
            Platform.runLater {
                tic.appendText(System.lineSeparator() + "Done!")
                progress.progress = 0.0
                progressind.isVisible = false
            }
        }
        t.isDaemon = true
        t.start()
    }

}

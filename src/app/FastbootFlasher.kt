package app

import javafx.application.Platform
import javafx.scene.control.ProgressBar
import javafx.scene.control.TextInputControl
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.util.*

class FastbootFlasher(var progress: ProgressBar, var tic: TextInputControl, var directory: File) {

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

    fun exec(arg: String) {
        tic.text = ""
        val script: File
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            script = File(directory, "$arg.bat")
            pb.command("cmd.exe", "/c", script.absolutePath)
        } else {
            script = File(directory, "$arg.sh")
            pb.command("sh", "-c", script.absolutePath)
        }
        val n = getCmdCount(script)
        try {
            proc = pb.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        scan = Scanner(proc.inputStream)
        t = Thread {
            while (scan.hasNext()) {
                val line = scan.nextLine() + System.lineSeparator()
                if (line.contains("pause"))
                    break
                Platform.runLater {
                    tic.appendText(line)
                    if (line.contains("fastboot"))
                        progress.progress = progress.progress + (1.0 / n)
                }
            }
            scan.close()
            Platform.runLater {
                tic.appendText(System.lineSeparator() + "Done!")
                progress.progress = 0.0
            }
        }
        t.isDaemon = true
        t.start()
    }

    fun waitFor() {
        try {
            proc.waitFor()
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }

    }

}

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ProgressBar
import javafx.scene.control.TextField
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class FileExplorer(var status: TextField, var progress: ProgressBar) : Command() {

    var path = "/"

    init {
        pb.redirectErrorStream(false)
    }

    fun makeFile(out: String): AndroidFile? {
        val bits = ArrayList<String>()
        for (bit in out.split(' '))
            if (bit.isNotEmpty()) {
                if (bit == "->")
                    break
                bits.add(bit)
            }
        if (bits.size < 8)
            return null
        var name = ""
        var cnt = 7
        while (cnt != bits.size)
            name += "${bits[cnt++]} "
        return AndroidFile(bits[0][0] != '-', name.trim(), bits[4].toInt(), "${bits[5]} ${bits[6]}")
    }

    fun navigate(where: String) {
        if (where == "..") {
            if (path.split('/').size < 3)
                return
            path = path.substring(0, path.lastIndex).substringBeforeLast('/') + "/"
        } else path += "$where/"
    }

    fun getFiles(): ObservableList<AndroidFile> {
        val lines = exec("adb shell ls -l $path", 5)
        val files = FXCollections.observableArrayList<AndroidFile>()
        for (line in lines.split('\n')) {
            if (':' in line && "ls:" !in line) {
                val file = makeFile(line)
                if (file != null)
                    files.add(file)
            }
        }
        return files
    }

    private fun format(pathname: String): String = "'$pathname'"

    private fun init(command: String = "adb") {
        status.text = ""
        try {
            proc = pb.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
            ExceptionAlert(ex)
        }
        val scan = Scanner(proc.inputStream).useDelimiter("")
        while (scan.hasNextLine()) {
            val line = scan.nextLine()
            Platform.runLater {
                if (line.contains('%'))
                    progress.progress = line.substring(line.indexOf('[') + 1, line.indexOf('%')).trim().toInt() / 100.0
                else if (line.contains(command))
                    status.text = "ERROR: ${line.substringAfterLast(':').trim()}"
            }
        }
        scan.close()
        proc.waitFor()
    }

    fun pull(selected: List<AndroidFile>, to: File, func: () -> Unit) {
        thread(true, true) {
            if (selected.isEmpty()) {
                val arguments = arrayOf("${prefix}adb", "pull", path, to.absolutePath)
                pb.command(*arguments)
                init()
            } else {
                for (file in selected) {
                    val arguments = arrayOf("${prefix}adb", "pull", path + file.name, to.absolutePath)
                    pb.command(*arguments)
                    init()
                }
            }
            Platform.runLater {
                if (status.text.isEmpty())
                    status.text = "Done!"
                progress.progress = 0.0
                func()
            }
        }
    }

    fun push(selected: List<File>, func: () -> Unit) {
        thread(true, true) {
            for (file in selected) {
                val arguments = arrayOf("${prefix}adb", "push", file.absolutePath, path)
                pb.command(*arguments)
                init()
            }
            Platform.runLater {
                if (status.text.isEmpty())
                    status.text = "Done!"
                progress.progress = 0.0
                func()
            }
        }
    }

    fun delete(selected: List<AndroidFile>, func: () -> Unit) {
        thread(true, true) {
            for (file in selected) {
                val arguments = if (file.dir)
                    arrayOf("${prefix}adb", "shell", "rm", "-rf", format(path + file.name))
                else arrayOf("${prefix}adb", "shell", "rm", "-f", format(path + file.name))
                pb.command(*arguments)
                init("rm")
            }
            Platform.runLater {
                if (status.text.isEmpty())
                    status.text = "Done!"
                progress.progress = 0.0
                func()
            }
        }
    }

    fun mkdir(name: String, func: () -> Unit) {
        thread(true, true) {
            val arguments = arrayOf("${prefix}adb", "shell", "mkdir", format(path + name))
            pb.command(*arguments)
            init("mkdir")
            Platform.runLater {
                if (status.text.isEmpty())
                    status.text = "Done!"
                progress.progress = 0.0
                func()
            }
        }
    }

    fun rename(selected: AndroidFile, to: String, func: () -> Unit) {
        thread(true, true) {
            val arguments = arrayOf("${prefix}adb", "shell", "mv", format(path + selected.name), format(path + to))
            pb.command(*arguments)
            init("mv")
            Platform.runLater {
                if (status.text.isEmpty())
                    status.text = "Done!"
                progress.progress = 0.0
                func()
            }
        }
    }

}
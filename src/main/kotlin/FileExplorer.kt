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
            path = path.dropLast(1).substringBeforeLast('/') + "/"
        } else path += "$where/"
    }

    fun getFiles(): ObservableList<AndroidFile> {
        val files = FXCollections.observableArrayList<AndroidFile>()
        exec("adb shell ls -l $path", lim = 5).lines().forEach {
            if ("ls:" !in it && ':' in it)
                makeFile(it)?.let { file ->
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
        val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
        while (scan.hasNextLine()) {
            output = scan.nextLine()
            Platform.runLater {
                if ('%' in output)
                    progress.progress = output.substringBefore('%').trim('[', ' ').toInt() / 100.0
                else if (command in output)
                    status.text = "ERROR: ${output.substringAfterLast(':').trim()}"
            }
        }
        scan.close()
        proc.waitFor()
    }

    fun pull(selected: List<AndroidFile>, to: File, func: () -> Unit) {
        thread(true, true) {
            if (selected.isEmpty()) {
                pb.command("${prefix}adb", "pull", path, to.absolutePath)
                init()
            } else {
                selected.forEach {
                    pb.command("${prefix}adb", "pull", path + it.name, to.absolutePath)
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
            selected.forEach {
                pb.command("${prefix}adb", "push", it.absolutePath, path)
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
            selected.forEach {
                if (it.dir)
                    pb.command("${prefix}adb", "shell", "rm", "-rf", format(path + it.name))
                else pb.command("${prefix}adb", "shell", "rm", "-f", format(path + it.name))
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
            pb.command("${prefix}adb", "shell", "mkdir", format(path + name))
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
            pb.command("${prefix}adb", "shell", "mv", format(path + selected.name), format(path + to))
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
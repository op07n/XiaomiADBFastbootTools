import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ProgressBar
import javafx.scene.control.TextField
import java.io.File
import java.util.*
import kotlin.concurrent.thread

object FileExplorer : Command() {

    var path = "/"
    lateinit var statusTextField: TextField
    lateinit var progressBar: ProgressBar

    private fun makeFile(out: String): AndroidFile? {
        val bits = mutableListOf<String>()
        out.split(' ').forEach {
            if (it.isNotBlank()) {
                if (it == "->")
                    return@forEach
                bits.add(it)
            }
        }
        return when {
            bits.size < 6 -> null
            bits[5].length == 10 && bits[6].length == 5 -> AndroidFile(
                bits[0][0] != '-',
                bits.drop(7).joinToString(" ").trim(),
                bits[4].toInt(),
                "${bits[5]} ${bits[6]}"
            )
            bits[4].length == 10 && bits[5].length == 5 -> AndroidFile(
                bits[0][0] != '-',
                bits.drop(6).joinToString(" ").trim(),
                bits[3].toInt(),
                "${bits[4]} ${bits[5]}"
            )
            bits[3].length == 10 && bits[4].length == 5 -> AndroidFile(
                bits[0][0] != '-',
                bits.drop(5).joinToString(" ").trim(),
                0,
                "${bits[3]} ${bits[4]}"
            )
            else -> null
        }

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
        exec("adb shell ls -l $path", lim = 5).trim().lines().forEach {
            if ("ls:" !in it && ':' in it)
                makeFile(it)?.let { file ->
                    files.add(file)
                }
        }
        return files
    }

    fun String.fmt(): String = "'$this'"

    fun init(command: String = "adb") {
        pb.redirectErrorStream(false)
        statusTextField.text = ""
        proc = pb.start()
        val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
        while (scan.hasNextLine()) {
            val output = scan.nextLine()
            Platform.runLater {
                if ('%' in output)
                    progressBar.progress = output.substringBefore('%').trim('[', ' ').toInt() / 100.0
                else if (command in output)
                    statusTextField.text = "ERROR: ${output.substringAfterLast(':').trim()}"
            }
        }
        scan.close()
        proc.waitFor()
    }

    inline fun pull(selected: List<AndroidFile>, to: File, crossinline func: () -> Unit) {
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
                if (statusTextField.text.isEmpty())
                    statusTextField.text = "Done!"
                progressBar.progress = 0.0
                func()
            }
        }
    }

    inline fun push(selected: List<File>, crossinline func: () -> Unit) {
        thread(true, true) {
            selected.forEach {
                pb.command("${prefix}adb", "push", it.absolutePath, path)
                init()
            }
            Platform.runLater {
                if (statusTextField.text.isEmpty())
                    statusTextField.text = "Done!"
                progressBar.progress = 0.0
                func()
            }
        }
    }

    inline fun delete(selected: List<AndroidFile>, crossinline func: () -> Unit) {
        thread(true, true) {
            selected.forEach {
                if (it.dir)
                    pb.command("${prefix}adb", "shell", "rm", "-rf", (path + it.name).fmt())
                else pb.command("${prefix}adb", "shell", "rm", "-f", (path + it.name).fmt())
                init("rm")
            }
            Platform.runLater {
                if (statusTextField.text.isEmpty())
                    statusTextField.text = "Done!"
                progressBar.progress = 0.0
                func()
            }
        }
    }

    inline fun mkdir(name: String, crossinline func: () -> Unit) {
        thread(true, true) {
            pb.command("${prefix}adb", "shell", "mkdir", (path + name).fmt())
            init("mkdir")
            Platform.runLater {
                if (statusTextField.text.isEmpty())
                    statusTextField.text = "Done!"
                progressBar.progress = 0.0
                func()
            }
        }
    }

    inline fun rename(selected: AndroidFile, to: String, crossinline func: () -> Unit) {
        thread(true, true) {
            pb.command("${prefix}adb", "shell", "mv", (path + selected.name).fmt(), (path + to).fmt())
            init("mv")
            Platform.runLater {
                if (statusTextField.text.isEmpty())
                    statusTextField.text = "Done!"
                progressBar.progress = 0.0
                func()
            }
        }
    }

}
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ProgressBar
import javafx.scene.control.TextField
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

object FileExplorer {

    var path = "/"
    lateinit var statusTextField: TextField
    lateinit var progressBar: ProgressBar

    private fun makeFile(out: String): AndroidFile? {
        val bits = ArrayList<String>()
        for (bit in out.split(' '))
            if (bit.isNotBlank()) {
                if (bit == "->")
                    break
                bits.add(bit)
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
        Command.exec("adb shell ls -l $path", lim = 5).trim().lines().forEach {
            if ("ls:" !in it && ':' in it)
                makeFile(it)?.let { file ->
                    files.add(file)
                }
        }
        return files
    }

    private fun format(pathname: String): String = "'$pathname'"

    private fun init(command: String = "adb") {
        Command.pb.redirectErrorStream(false)
        statusTextField.text = ""
        Command.proc = Command.pb.start()
        val scan = Scanner(Command.proc.inputStream, "UTF-8").useDelimiter("")
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
        Command.proc.waitFor()
    }

    fun pull(selected: List<AndroidFile>, to: File, func: () -> Unit) {
        thread(true, true) {
            if (selected.isEmpty()) {
                Command.pb.command("${Command.prefix}adb", "pull", path, to.absolutePath)
                init()
            } else {
                selected.forEach {
                    Command.pb.command("${Command.prefix}adb", "pull", path + it.name, to.absolutePath)
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

    fun push(selected: List<File>, func: () -> Unit) {
        thread(true, true) {
            selected.forEach {
                Command.pb.command("${Command.prefix}adb", "push", it.absolutePath, path)
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

    fun delete(selected: List<AndroidFile>, func: () -> Unit) {
        thread(true, true) {
            selected.forEach {
                if (it.dir)
                    Command.pb.command("${Command.prefix}adb", "shell", "rm", "-rf", format(path + it.name))
                else Command.pb.command("${Command.prefix}adb", "shell", "rm", "-f", format(path + it.name))
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

    fun mkdir(name: String, func: () -> Unit) {
        thread(true, true) {
            Command.pb.command("${Command.prefix}adb", "shell", "mkdir", format(path + name))
            init("mkdir")
            Platform.runLater {
                if (statusTextField.text.isEmpty())
                    statusTextField.text = "Done!"
                progressBar.progress = 0.0
                func()
            }
        }
    }

    fun rename(selected: AndroidFile, to: String, func: () -> Unit) {
        thread(true, true) {
            Command.pb.command("${Command.prefix}adb", "shell", "mv", format(path + selected.name), format(path + to))
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
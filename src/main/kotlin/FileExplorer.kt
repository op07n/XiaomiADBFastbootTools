import Command.prefix
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ProgressBar
import javafx.scene.control.TextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class FileExplorer(val statusTextField: TextField, val statusProgressBar: ProgressBar) {

    var path = "/"

    private fun makeFile(out: String): AndroidFile? {
        val bits = mutableListOf<String>().also {
            for (bit in out.split(' '))
                if (bit.isNotBlank()) {
                    if (bit == "->")
                        break
                    it.add(bit)
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

    suspend fun getFiles(): ObservableList<AndroidFile> =
        FXCollections.observableArrayList<AndroidFile>().also { list ->
            Command.exec(mutableListOf("adb", "shell", "ls", "-l", path)).trim().lines().forEach {
                if ("ls:" !in it && ':' in it)
                    makeFile(it)?.let { file ->
                        list.add(file)
                    }
            }
        }

    suspend fun exec(command: MutableList<String>) {
        withContext(Dispatchers.Main) {
            statusTextField.text = ""
        }
        command[0] = prefix + command[0]
        withContext(Dispatchers.IO) {
            Scanner(startProcess(command).inputStream, "UTF-8").useDelimiter("").use { scanner ->
                while (scanner.hasNextLine()) {
                    val output = scanner.nextLine()
                    withContext(Dispatchers.Main) {
                        if ('%' in output)
                            statusProgressBar.progress = output.substringBefore('%').trim('[', ' ').toInt() / 100.0
                        else if ((command[1] == "shell" && command[2] in output) || "adb" in output)
                            statusTextField.text = "ERROR: ${output.substringAfterLast(':').trim()}"
                    }
                }
            }
        }
    }

    suspend fun pull(selected: List<AndroidFile>, to: File) {
        if (selected.isEmpty()) {
            exec(mutableListOf("adb", "pull", path, to.absolutePath))
        } else {
            selected.forEach {
                exec(mutableListOf("adb", "pull", path + it.name, to.absolutePath))
            }
        }
        withContext(Dispatchers.Main) {
            if (statusTextField.text.isEmpty())
                statusTextField.text = "Done!"
            statusProgressBar.progress = 0.0
        }
    }

    suspend fun push(selected: List<File>) {
        selected.forEach {
            exec(mutableListOf("adb", "push", it.absolutePath, path))
        }
        withContext(Dispatchers.Main) {
            if (statusTextField.text.isEmpty())
                statusTextField.text = "Done!"
            statusProgressBar.progress = 0.0
        }
    }

    suspend fun delete(selected: List<AndroidFile>) {
        selected.forEach {
            if (it.dir)
                exec(mutableListOf("adb", "shell", "rm", "-rf", (path + it.name).escape()))
            else exec(mutableListOf("adb", "shell", "rm", "-f", (path + it.name).escape()))
        }
        withContext(Dispatchers.Main) {
            if (statusTextField.text.isEmpty())
                statusTextField.text = "Done!"
            statusProgressBar.progress = 0.0
        }
    }

    suspend fun mkdir(name: String) {
        exec(mutableListOf("adb", "shell", "mkdir", (path + name).escape()))
        withContext(Dispatchers.Main) {
            if (statusTextField.text.isEmpty())
                statusTextField.text = "Done!"
            statusProgressBar.progress = 0.0
        }
    }

    suspend fun rename(selected: AndroidFile, to: String) {
        exec(mutableListOf("adb", "shell", "mv", (path + selected.name).escape(), (path + to).escape()))
        withContext(Dispatchers.Main) {
            if (statusTextField.text.isEmpty())
                statusTextField.text = "Done!"
            statusProgressBar.progress = 0.0
        }
    }

}
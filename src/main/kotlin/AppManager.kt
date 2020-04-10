import Command.prefix
import javafx.collections.ObservableList
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableView
import javafx.scene.control.TextInputControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.util.*


object AppManager {

    lateinit var uninstallerTableView: TableView<App>
    lateinit var reinstallerTableView: TableView<App>
    lateinit var disablerTableView: TableView<App>
    lateinit var enablerTableView: TableView<App>
    lateinit var progress: ProgressBar
    lateinit var progressInd: ProgressIndicator
    lateinit var outputTextArea: TextInputControl
    var user = "0"
    val customApps = File(XiaomiADBFastbootTools.dir, "apps.yml")
    private val potentialApps = mutableMapOf<String, String>()

    init {
        if (!customApps.exists())
            customApps.createNewFile()
    }

    suspend fun readPotentialApps() {
        potentialApps.clear()
        potentialApps["android.autoinstalls.config.Xiaomi.${Device.codename}"] = "PAI"
        withContext(Dispatchers.IO) {
            try {
                URL("https://raw.githubusercontent.com/Szaki/XiaomiADBFastbootTools/master/src/main/resources/apps.yml").readText()
                    .trim().lines()
            } catch (ex: Exception) {
                this::class.java.classLoader.getResource("apps.yml")?.readText()?.trim()?.lines()
            }?.forEach { line ->
                val app = line.split(':')
                potentialApps[app[0].trim()] = app[1].trim()
            }
        }
        customApps.forEachLine { line ->
            val app = line.split(':')
            if (app.size == 1) {
                potentialApps[app[0].trim()] = app[0].trim()
            } else {
                potentialApps[app[0].trim()] = app[1].trim()
            }
        }
    }

    suspend fun createTables() {
        val uninstallApps = mutableMapOf<String, MutableList<String>>()
        val reinstallApps = mutableMapOf<String, MutableList<String>>()
        val disableApps = mutableMapOf<String, MutableList<String>>()
        val enableApps = mutableMapOf<String, MutableList<String>>()
        val deviceApps = mutableMapOf<String, String>()
        Command.exec(mutableListOf("adb", "shell", "pm", "list", "packages", "-u", "--user", user)).trim().lines()
            .forEach {
                deviceApps[it.substringAfter(':').trim()] = "uninstalled"
            }
        Command.exec(mutableListOf("adb", "shell", "pm", "list", "packages", "-d", "--user", user)).trim().lines()
            .forEach {
                deviceApps[it.substringAfter(':').trim()] = "disabled"
            }
        Command.exec(mutableListOf("adb", "shell", "pm", "list", "packages", "-e", "--user", user)).trim().lines()
            .forEach {
                deviceApps[it.substringAfter(':').trim()] = "enabled"
            }
        potentialApps.forEach { (pkg, name) ->
            when (deviceApps[pkg]) {
                "disabled" -> {
                    uninstallApps.add(name, pkg)
                    enableApps.add(name, pkg)
                }
                "enabled" -> {
                    uninstallApps.add(name, pkg)
                    disableApps.add(name, pkg)
                }
                "uninstalled" -> reinstallApps.add(name, pkg)
            }
        }
        withContext(Dispatchers.Main) {
            uninstallerTableView.items.setAll(uninstallApps.toSortedMap().map { App(it.key, it.value) })
            reinstallerTableView.items.setAll(reinstallApps.toSortedMap().map { App(it.key, it.value) })
            disablerTableView.items.setAll(disableApps.toSortedMap().map { App(it.key, it.value) })
            enablerTableView.items.setAll(enableApps.toSortedMap().map { App(it.key, it.value) })
            uninstallerTableView.refresh()
            reinstallerTableView.refresh()
            disablerTableView.refresh()
            enablerTableView.refresh()
        }
    }

    suspend fun uninstall(selected: ObservableList<App>, n: Int) {
        withContext(Dispatchers.Main) {
            outputTextArea.text = ""
            progress.progress = 0.0
            progressInd.isVisible = true
        }
        selected.forEach {
            it.packagenameProperty().get().trim().lines().forEach { pkg ->
                val sb = StringBuilder()
                withContext(Dispatchers.IO) {
                    Scanner(
                        startProcess(
                            mutableListOf(
                                "${prefix}adb",
                                "shell",
                                "pm",
                                "uninstall",
                                "--user",
                                user,
                                pkg.trim()
                            )
                        ).inputStream, "UTF-8"
                    ).useDelimiter("").use { scanner ->
                        while (scanner.hasNextLine())
                            sb.append(scanner.nextLine() + '\n')
                    }
                }
                withContext(Dispatchers.Main) {
                    outputTextArea.apply {
                        appendText("App: ${it.appnameProperty().get()}\n")
                        appendText("Package: $pkg\n")
                        appendText("Result: $sb\n")
                    }
                    progress.progress += 1.0 / n
                }
            }
        }
        withContext(Dispatchers.Main) {
            outputTextArea.appendText("Done!")
            progress.progress = 0.0
            progressInd.isVisible = false
            createTables()
        }
    }

    suspend fun reinstall(selected: ObservableList<App>, n: Int) {
        withContext(Dispatchers.Main) {
            outputTextArea.text = ""
            progress.progress = 0.0
            progressInd.isVisible = true
        }
        selected.forEach {
            it.packagenameProperty().get().trim().lines().forEach { pkg ->
                val sb = StringBuilder()
                withContext(Dispatchers.IO) {
                    Scanner(
                        startProcess(
                            mutableListOf(
                                "${prefix}adb",
                                "shell",
                                "cmd",
                                "package",
                                "install-existing",
                                "--user",
                                user,
                                pkg.trim()
                            )
                        ).inputStream, "UTF-8"
                    ).useDelimiter("").use { scanner ->
                        while (scanner.hasNextLine())
                            sb.append(scanner.nextLine() + '\n')
                    }
                }
                var output = sb.toString()
                output = if ("installed for user" in output)
                    "Success\n"
                else "Failure [${output.substringAfter(pkg).trim()}]\n"
                withContext(Dispatchers.Main) {
                    outputTextArea.apply {
                        appendText("App: ${it.appnameProperty().get()}\n")
                        appendText("Package: $pkg\n")
                        appendText("Result: $output\n")
                    }
                    progress.progress += 1.0 / n
                }
            }
        }
        withContext(Dispatchers.Main) {
            outputTextArea.appendText("Done!")
            progress.progress = 0.0
            progressInd.isVisible = false
            createTables()
        }
    }

    suspend fun disable(selected: ObservableList<App>, n: Int) {
        withContext(Dispatchers.Main) {
            outputTextArea.text = ""
            progress.progress = 0.0
            progressInd.isVisible = true
        }
        selected.forEach {
            it.packagenameProperty().get().trim().lines().forEach { pkg ->
                val sb = StringBuilder()
                withContext(Dispatchers.IO) {
                    Scanner(
                        startProcess(
                            mutableListOf(
                                "${prefix}adb",
                                "shell",
                                "pm",
                                "disable-user",
                                "--user",
                                user,
                                pkg.trim()
                            )
                        ).inputStream, "UTF-8"
                    ).useDelimiter("").use { scanner ->
                        while (scanner.hasNextLine())
                            sb.append(scanner.nextLine() + '\n')
                    }
                }
                val output = if ("disabled-user" in sb.toString())
                    "Success\n"
                else "Failure\n"
                withContext(Dispatchers.Main) {
                    outputTextArea.apply {
                        appendText("App: ${it.appnameProperty().get()}\n")
                        appendText("Package: $pkg\n")
                        appendText("Result: $output\n")
                    }
                    progress.progress += 1.0 / n
                }
            }
        }
        withContext(Dispatchers.Main) {
            outputTextArea.appendText("Done!")
            progress.progress = 0.0
            progressInd.isVisible = false
            createTables()
        }
    }

    suspend fun enable(selected: ObservableList<App>, n: Int) {
        withContext(Dispatchers.Main) {
            outputTextArea.text = ""
            progress.progress = 0.0
            progressInd.isVisible = true
        }
        selected.forEach {
            it.packagenameProperty().get().trim().lines().forEach { pkg ->
                val sb = StringBuilder()
                withContext(Dispatchers.IO) {
                    Scanner(
                        startProcess(mutableListOf("${prefix}adb", "shell", "pm", "enable", "--user", user, pkg.trim()))
                            .inputStream, "UTF-8"
                    ).useDelimiter("").use { scanner ->
                        while (scanner.hasNextLine())
                            sb.append(scanner.nextLine() + '\n')
                    }
                }
                val output = if ("enabled" in sb.toString())
                    "Success\n"
                else "Failure\n"
                withContext(Dispatchers.Main) {
                    outputTextArea.apply {
                        appendText("App: ${it.appnameProperty().get()}\n")
                        appendText("Package: $pkg\n")
                        appendText("Result: $output\n")
                    }
                    progress.progress += 1.0 / n
                }
            }
        }
        withContext(Dispatchers.Main) {
            outputTextArea.appendText("Done!")
            progress.progress = 0.0
            progressInd.isVisible = false
            createTables()
        }
    }
}
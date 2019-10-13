import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableView
import java.io.File
import java.util.*
import kotlin.concurrent.thread


object AppManager : Command() {

    lateinit var uninstallerTableView: TableView<App>
    lateinit var reinstallerTableView: TableView<App>
    lateinit var disablerTableView: TableView<App>
    lateinit var enablerTableView: TableView<App>
    lateinit var progress: ProgressBar
    lateinit var progressInd: ProgressIndicator
    var user = 0
    val appsFile = File(System.getProperty("user.home"), "XiaomiADBFastbootTools" + File.pathSeparator + "apps.yml")
    private var potentialApps = mutableMapOf<String, String>()

    init {
        readPotentialApps()
    }

    private fun MutableMap<String, MutableList<String>>.add(key: String, value: String) {
        if (this[key] == null) {
            this[key] = mutableListOf(value)
        } else this[key]!!.add(value)
    }

    fun readPotentialApps() {
        potentialApps.clear()
        potentialApps["android.autoinstalls.config.Xiaomi.${Device.codename}"] = "PAI"
        this::class.java.classLoader.getResource("apps.yml")!!.readText().trim().lines().forEach { line ->
            val app = line.split(':')
            potentialApps[app[0].trim()] = app[1].trim()
        }
        if (appsFile.exists()) {
            appsFile.readText().trim().lines().forEach { line ->
                val app = line.split(':')
                if (app.size == 1) {
                    potentialApps[app[0].trim()] = app[0].trim()
                } else {
                    potentialApps[app[0].trim()] = app[1].trim()
                }
            }
        }
    }

    fun createTables() {
        uninstallerTableView.items.clear()
        reinstallerTableView.items.clear()
        disablerTableView.items.clear()
        enablerTableView.items.clear()
        val uninstallApps = mutableMapOf<String, MutableList<String>>()
        val reinstallApps = mutableMapOf<String, MutableList<String>>()
        val disableApps = mutableMapOf<String, MutableList<String>>()
        val enableApps = mutableMapOf<String, MutableList<String>>()
        val deviceApps = mutableMapOf<String, String>()
        exec("adb shell pm list packages -u --user $user").trim().lines().forEach {
            deviceApps[it.substringAfter(':')] = "uninstalled"
        }
        exec("adb shell pm list packages -d --user $user").trim().lines().forEach {
            deviceApps[it.substringAfter(':')] = "disabled"
        }
        exec("adb shell pm list packages -e --user $user").trim().lines().forEach {
            deviceApps[it.substringAfter(':')] = "enabled"
        }
        potentialApps.forEach { (pkg, name) ->
            when (deviceApps[name]) {
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
        uninstallerTableView.items.addAll(uninstallApps.map { App(it.key, it.value) })
        reinstallerTableView.items.addAll(reinstallApps.map { App(it.key, it.value) })
        disablerTableView.items.addAll(disableApps.map { App(it.key, it.value) })
        enablerTableView.items.addAll(enableApps.map { App(it.key, it.value) })
        uninstallerTableView.refresh()
        reinstallerTableView.refresh()
        disablerTableView.refresh()
        enablerTableView.refresh()
    }

    inline fun uninstall(selected: ObservableList<App>, n: Int, crossinline func: () -> Unit) {
        pb.redirectErrorStream(false)
        outputTextArea.text = ""
        progress.progress = 0.0
        progressInd.isVisible = true
        thread(true, true) {
            selected.forEach {
                it.packagenameProperty().get().trim().lines().forEach { pkg ->
                    proc =
                        pb.command("${prefix}adb", "shell", "pm", "uninstall", "--user", "$user", pkg)
                            .start()
                    val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                    var output = ""
                    while (scan.hasNextLine())
                        output += scan.nextLine() + '\n'
                    scan.close()
                    Platform.runLater {
                        outputTextArea.appendText("App: ${it.appnameProperty().get()}\n")
                        outputTextArea.appendText("Package: $pkg\n")
                        outputTextArea.appendText("Result: $output\n")
                        progress.progress += 1.0 / n
                    }
                }
            }
            Platform.runLater {
                outputTextArea.appendText("Done!")
                progress.progress = 0.0
                progressInd.isVisible = false
                createTables()
                func()
            }
        }
    }

    inline fun reinstall(selected: ObservableList<App>, n: Int, crossinline func: () -> Unit) {
        pb.redirectErrorStream(false)
        outputTextArea.text = ""
        progress.progress = 0.0
        progressInd.isVisible = true
        thread(true, true) {
            selected.forEach {
                it.packagenameProperty().get().trim().lines().forEach { pkg ->
                    proc =
                        pb.command(
                            "${prefix}adb",
                            "shell",
                            "cmd",
                            "package",
                            "install-existing",
                            "--user",
                            "$user",
                            pkg
                        )
                            .start()
                    val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                    var output = ""
                    while (scan.hasNextLine())
                        output += scan.nextLine() + '\n'
                    scan.close()
                    output = if ("installed for user" in output)
                        "Success\n"
                    else "Failure [${output.substringAfter(pkg).trim()}]\n"
                    Platform.runLater {
                        outputTextArea.appendText("App: ${it.appnameProperty().get()}\n")
                        outputTextArea.appendText("Package: $pkg\n")
                        outputTextArea.appendText("Result: $output\n")
                        progress.progress += 1.0 / n
                    }
                }
            }
            Platform.runLater {
                outputTextArea.appendText("Done!")
                progress.progress = 0.0
                progressInd.isVisible = false
                createTables()
                func()
            }
        }
    }

    inline fun disable(selected: ObservableList<App>, n: Int, crossinline func: () -> Unit) {
        pb.redirectErrorStream(false)
        outputTextArea.text = ""
        progress.progress = 0.0
        progressInd.isVisible = true
        thread(true, true) {
            selected.forEach {
                it.packagenameProperty().get().trim().lines().forEach { pkg ->
                    proc = pb.command(
                        "${prefix}adb",
                        "shell",
                        "pm",
                        "disable-user",
                        "--user",
                        "$user",
                        pkg
                    ).start()
                    val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                    var output = ""
                    while (scan.hasNextLine())
                        output += scan.nextLine() + '\n'
                    scan.close()
                    output = if ("disabled-user" in output)
                        "Success\n"
                    else "Failure\n"
                    Platform.runLater {
                        outputTextArea.appendText("App: ${it.appnameProperty().get()}\n")
                        outputTextArea.appendText("Package: $pkg\n")
                        outputTextArea.appendText("Result: $output\n")
                        progress.progress += 1.0 / n
                    }
                }
            }
            Platform.runLater {
                outputTextArea.appendText("Done!")
                progress.progress = 0.0
                progressInd.isVisible = false
                createTables()
                func()
            }
        }
    }

    inline fun enable(selected: ObservableList<App>, n: Int, crossinline func: () -> Unit) {
        pb.redirectErrorStream(false)
        outputTextArea.text = ""
        progress.progress = 0.0
        progressInd.isVisible = true
        thread(true, true) {
            selected.forEach {
                it.packagenameProperty().get().trim().lines().forEach { pkg ->
                    proc =
                        pb.command("${prefix}adb", "shell", "pm", "enable", "--user", "$user", pkg)
                            .start()
                    val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                    var output = ""
                    while (scan.hasNextLine())
                        output += scan.nextLine() + '\n'
                    scan.close()
                    output = if ("enabled" in output)
                        "Success\n"
                    else "Failure\n"
                    Platform.runLater {
                        outputTextArea.appendText("App: ${it.appnameProperty().get()}\n")
                        outputTextArea.appendText("Package: $pkg\n")
                        outputTextArea.appendText("Result: $output\n")
                        progress.progress += 1.0 / n
                    }
                }
            }
            Platform.runLater {
                outputTextArea.appendText("Done!")
                progress.progress = 0.0
                progressInd.isVisible = false
                createTables()
                func()
            }
        }
    }
}
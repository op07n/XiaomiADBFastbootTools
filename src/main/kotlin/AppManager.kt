import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.thread


class AppManager : Command() {

    companion object {
        var user = 0
        lateinit var uninstallerTableView: TableView<App>
        lateinit var reinstallerTableView: TableView<App>
        lateinit var disablerTableView: TableView<App>
        lateinit var enablerTableView: TableView<App>
        lateinit var progress: ProgressBar
        lateinit var progressInd: ProgressIndicator
        var potentialApps = this::class.java.classLoader.getResource("apps.txt").readText().lines().toMutableList()

        fun createTables() {
            uninstallerTableView.items.clear()
            reinstallerTableView.items.clear()
            disablerTableView.items.clear()
            enablerTableView.items.clear()
            val uninst = ArrayList<String>()
            val reinst = ArrayList<String>()
            val disable = ArrayList<String>()
            val enable = ArrayList<String>()
            val apps = HashMap<String, String>()
            exec("adb shell pm list packages -u --user $user").trim().lines().forEach {
                apps[it.substringAfter(':')] = "uninstalled"
            }
            exec("adb shell pm list packages -d --user $user").trim().lines().forEach {
                apps[it.substringAfter(':')] = "disabled"
            }
            exec("adb shell pm list packages -e --user $user").trim().lines().forEach {
                apps[it.substringAfter(':')] = "enabled"
            }
            potentialApps.forEach {
                uninst.clear()
                reinst.clear()
                disable.clear()
                enable.clear()
                val app = it.split(';')
                for (pkg in app[1].split(',')) {
                    if (apps[pkg].isNullOrBlank())
                        continue
                    when (apps[pkg]) {
                        "disabled" -> {
                            uninst.add(pkg)
                            enable.add(pkg)
                        }
                        "enabled" -> {
                            uninst.add(pkg)
                            disable.add(pkg)
                        }
                        "uninstalled" -> reinst.add(pkg)
                    }
                }
                if (uninst.isNotEmpty())
                    uninstallerTableView.items.add(App(app[0], uninst))
                if (reinst.isNotEmpty())
                    reinstallerTableView.items.add(App(app[0], reinst))
                if (disable.isNotEmpty())
                    disablerTableView.items.add(App(app[0], disable))
                if (enable.isNotEmpty())
                    enablerTableView.items.add(App(app[0], enable))
            }
            uninstallerTableView.refresh()
            reinstallerTableView.refresh()
            disablerTableView.refresh()
            enablerTableView.refresh()
        }

        fun addApp(app: String) {
            if (';' in app) {
                if (potentialApps.find { app.substringAfterLast(';') in it } == null)
                    potentialApps.add(app)
            } else {
                if (potentialApps.find { app in it } == null)
                    potentialApps.add("${app.substringAfterLast('.')};$app")
            }
        }

        fun uninstall(selected: ObservableList<App>, n: Int, func: () -> Unit) {
            pb.redirectErrorStream(false)
            tic.text = ""
            progress.progress = 0.0
            progressInd.isVisible = true
            thread(true, true) {
                selected.forEach {
                    it.packagenameProperty().get().trim().lines().forEach { pkg ->
                        proc = pb.command("${prefix}adb", "shell", "pm", "uninstall", "--user", "$user", pkg).start()
                        val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                        var output = ""
                        while (scan.hasNextLine())
                            output += scan.nextLine() + '\n'
                        scan.close()
                        Platform.runLater {
                            tic.appendText("App: ${it.appnameProperty().get()}\n")
                            tic.appendText("Package: $pkg\n")
                            tic.appendText("Result: $output\n")
                            progress.progress += 1.0 / n
                        }
                    }
                }
                Platform.runLater {
                    tic.appendText("Done!")
                    progress.progress = 0.0
                    progressInd.isVisible = false
                    createTables()
                    func()
                }
            }
        }

        fun reinstall(selected: ObservableList<App>, n: Int, func: () -> Unit) {
            pb.redirectErrorStream(false)
            tic.text = ""
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
                            tic.appendText("App: ${it.appnameProperty().get()}\n")
                            tic.appendText("Package: $pkg\n")
                            tic.appendText("Result: $output\n")
                            progress.progress += 1.0 / n
                        }
                    }
                }
                Platform.runLater {
                    tic.appendText("Done!")
                    progress.progress = 0.0
                    progressInd.isVisible = false
                    createTables()
                    func()
                }
            }
        }

        fun disable(selected: ObservableList<App>, n: Int, func: () -> Unit) {
            pb.redirectErrorStream(false)
            tic.text = ""
            progress.progress = 0.0
            progressInd.isVisible = true
            thread(true, true) {
                selected.forEach {
                    it.packagenameProperty().get().trim().lines().forEach { pkg ->
                        proc = pb.command("${prefix}adb", "shell", "pm", "disable-user", "--user", "$user", pkg).start()
                        val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                        var output = ""
                        while (scan.hasNextLine())
                            output += scan.nextLine() + '\n'
                        scan.close()
                        output = if ("disabled-user" in output)
                            "Success\n"
                        else "Failure\n"
                        Platform.runLater {
                            tic.appendText("App: ${it.appnameProperty().get()}\n")
                            tic.appendText("Package: $pkg\n")
                            tic.appendText("Result: $output\n")
                            progress.progress += 1.0 / n
                        }
                    }
                }
                Platform.runLater {
                    tic.appendText("Done!")
                    progress.progress = 0.0
                    progressInd.isVisible = false
                    createTables()
                    func()
                }
            }
        }

        fun enable(selected: ObservableList<App>, n: Int, func: () -> Unit) {
            pb.redirectErrorStream(false)
            tic.text = ""
            progress.progress = 0.0
            progressInd.isVisible = true
            thread(true, true) {
                selected.forEach {
                    it.packagenameProperty().get().trim().lines().forEach { pkg ->
                        proc = pb.command("${prefix}adb", "shell", "pm", "enable", "--user", "$user", pkg).start()
                        val scan = Scanner(proc.inputStream, "UTF-8").useDelimiter("")
                        var output = ""
                        while (scan.hasNextLine())
                            output += scan.nextLine() + '\n'
                        scan.close()
                        output = if ("enabled" in output)
                            "Success\n"
                        else "Failure\n"
                        Platform.runLater {
                            tic.appendText("App: ${it.appnameProperty().get()}\n")
                            tic.appendText("Package: $pkg\n")
                            tic.appendText("Result: $output\n")
                            progress.progress += 1.0 / n
                        }
                    }
                }
                Platform.runLater {
                    tic.appendText("Done!")
                    progress.progress = 0.0
                    progressInd.isVisible = false
                    createTables()
                    func()
                }
            }
        }
    }
}
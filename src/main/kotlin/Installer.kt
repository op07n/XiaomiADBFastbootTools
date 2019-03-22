import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableView
import javafx.scene.control.TextInputControl
import java.io.IOException
import java.util.*


class Installer(
    var uninstallTableView: TableView<App>,
    var reinstallTableView: TableView<App>,
    var progress: ProgressBar,
    var progressind: ProgressIndicator,
    control: TextInputControl
) : Command(control) {

    private val apps = FXCollections.observableArrayList<App>()
    private val command = Command()

    init {
        pb.redirectErrorStream(false)
    }

    fun loadApps(device: Device) {
        apps.clear()
        apps.addAll(
            App("Analytics", "com.miui.analytics"),
            App("App Vault", "com.miui.personalassistant"),
            App("App Vault", "com.mi.android.globalpersonalassistant"),
            App("Browser", "com.android.browser"),
            App("Calculator", "com.miui.calculator"),
            App("Calendar", "com.android.calendar"),
            App("Cleaner", "com.miui.cleanmaster"),
            App("Clock", "com.android.deskclock"),
            App("Compass", "com.miui.compass"),
            App("Downloads", "com.android.providers.downloads.ui"),
            App("Facebook", "com.facebook.katana"),
            App("Facebook App Installer", "com.facebook.system"),
            App("Facebook App Manager", "com.facebook.appmanager"),
            App("Facebook Services", "com.facebook.services"),
            App("Feedback", "com.miui.bugreport"),
            App("FM Radio", "com.miui.fm"),
            App("Games", "com.xiaomi.glgm"),
            App("Gmail", "com.google.android.gm"),
            App("Google App", "com.google.android.googlequicksearchbox"),
            App("Google Assistant", "com.google.android.apps.googleassistant"),
            App("Google Calculator", "com.google.android.calculator"),
            App("Google Calendar", "com.google.android.calendar"),
            App("Google Chrome", "com.android.chrome"),
            App("Google Clock", "com.google.android.deskclock"),
            App("Google Drive", "com.google.android.apps.docs"),
            App("Google Duo", "com.google.android.apps.tachyon"),
            App("Google Hangouts", "com.google.android.talk"),
            App("Google Indic Keyboard", "com.google.android.apps.inputmethod.hindi"),
            App("Google Keep", "com.google.android.keep"),
            App("Google Korean Input", "com.google.android.inputmethod.korean"),
            App("Google Maps", "com.google.android.apps.maps"),
            App("Google Photos", "com.google.android.apps.photos"),
            App("Google Pinyin Input", "com.google.android.inputmethod.pinyin"),
            App("Google Play Books", "com.google.android.apps.books"),
            App("Google Play Games", "com.google.android.play.games"),
            App("Google Play Movies", "com.google.android.videos"),
            App("Google Play Music", "com.google.android.music"),
            App("Google Zhuyin Input", "com.google.android.apps.inputmethod.zhuyin"),
            App("HybridAccessory", "com.miui.hybrid.accessory"),
            App("Joyose", "com.xiaomi.joyose"),
            App("KLO Bugreport", "com.miui.klo.bugreport"),
            App("MAB", "com.xiaomi.ab"),
            App("Mail", "com.android.email"),
            App("Mi Account", "com.xiaomi.account"),
            App("Mi AI", "com.miui.voiceassist"),
            App("Mi App Store", "com.xiaomi.mipicks"),
            App("Mi Cloud", "com.miui.cloudservice"),
            App("Mi Cloud Backup", "com.miui.cloudbackup"),
            App("Mi Cloud Sync", "com.miui.micloudsync"),
            App("Mi Credit", "com.micredit.in"),
            App("Mi Credit", "com.xiaomi.payment"),
            App("Mi Drop", "com.xiaomi.midrop"),
            App("Mi File Manager", "com.mi.android.globalFileexplorer"),
            App("Mi Pay", "com.mipay.wallet.in"),
            App("Mi Recycle", "com.xiaomi.mirecycle"),
            App("Mi Roaming", "com.miui.virtualsim"),
            App("Mi Video", "com.miui.video"),
            App("Mi Video", "com.miui.videoplayer"),
            App("Mi Wallet", "com.mipay.wallet"),
            App("MiuiDaemon", "com.miui.daemon"),
            App("Mobile Device Information Provider", "com.amazon.appmanager"),
            App("MSA", "com.miui.msa.global"),
            App("MSA", "com.miui.systemAdSolution"),
            App("Music", "com.miui.player"),
            App("News", "com.mi.globalTrendNews"),
            App("Notes", "com.miui.notes"),
            App("PAI", "android.autoinstalls.config.Xiaomi.${device.codename}"),
            App("Quick Apps", "com.miui.hybrid"),
            App("Recorder", "com.android.soundrecorder"),
            App("Scanner", "com.xiaomi.scanner"),
            App("Screen Recorder", "com.miui.screenrecorder"),
            App("Search", "com.android.quicksearchbox"),
            App("Weather", "com.miui.weather2"),
            App("Xiaomi VIP Account", "com.xiaomi.vipaccount"),
            App("Xiaomi Service Framework", "com.xiaomi.xmsf"),
            App("Xiaomi SIM Activate Service", "com.xiaomi.simactivate.service"),
            App("Yellow Pages", "com.miui.yellowpage"),
            App("YouTube", "com.google.android.youtube")
        )
        createTables()
    }

    private fun createTables() {
        var installed = command.exec("adb shell cmd package list packages")
        if (installed.contains("not found"))
            installed = command.exec("adb shell pm list packages")
        val all = command.exec("adb shell cmd package list packages -u")
        uninstallTableView.items.clear()
        reinstallTableView.items.clear()
        for (app in apps) {
            app.selectedProperty().set(false)
            if (installed.contains(app.packagenameProperty().get() + System.lineSeparator()))
                uninstallTableView.items.add(app)
            else if (all.contains(app.packagenameProperty().get() + System.lineSeparator()))
                reinstallTableView.items.add(app)
        }
        uninstallTableView.refresh()
        reinstallTableView.refresh()
    }

    fun uninstall(func: () -> Unit) {
        val undesired = FXCollections.observableArrayList<App>()
        if (uninstallTableView.items.size != 0) {
            for (app in uninstallTableView.items)
                if (app.selectedProperty().get())
                    undesired.add(app)
            if (undesired.size == 0)
                return
        } else return
        val n = undesired.size
        tic?.text = ""
        progress.progress = 0.0
        progressind.isVisible = true
        val t = Thread {
            for (app in undesired) {
                val arguments =
                    ("adb shell pm uninstall --user 0 ${app.packagenameProperty().get()}").split(' ').toTypedArray()
                arguments[0] = prefix + arguments[0]
                pb.command(*arguments)
                try {
                    proc = pb.start()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    ExceptionAlert(ex)
                }
                val scan = Scanner(proc.inputStream)
                var line = ""
                while (scan.hasNext())
                    line += scan.nextLine() + System.lineSeparator()
                scan.close()
                Platform.runLater {
                    tic?.appendText("App: ${app.appnameProperty().get()}\n")
                    tic?.appendText("Package: ${app.packagenameProperty().get()}\n")
                    tic?.appendText("Result: $line\n")
                    progress.progress += 1.0 / n
                }
            }
            Platform.runLater {
                tic?.appendText("Done!")
                progress.progress = 0.0
                progressind.isVisible = false
                func()
                createTables()
            }
        }
        t.isDaemon = true
        t.start()
    }

    fun reinstall(func: () -> Unit) {
        val desired = FXCollections.observableArrayList<App>()
        if (reinstallTableView.items.size != 0) {
            for (app in reinstallTableView.items)
                if (app.selectedProperty().get())
                    desired.add(app)
            if (desired.size == 0)
                return
        } else return
        val n = desired.size
        tic?.text = ""
        progress.progress = 0.0
        progressind.isVisible = true
        val t = Thread {
            for (app in desired) {
                val arguments =
                    ("adb shell cmd package install-existing ${app.packagenameProperty().get()}").split(' ')
                        .toTypedArray()
                arguments[0] = prefix + arguments[0]
                pb.command(*arguments)
                try {
                    proc = pb.start()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    ExceptionAlert(ex)
                }
                val scan = Scanner(proc.inputStream)
                var line = ""
                while (scan.hasNext())
                    line += scan.nextLine() + System.lineSeparator()
                scan.close()
                if (line.contains("installed for user"))
                    line = "Success\n"
                else line = "Failure [${line.substringAfter(app.packagenameProperty().get()).trim()}]\n"
                Platform.runLater {
                    tic?.appendText("App: ${app.appnameProperty().get()}\n")
                    tic?.appendText("Package: ${app.packagenameProperty().get()}\n")
                    tic?.appendText("Result: $line\n")
                    progress.progress += 1.0 / n
                }
            }
            Platform.runLater {
                tic?.appendText("Done!")
                progress.progress = 0.0
                progressind.isVisible = false
                func()
                createTables()
            }
        }
        t.isDaemon = true
        t.start()
    }
}
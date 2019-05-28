import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableView
import javafx.scene.control.TextInputControl
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class AppManager(
    var uninstallerTableView: TableView<App>,
    var reinstallerTableView: TableView<App>,
    var disablerTableView: TableView<App>,
    var enablerTableView: TableView<App>,
    var progress: ProgressBar,
    var progressind: ProgressIndicator,
    control: TextInputControl
) : Command(control) {

    private lateinit var apps: ArrayList<String>
    private val command = Command()
    private lateinit var device: Device
    var user = 0

    init {
        pb.redirectErrorStream(false)
    }

    fun loadApps(dev: Device) {
        device = dev
        apps = arrayListOf(
            "Analytics;com.miui.analytics",
            "App Vault;com.miui.personalassistant,com.mi.android.globalpersonalassistant",
            "Backup;com.miui.backup",
            "Blocklist;com.miui.antispam",
            "Browser;com.android.browser",
            "Calculator;com.miui.calculator",
            "Calendar;com.android.calendar",
            "CatchLog;com.bsp.catchlog",
            "Cleaner;com.miui.cleanmaster",
            "Clock;com.android.deskclock",
            "Compass;com.miui.compass",
            "DiagLogger;com.huaqin.diaglogger",
            "Downloads;com.android.providers.downloads.ui",
            "Facebook;com.facebook.system,com.facebook.appmanager,com.facebook.services",
            "Feedback;com.miui.bugreport",
            "FM Radio;com.miui.fm",
            "Freeform;com.miui.freeform",
            "Game Turbo;com.xiaomi.gameboosterglobal",
            "Games;com.xiaomi.glgm",
            "Gboard;com.google.android.inputmethod.latin",
            "Gmail;com.google.android.gm",
            "Google App;com.google.android.googlequicksearchbox",
            "Google Assistant;com.google.android.apps.googleassistant",
            "Google Calculator;com.google.android.calculator",
            "Google Calendar;com.google.android.calendar",
            "Google Chrome;com.android.chrome",
            "Google Clock;com.google.android.deskclock",
            "Google Drive;com.google.android.apps.docs",
            "Google Duo;com.google.android.apps.tachyon",
            "Google Hangouts;com.google.android.talk",
            "Google Indic Keyboard;com.google.android.apps.inputmethod.hindi",
            "Google Keep;com.google.android.keep",
            "Google Korean Input;com.google.android.inputmethod.korean",
            "Google Lens;com.google.ar.lens",
            "Google Maps;com.google.android.apps.maps",
            "Google Photos;com.google.android.apps.photos",
            "Google Pinyin Input;com.google.android.inputmethod.pinyin",
            "Google Play Books;com.google.android.apps.books",
            "Google Play Games;com.google.android.play.games",
            "Google Play Movies;com.google.android.videos",
            "Google Play Music;com.google.android.music",
            "Google Zhuyin Input;com.google.android.apps.inputmethod.zhuyin",
            "HybridAccessory;com.miui.hybrid.accessory",
            "Joyose;com.xiaomi.joyose",
            "KLO Bugreport;com.miui.klo.bugreport",
            "MAB;com.xiaomi.ab",
            "Mail;com.android.email",
            "Market Feedback Agent;com.google.android.feedback",
            "Mi AI;com.miui.voiceassist",
            "Mi App Store;com.xiaomi.mipicks",
            "Mi Cloud;com.miui.cloudservice,com.miui.cloudservice.sysbase,com.miui.micloudsync,com.miui.cloudbackup",
            "Mi Credit;com.xiaomi.payment,com.micredit.in",
            "Mi Drop;com.xiaomi.midrop",
            "Mi File Manager;com.mi.android.globalFileexplorer",
            "Mi Pay;com.mipay.wallet.in,com.mipay.wallet.id",
            "Mi Recycle;com.xiaomi.mirecycle",
            "Mi Roaming;com.miui.virtualsim",
            "Mi Video;com.miui.video,com.miui.videoplayer",
            "Mi VR;com.mi.dlabs.vr",
            "Mi Wallet;com.mipay.wallet",
            "Mi Wallpaper;com.miui.miwallpaper",
            "MiuiDaemon;com.miui.daemon",
            "MiWebView;com.mi.webkit.core",
            "Mobile Device Information Provider;com.amazon.appmanager",
            "MSA;com.miui.msa.global,com.miui.systemAdSolution",
            "Music;com.miui.player",
            "News;com.mi.globalTrendNews",
            "Notes;com.miui.notes",
            "Package Installer;com.miui.global.packageinstaller",
            "PAI;android.autoinstalls.config.Xiaomi.${device.codename}",
            "PartnerBookmarks;com.android.providers.partnerbookmarks",
            "PartnerNetflixActivation;com.netflix.partner.activation",
            "POCO Launcher;com.mi.android.globallauncher",
            "Quick Apps;com.miui.hybrid",
            "Quick Ball;com.miui.touchassistant",
            "Recorder;com.android.soundrecorder",
            "Scanner;com.xiaomi.scanner",
            "Screen Recorder;com.miui.screenrecorder",
            "Search;com.android.quicksearchbox",
            "SMS Extra;com.miui.smsextra",
            "Translation Service;com.miui.translationservice,com.miui.translation.kingsoft,com.miui.translation.xmcloud,com.miui.translation.youdao",
            "UniPlay Service;com.milink.service",
            "VsimCore;com.miui.vsimcore",
            "Weather;com.miui.weather2,com.miui.providers.weather",
            "Xiaomi VIP Account;com.xiaomi.vipaccount",
            "Xiaomi Service Framework;com.xiaomi.xmsf",
            "Xiaomi SIM Activate Service;com.xiaomi.simactivate.service",
            "Yellow Pages;com.miui.yellowpage",
            "YouTube;com.google.android.youtube"
        )
        val support = command.exec("adb shell cmd package install-existing xaft")
        device.reinstaller = !("not found" in support || "Unknown command" in support)
        device.disabler = "enabled" in command.exec("adb shell pm enable com.android.settings")
        createTables()
    }

    fun createTables() {
        val disabled = command.exec("adb shell pm list packages -d --user $user")
        val enabled = command.exec("adb shell pm list packages -e --user $user")
        val all = command.exec("adb shell pm list packages -u --user $user")
        uninstallerTableView.items.clear()
        reinstallerTableView.items.clear()
        disablerTableView.items.clear()
        enablerTableView.items.clear()
        apps.forEach {
            val app = it.split(';')
            val uninst = ArrayList<String>()
            val reinst = ArrayList<String>()
            val disable = ArrayList<String>()
            val enable = ArrayList<String>()
            app[1].split(',').forEach { pkg ->
                when {
                    pkg + System.lineSeparator() in disabled -> {
                        uninst.add(pkg)
                        enable.add(pkg)
                    }
                    pkg + System.lineSeparator() in enabled -> {
                        uninst.add(pkg)
                        disable.add(pkg)
                    }
                    pkg + System.lineSeparator() in all -> reinst.add(pkg)
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
            if (apps.find { app.substringAfterLast(';') in it } == null)
                apps.add(app)
        } else {
            if (apps.find { app in it } == null)
                apps.add("${app.substringAfterLast('.')};$app")
        }
    }

    fun isAppSelected(option: Int): Boolean {
        val list = when (option) {
            0 -> uninstallerTableView.items
            1 -> reinstallerTableView.items
            2 -> disablerTableView.items
            else -> enablerTableView.items
        }
        if (list.isNotEmpty()) {
            for (app in list)
                if (app.selectedProperty().get())
                    return true
            return false
        } else return false
    }

    fun uninstall(func: () -> Unit) {
        val selected = FXCollections.observableArrayList<App>()
        var n = 0
        uninstallerTableView.items.forEach {
            if (it.selectedProperty().get()) {
                selected.add(it)
                n += it.packagenameProperty().get().lines().size
            }
        }
        tic?.text = ""
        progress.progress = 0.0
        progressind.isVisible = true
        thread(true, true) {
            selected.forEach {
                it.packagenameProperty().get().lines().forEach { pkg ->
                    val arguments =
                        ("adb shell pm uninstall --user $user $pkg").split(' ').toTypedArray()
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
                        tic?.appendText("App: ${it.appnameProperty().get()}\n")
                        tic?.appendText("Package: $pkg\n")
                        tic?.appendText("Result: $line\n")
                        progress.progress += 1.0 / n
                    }
                }
            }
            Platform.runLater {
                tic?.appendText("Done!")
                progress.progress = 0.0
                progressind.isVisible = false
                createTables()
                func()
            }
        }
    }

    fun reinstall(func: () -> Unit) {
        val selected = FXCollections.observableArrayList<App>()
        var n = 0
        reinstallerTableView.items.forEach {
            if (it.selectedProperty().get()) {
                selected.add(it)
                n += it.packagenameProperty().get().lines().size
            }
        }
        tic?.text = ""
        progress.progress = 0.0
        progressind.isVisible = true
        thread(true, true) {
            selected.forEach {
                it.packagenameProperty().get().lines().forEach { pkg ->
                    val arguments =
                        ("adb shell cmd package install-existing --user $user $pkg").split(' ')
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
                    line = if ("installed for user" in line)
                        "Success\n"
                    else "Failure [${line.substringAfter(pkg).trim()}]\n"
                    Platform.runLater {
                        tic?.appendText("App: ${it.appnameProperty().get()}\n")
                        tic?.appendText("Package: $pkg\n")
                        tic?.appendText("Result: $line\n")
                        progress.progress += 1.0 / n
                    }
                }
            }
            Platform.runLater {
                tic?.appendText("Done!")
                progress.progress = 0.0
                progressind.isVisible = false
                createTables()
                func()
            }
        }
    }

    fun disable(func: () -> Unit) {
        val selected = FXCollections.observableArrayList<App>()
        var n = 0
        disablerTableView.items.forEach {
            if (it.selectedProperty().get()) {
                selected.add(it)
                n += it.packagenameProperty().get().lines().size
            }
        }
        tic?.text = ""
        progress.progress = 0.0
        progressind.isVisible = true
        thread(true, true) {
            selected.forEach {
                it.packagenameProperty().get().lines().forEach { pkg ->
                    val arguments =
                        ("adb shell pm disable-user --user $user $pkg").split(' ')
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
                    line = if ("disabled-user" in line)
                        "Success\n"
                    else "Failure\n"
                    Platform.runLater {
                        tic?.appendText("App: ${it.appnameProperty().get()}\n")
                        tic?.appendText("Package: $pkg\n")
                        tic?.appendText("Result: $line\n")
                        progress.progress += 1.0 / n
                    }
                }
            }
            Platform.runLater {
                tic?.appendText("Done!")
                progress.progress = 0.0
                progressind.isVisible = false
                createTables()
                func()
            }
        }
    }

    fun enable(func: () -> Unit) {
        val selected = FXCollections.observableArrayList<App>()
        var n = 0
        enablerTableView.items.forEach {
            if (it.selectedProperty().get()) {
                selected.add(it)
                n += it.packagenameProperty().get().lines().size
            }
        }
        tic?.text = ""
        progress.progress = 0.0
        progressind.isVisible = true
        thread(true, true) {
            selected.forEach {
                it.packagenameProperty().get().lines().forEach { pkg ->
                    val arguments =
                        ("adb shell pm enable --user $user $pkg").split(' ')
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
                    line = if ("enabled" in line)
                        "Success\n"
                    else "Failure\n"
                    Platform.runLater {
                        tic?.appendText("App: ${it.appnameProperty().get()}\n")
                        tic?.appendText("Package: $pkg\n")
                        tic?.appendText("Result: $line\n")
                        progress.progress += 1.0 / n
                    }
                }
            }
            Platform.runLater {
                tic?.appendText("Done!")
                progress.progress = 0.0
                progressind.isVisible = false
                createTables()
                func()
            }
        }
    }
}
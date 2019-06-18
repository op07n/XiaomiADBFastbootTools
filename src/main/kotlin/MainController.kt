import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.*
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class MainController : Initializable {

    //TODO: Check ERROR mode device

    @FXML
    private lateinit var deviceMenu: Menu
    @FXML
    private lateinit var appManagerMenu: Menu
    @FXML
    private lateinit var secondSpaceButton: CheckMenuItem
    @FXML
    private lateinit var recoveryMenuItem: MenuItem
    @FXML
    private lateinit var reloadMenuItem: MenuItem
    @FXML
    private lateinit var infoTextArea: TextArea
    @FXML
    private lateinit var outputTextArea: TextArea
    @FXML
    private lateinit var progressBar: ProgressBar
    @FXML
    private lateinit var progressIndicator: ProgressIndicator
    @FXML
    private lateinit var uninstallerTableView: TableView<App>
    @FXML
    private lateinit var reinstallerTableView: TableView<App>
    @FXML
    private lateinit var disablerTableView: TableView<App>
    @FXML
    private lateinit var enablerTableView: TableView<App>
    @FXML
    private lateinit var uncheckTableColumn: TableColumn<App, Boolean>
    @FXML
    private lateinit var unappTableColumn: TableColumn<App, String>
    @FXML
    private lateinit var unpackageTableColumn: TableColumn<App, String>
    @FXML
    private lateinit var recheckTableColumn: TableColumn<App, Boolean>
    @FXML
    private lateinit var reappTableColumn: TableColumn<App, String>
    @FXML
    private lateinit var repackageTableColumn: TableColumn<App, String>
    @FXML
    private lateinit var discheckTableColumn: TableColumn<App, Boolean>
    @FXML
    private lateinit var disappTableColumn: TableColumn<App, String>
    @FXML
    private lateinit var dispackageTableColumn: TableColumn<App, String>
    @FXML
    private lateinit var encheckTableColumn: TableColumn<App, Boolean>
    @FXML
    private lateinit var enappTableColumn: TableColumn<App, String>
    @FXML
    private lateinit var enpackageTableColumn: TableColumn<App, String>
    @FXML
    private lateinit var dpiTextField: TextField
    @FXML
    private lateinit var widthTextField: TextField
    @FXML
    private lateinit var heightTextField: TextField
    @FXML
    private lateinit var partitionComboBox: ComboBox<String>
    @FXML
    private lateinit var scriptComboBox: ComboBox<String>
    @FXML
    private lateinit var autobootCheckBox: CheckBox
    @FXML
    private lateinit var imageLabel: Label
    @FXML
    private lateinit var romLabel: Label
    @FXML
    private lateinit var codenameTextField: TextField
    @FXML
    private lateinit var branchComboBox: ComboBox<String>
    @FXML
    private lateinit var versionLabel: Label
    @FXML
    private lateinit var appManagerPane: TabPane
    @FXML
    private lateinit var reinstallerTab: Tab
    @FXML
    private lateinit var disablerTab: Tab
    @FXML
    private lateinit var enablerTab: Tab
    @FXML
    private lateinit var fileExplorerPane: TitledPane
    @FXML
    private lateinit var camera2Pane: TitledPane
    @FXML
    private lateinit var resolutionPane: TitledPane
    @FXML
    private lateinit var flasherPane: TitledPane
    @FXML
    private lateinit var wiperPane: TitledPane
    @FXML
    private lateinit var oemPane: TitledPane
    @FXML
    private lateinit var dpiPane: TitledPane

    private val version = "6.5"

    private var image: File? = null
    private var rom: File? = null

    companion object {
        lateinit var thread: Thread
    }

    private fun setPanels() {
        when (Device.mode) {
            Mode.ADB -> {
                appManagerPane.isDisable = false
                appManagerMenu.isDisable = false
                reinstallerTab.isDisable = !Device.reinstaller
                disablerTab.isDisable = !Device.disabler
                enablerTab.isDisable = !Device.disabler
                camera2Pane.isDisable = true
                fileExplorerPane.isDisable = false
                resolutionPane.isDisable = false
                dpiPane.isDisable = false
                flasherPane.isDisable = true
                wiperPane.isDisable = true
                oemPane.isDisable = true
                deviceMenu.isDisable = false
                recoveryMenuItem.isDisable = false
                reloadMenuItem.isDisable = false
            }
            Mode.RECOVERY -> {
                appManagerPane.isDisable = true
                appManagerMenu.isDisable = true
                reinstallerTab.isDisable = true
                disablerTab.isDisable = true
                enablerTab.isDisable = true
                camera2Pane.isDisable = false
                fileExplorerPane.isDisable = true
                resolutionPane.isDisable = true
                dpiPane.isDisable = true
                flasherPane.isDisable = true
                wiperPane.isDisable = true
                oemPane.isDisable = true
                deviceMenu.isDisable = false
                recoveryMenuItem.isDisable = false
                reloadMenuItem.isDisable = false
            }
            Mode.FASTBOOT -> {
                appManagerPane.isDisable = true
                appManagerMenu.isDisable = true
                camera2Pane.isDisable = true
                fileExplorerPane.isDisable = true
                resolutionPane.isDisable = true
                dpiPane.isDisable = true
                flasherPane.isDisable = false
                wiperPane.isDisable = false
                oemPane.isDisable = false
                deviceMenu.isDisable = false
                recoveryMenuItem.isDisable = true
                reloadMenuItem.isDisable = false
            }
            else -> {
                appManagerPane.isDisable = true
                appManagerMenu.isDisable = true
                camera2Pane.isDisable = true
                fileExplorerPane.isDisable = true
                resolutionPane.isDisable = true
                dpiPane.isDisable = true
                flasherPane.isDisable = true
                wiperPane.isDisable = true
                oemPane.isDisable = true
                deviceMenu.isDisable = true
                recoveryMenuItem.isDisable = true
                reloadMenuItem.isDisable = true
            }
        }
    }

    private fun setUI() {
        setPanels()
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> {
                infoTextArea.text = "Serial number:\t\t${Device.serial}\n" +
                        "Codename:\t\t${Device.codename}\n" +
                        "Bootloader:\t\t"
                if (Device.bootloader)
                    infoTextArea.appendText("unlocked\n")
                else infoTextArea.appendText("locked\n")
                infoTextArea.appendText("Camera2:\t\t\t")
                if (Device.camera2)
                    infoTextArea.appendText("enabled")
                else infoTextArea.appendText("unknown")
            }
            Mode.FASTBOOT -> {
                infoTextArea.text = "Serial number:\t\t${Device.serial}\n" +
                        "Codename:\t\t${Device.codename}\n" +
                        "Bootloader:\t\t"
                if (Device.bootloader)
                    infoTextArea.appendText("unlocked")
                else infoTextArea.appendText("locked")
                if (Device.anti != 0)
                    infoTextArea.appendText("\nAnti version:\t\t${Device.anti}")
            }
            else -> infoTextArea.text = ""
        }
    }

    private fun versionToInt(ver: String): Int {
        val bits = "$ver.0".split('.')
        return bits[0].toInt() * 100 + bits[1].toInt() * 10 + bits[2].toInt()
    }

    private fun checkVersion() {
        val huc =
            URL("https://github.com/Saki-EU/XiaomiADBFastbootTools/releases/latest").openConnection() as HttpURLConnection
        huc.requestMethod = "GET"
        huc.setRequestProperty("Referer", "https://github.com/")
        huc.instanceFollowRedirects = false
        try {
            huc.connect()
            huc.disconnect()
        } catch (e: IOException) {
            return
        }
        val link = huc.getHeaderField("Location")
        val latest = link.substringAfterLast('/')
        if (versionToInt(latest) > versionToInt(version)) {
            val alert = Alert(AlertType.INFORMATION)
            alert.initStyle(StageStyle.UTILITY)
            alert.title = "New version available!"
            alert.graphic = ImageView("mitu.png")
            alert.headerText =
                "Version $latest is available!"
            val vb = VBox()
            vb.alignment = Pos.CENTER
            val download = Hyperlink("Download")
            download.onAction = EventHandler {
                if ("linux" in System.getProperty("os.name").toLowerCase())
                    Runtime.getRuntime().exec("xdg-open $link")
                else Desktop.getDesktop().browse(URI(link))
            }
            download.font = Font(15.0)
            vb.children.add(download)
            alert.dialogPane.content = vb
            alert.showAndWait()
        }
    }

    private fun checkADBFastboot() {
        //TODO Linux check
        if (File("adb.exe").exists() && File("fastboot.exe").exists() && File("AdbWinApi.dll").exists() && File("AdbWinUsbApi.dll").exists()) {
            Command.prefix = System.getProperty("user.dir") + '/'
            Command.exec("adb start-server")
            return
        } else if (File("adb").exists() && File("fastboot").exists()) {
            Command.prefix = "./"
            Command.exec("adb start-server")
            return
        } else {
            try {
                ProcessBuilder("adb", "--version").start()
                ProcessBuilder("fastboot", "--version").start()
                Command.exec("adb start-server")
            } catch (e: Exception) {
                val alert = Alert(AlertType.ERROR)
                alert.title = "Fatal Error"
                alert.headerText =
                    "ERROR: Can't find ADB/Fastboot!\nPlease install them system-wide or put the JAR next to them!"
                alert.showAndWait()
                Platform.exit()
            }
        }
    }

    private fun checkADB(): Boolean {
        val adb = Device.readADB()
        setUI()
        return adb
    }

    private fun checkFastboot(): Boolean {
        val fb = Device.readFastboot()
        setUI()
        return fb
    }

    private fun loadDevice() {
        if ("Looking" !in outputTextArea.text)
            outputTextArea.text = "Looking for devices..."
        reloadMenuItem.isDisable = true
        progressIndicator.isVisible = true
        when {
            Device.readADB() -> {
                progressIndicator.isVisible = false
                val support = Command.exec("adb shell cmd package install-existing xaft")
                Device.reinstaller = !("not found" in support || "Unknown command" in support)
                Device.disabler = "enabled" in Command.exec("adb shell pm enable com.android.settings")
                AppManager.createTables()
                codenameTextField.text = Device.codename
                if (Device.mode == Mode.ADB) {
                    dpiTextField.text = if (Device.dpi != -1)
                        Device.dpi.toString()
                    else "ERROR"
                    widthTextField.text = if (Device.width != -1)
                        Device.width.toString()
                    else "ERROR"
                    heightTextField.text = if (Device.height != -1)
                        Device.height.toString()
                    else "ERROR"
                }
                outputTextArea.text = "Device found in ADB mode!\n\n"
                if (Device.mode == Mode.ADB && (!Device.reinstaller || !Device.disabler))
                    outputTextArea.appendText("Note:\nThis device isn't fully supported by the App Manager.\nAs a result, some modules have been disabled.")
                setUI()
            }
            Device.readFastboot() -> {
                progressIndicator.isVisible = false
                codenameTextField.text = Device.codename
                outputTextArea.text = "Device found in Fastboot mode!"
                setUI()
            }
            Device.mode == Mode.AUTH && "Unauthorised" !in outputTextArea.text -> {
                outputTextArea.text = "Unauthorised device found!\nPlease allow USB debugging!"
                setUI()
            }
        }
    }

    private fun checkDevice() {
        thread = Thread {
            while (true) {
                if (Device.mode != Mode.ADB && Device.mode != Mode.FASTBOOT && Device.mode != Mode.RECOVERY)
                    Platform.runLater { loadDevice() }
                try {
                    Thread.sleep(2000)
                } catch (ie: InterruptedException) {
                    break
                }
            }
        }
        thread.isDaemon = true
        thread.start()
    }

    private fun confirm(msg: String = "Are you sure you want to proceed?", func: () -> Unit) {
        val alert = Alert(AlertType.CONFIRMATION)
        alert.initStyle(StageStyle.UTILITY)
        alert.isResizable = false
        alert.headerText = msg.trim()
        val yes = ButtonType("Yes")
        val no = ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE)
        alert.buttonTypes.setAll(yes, no)
        val result = alert.showAndWait()
        if (result.get() == yes)
            func()
    }

    override fun initialize(url: URL, rb: ResourceBundle?) {
        outputTextArea.text = "Looking for devices..."
        progressIndicator.isVisible = true
        thread(true) {
            checkADBFastboot()
            checkVersion()
            checkDevice()
        }
        partitionComboBox.items.addAll(
            "boot", "cust", "modem", "persist", "recovery", "system"
        )
        scriptComboBox.items.addAll(
            "Clean install", "Clean install and lock", "Update"
        )
        branchComboBox.items.addAll(
            "Global Stable",
            "Global Developer",
            "China Stable",
            "China Developer",
            "EEA Stable",
            "Russia Stable",
            "India Stable"
        )

        uncheckTableColumn.cellValueFactory = PropertyValueFactory("selected")
        uncheckTableColumn.setCellFactory { CheckBoxTableCell() }
        unappTableColumn.cellValueFactory = PropertyValueFactory("appname")
        unpackageTableColumn.cellValueFactory = PropertyValueFactory("packagename")

        recheckTableColumn.cellValueFactory = PropertyValueFactory("selected")
        recheckTableColumn.setCellFactory { CheckBoxTableCell() }
        reappTableColumn.cellValueFactory = PropertyValueFactory("appname")
        repackageTableColumn.cellValueFactory = PropertyValueFactory("packagename")

        discheckTableColumn.cellValueFactory = PropertyValueFactory("selected")
        discheckTableColumn.setCellFactory { CheckBoxTableCell() }
        disappTableColumn.cellValueFactory = PropertyValueFactory("appname")
        dispackageTableColumn.cellValueFactory = PropertyValueFactory("packagename")

        encheckTableColumn.cellValueFactory = PropertyValueFactory("selected")
        encheckTableColumn.setCellFactory { CheckBoxTableCell() }
        enappTableColumn.cellValueFactory = PropertyValueFactory("appname")
        enpackageTableColumn.cellValueFactory = PropertyValueFactory("packagename")

        uninstallerTableView.columns.setAll(uncheckTableColumn, unappTableColumn, unpackageTableColumn)
        reinstallerTableView.columns.setAll(recheckTableColumn, reappTableColumn, repackageTableColumn)
        disablerTableView.columns.setAll(discheckTableColumn, disappTableColumn, dispackageTableColumn)
        enablerTableView.columns.setAll(encheckTableColumn, enappTableColumn, enpackageTableColumn)

        Command.tic = outputTextArea
        Flasher.progressInd = progressIndicator
        ROMFlasher.progress = progressBar
        AppManager.uninstallerTableView = uninstallerTableView
        AppManager.reinstallerTableView = reinstallerTableView
        AppManager.disablerTableView = disablerTableView
        AppManager.enablerTableView = enablerTableView
        AppManager.progress = progressBar
        AppManager.progressInd = progressIndicator
    }

    private fun checkCamera2(): Boolean = "1" in Command.exec("adb shell getprop persist.camera.HAL3.enabled")

    private fun checkEIS(): Boolean = "1" in Command.exec("adb shell getprop persist.camera.eis.enable")

    @FXML
    private fun disableCamera2ButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (Device.mode != Mode.RECOVERY) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            Command.exec("adb shell setprop persist.camera.HAL3.enabled 0")
            outputTextArea.text = if (!checkCamera2())
                "Camera2 disabled!"
            else "ERROR: Couldn't disable Camera2!"
        }
    }

    @FXML
    private fun enableCamera2ButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (Device.mode != Mode.RECOVERY) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            Command.exec("adb shell setprop persist.camera.HAL3.enabled 1")
            outputTextArea.text = if (checkCamera2())
                "Camera2 enabled!"
            else "ERROR: Couldn't enable Camera2!"
        }
    }

    @FXML
    private fun disableEISButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (Device.mode != Mode.RECOVERY) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            Command.exec("adb shell setprop persist.camera.eis.enable 0")
            outputTextArea.text = if (!checkEIS())
                "EIS disabled!"
            else "ERROR: Couldn't disable EIS!"
        }
    }

    @FXML
    private fun enableEISButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (Device.mode != Mode.RECOVERY) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            Command.exec("adb shell setprop persist.camera.eis.enable 1")
            outputTextArea.text = if (checkEIS())
                "EIS enabled!"
            else "ERROR: Couldn't enable EIS!"
        }
    }

    @FXML
    private fun openButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            val scene = Scene(FXMLLoader(javaClass.classLoader.getResource("FileExplorer.fxml")).load<Parent>())
            val stage = Stage()
            stage.scene = scene
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.scene = scene
            stage.title = "File Explorer"
            stage.isResizable = false
            stage.showAndWait()
        }
    }

    @FXML
    private fun applyDpiButtonPressed(event: ActionEvent) {
        if (dpiTextField.text.trim().isEmpty())
            return
        if (checkADB()) {
            val attempt = Command.exec_displayed("adb shell wm density ${dpiTextField.text.trim()}")
            outputTextArea.text = when {
                "permission" in attempt ->
                    "ERROR: Please allow USB debugging (Security settings)!"
                "bad" in attempt ->
                    "ERROR: Invalid value!"
                attempt.isEmpty() ->
                    "Done!"
                else ->
                    "ERROR: $attempt"
            }
        }
    }

    @FXML
    private fun resetDpiButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            val attempt = Command.exec_displayed("adb shell wm density reset")
            outputTextArea.text = when {
                "permission" in attempt ->
                    "ERROR: Please allow USB debugging (Security settings)!"
                attempt.isEmpty() ->
                    "Done!"
                else ->
                    "ERROR: $attempt"
            }
        }
    }

    @FXML
    private fun applyResButtonPressed(event: ActionEvent) {
        if (widthTextField.text.trim().isEmpty() || heightTextField.text.trim().isEmpty())
            return
        if (checkADB()) {
            val attempt =
                Command.exec_displayed("adb shell wm size ${widthTextField.text.trim()}x${heightTextField.text.trim()}")
            outputTextArea.text = when {
                "permission" in attempt ->
                    "ERROR: Please allow USB debugging (Security settings)!"
                "bad" in attempt ->
                    "ERROR: Invalid value!"
                attempt.isEmpty() ->
                    "Done!"
                else ->
                    "ERROR: $attempt"
            }
        }
    }

    @FXML
    private fun resetResButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            val attempt = Command.exec_displayed("adb shell wm size reset")
            outputTextArea.text = when {
                "permission" in attempt ->
                    "ERROR: Please allow USB debugging (Security settings)!"
                attempt.isEmpty() ->
                    "Done!"
                else ->
                    "ERROR: $attempt"
            }
        }
    }

    @FXML
    private fun readPropertiesMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB())
                Command.exec_displayed("adb shell getprop")
            Mode.FASTBOOT -> if (checkFastboot())
                Command.exec_displayed("fastboot getvar all")
            else -> return
        }
    }

    //TODO

    @FXML
    private fun savePropertiesMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB()) {
                val props = Command.exec("adb shell getprop")
                val fc = FileChooser()
                fc.extensionFilters.add(FileChooser.ExtensionFilter("Text File", "*"))
                fc.title = "Save properties"
                fc.showSaveDialog((event.target as MenuItem).parentPopup.ownerWindow)?.let {
                    try {
                        it.writeText(props)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ExceptionAlert(ex)
                    }
                }
            }
            Mode.FASTBOOT -> if (checkFastboot()) {
                val props = Command.exec("fastboot getvar all")
                val fc = FileChooser()
                fc.extensionFilters.add(FileChooser.ExtensionFilter("Text File", "*"))
                fc.title = "Save properties"
                fc.showSaveDialog((event.target as MenuItem).parentPopup.ownerWindow)?.let {
                    try {
                        it.writeText(props)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ExceptionAlert(ex)
                    }
                }
            }
            else -> return
        }
    }

    @FXML
    private fun antirbButtonPressed(event: ActionEvent) {
        if (checkFastboot()) {
            val dummy = File("dummy.img")
            dummy.writeBytes(ByteArray(8192))
            val result = Command.exec("fastboot flash antirbpass dummy.img")
            if ("FAILED" in result)
                Command.exec_displayed("fastboot oem ignore_anti")
            else outputTextArea.text = result
            dummy.delete()
        }
    }

    @FXML
    private fun browseimageButtonPressed(event: ActionEvent) {
        val fc = FileChooser()
        fc.extensionFilters.add(FileChooser.ExtensionFilter("Image File", "*.*"))
        fc.title = "Select an image"
        image = fc.showOpenDialog((event.source as Node).scene.window)
        imageLabel.text = image?.name
    }

    @FXML
    private fun flashimageButtonPressed(event: ActionEvent) {
        image?.let {
            partitionComboBox.value?.let { pcb ->
                if (it.absolutePath.isNotEmpty() && pcb.isBlank() && checkFastboot()) {
                    confirm {
                        if (autobootCheckBox.isSelected && pcb.trim() == "recovery")
                            Flasher.exec(it, "fastboot flash ${pcb.trim()}", "fastboot boot")
                        else Flasher.exec(it, "fastboot flash ${pcb.trim()}")
                    }
                }
            }
        }
    }

    @FXML
    private fun browseromButtonPressed(event: ActionEvent) {
        val dc = DirectoryChooser()
        dc.title = "Select the root directory of a Fastboot ROM"
        outputTextArea.text = ""
        romLabel.text = "-"
        rom = dc.showDialog((event.source as Node).scene.window)
        rom?.let {
            if (File(it, "images").exists()) {
                romLabel.text = it.name
                outputTextArea.text = "Fastboot ROM found!"
                File(it, "flash_all.sh").setExecutable(true, false)
                File(it, "flash_all_lock.sh").setExecutable(true, false)
                File(it, "flash_all_except_storage.sh").setExecutable(true, false)
                File(it, "flash_all_except_data.sh").setExecutable(true, false)
                File(it, "flash_all_except_data_storage.sh").setExecutable(true, false)
            } else {
                outputTextArea.text = "ERROR: Fastboot ROM not found!"
                rom = null
            }
        }
    }

    @FXML
    private fun flashromButtonPressed(event: ActionEvent) {
        rom?.let {
            scriptComboBox.value?.let { scb ->
                if (checkFastboot())
                    confirm {
                        setPanels()
                        when (scb) {
                            "Clean install" -> ROMFlasher.exec("flash_all")
                            "Clean install and lock" -> ROMFlasher.exec("flash_all_lock")
                            "Update" -> when {
                                File(
                                    it,
                                    "flash_all_except_data_storage.sh"
                                ).exists() -> ROMFlasher.exec("flash_all_except_data_storage")
                                File(
                                    it,
                                    "flash_all_except_data.sh"
                                ).exists() -> ROMFlasher.exec("flash_all_except_data")
                                else -> ROMFlasher.exec("flash_all_except_storage")
                            }
                        }
                    }
            }
        }
    }

    @FXML
    private fun bootButtonPressed(event: ActionEvent) {
        image?.let {
            if (it.absolutePath.isNotEmpty() && checkFastboot())
                Flasher.exec(it, "fastboot boot")
        }
    }

    @FXML
    private fun cacheButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            Command.exec_displayed("fastboot erase cache")
    }

    @FXML
    private fun dataButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            confirm("All your data will be gone.\nAre you sure you want to proceed?") { Command.exec_displayed("fastboot erase userdata") }
    }

    @FXML
    private fun cachedataButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            confirm("All your data will be gone.\nAre you sure you want to proceed?") {
                Command.exec_displayed(
                    "fastboot erase cache",
                    "fastboot erase userdata"
                )
            }
    }

    @FXML
    private fun lockButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            confirm("Your partitions must be intact in order to successfully lock the bootloader.\nAre you sure you want to proceed?") {
                Command.exec_displayed(
                    "fastboot oem lock"
                )
            }
    }

    @FXML
    private fun unlockButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            Command.exec_displayed("fastboot oem unlock")
    }

    @FXML
    private fun getlinkButtonPressed(event: ActionEvent) {
        branchComboBox.value?.let {
            if (codenameTextField.text.isNotBlank()) {
                val codename = codenameTextField.text.trim()
                val huc = when (it) {
                    "Global Stable" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_global&b=F&r=global&n=")
                    "Global Developer" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_global&b=X&r=global&n=")
                    "China Stable" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}&b=F&r=cn&n=")
                    "China Developer" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}&b=X&r=cn&n=")
                    "EEA Stable" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_eea_global&b=F&r=eea&n=")
                    "Russia Stable" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_ru_global&b=F&r=global&n=")
                    "India Stable" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_india_global&b=F&r=global&n=")
                    else -> URL("http://google.com")
                }.openConnection() as HttpURLConnection
                huc.requestMethod = "GET"
                huc.setRequestProperty("Referer", "http://en.miui.com/a-234.html")
                huc.instanceFollowRedirects = false
                try {
                    huc.connect()
                    huc.disconnect()
                } catch (e: IOException) {
                    return
                }
                huc.getHeaderField("Location")?.let { link ->
                    if ("bigota" in link) {
                        versionLabel.text = link.substringAfter(".com/").substringBefore('/')
                        outputTextArea.appendText("\n\n$link\n\nLink copied to clipboard!")
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(link), null)
                    } else {
                        versionLabel.text = "-"
                        outputTextArea.appendText("\n\nLink not found!")
                    }
                }
            }
        }
    }

    @FXML
    private fun downloadromButtonPressed(event: ActionEvent) {
        branchComboBox.value?.let {
            if (codenameTextField.text.isNotBlank()) {
                val codename = codenameTextField.text.trim()
                val huc = when (it) {
                    "Global Stable" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_global&b=F&r=global&n=")
                    "Global Developer" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_global&b=X&r=global&n=")
                    "China Stable" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}&b=F&r=cn&n=")
                    "China Developer" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}&b=X&r=cn&n=")
                    "EEA Stable" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_eea_global&b=F&r=eea&n=")
                    "Russia Stable" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_ru_global&b=F&r=global&n=")
                    "India Stable" ->
                        URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_india_global&b=F&r=global&n=")
                    else -> URL("http://google.com")
                }.openConnection() as HttpURLConnection
                huc.requestMethod = "GET"
                huc.setRequestProperty("Referer", "http://en.miui.com/a-234.html")
                huc.instanceFollowRedirects = false
                try {
                    huc.connect()
                    huc.disconnect()
                } catch (e: IOException) {
                    return
                }
                huc.getHeaderField("Location")?.let { link ->
                    if ("bigota" in link) {
                        versionLabel.text = link.substringAfter(".com/").substringBefore('/')
                        outputTextArea.appendText("\n\nStarting download in browser...")
                        if ("linux" in System.getProperty("os.name").toLowerCase())
                            Runtime.getRuntime().exec("xdg-open $link")
                        else Desktop.getDesktop().browse(URI(link))
                    } else {
                        versionLabel.text = "-"
                        outputTextArea.appendText("\n\nLink not found!")
                    }
                }
            }
        }
    }

    private fun reload() {
        outputTextArea.clear()
        infoTextArea.clear()
        loadDevice()
    }

    @FXML
    private fun systemMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB()) {
                Command.exec("adb reboot")
                reload()
            }
            Mode.FASTBOOT -> if (checkFastboot()) {
                Command.exec("fastboot reboot")
                reload()
            }
            else -> return
        }
    }

    @FXML
    private fun recoveryMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB()) {
                Command.exec("adb reboot recovery")
                reload()
            }
            else -> return
        }
    }

    @FXML
    private fun fastbootMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB()) {
                Command.exec("adb reboot bootloader")
                reload()
            }
            Mode.FASTBOOT -> if (checkFastboot()) {
                Command.exec("fastboot reboot bootloader")
                reload()
            }
            else -> return
        }
    }

    @FXML
    private fun edlMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB()) {
                Command.exec("adb reboot edl")
                reload()
            }
            Mode.FASTBOOT -> if (checkFastboot()) {
                Command.exec("fastboot oem edl")
                reload()
            }
            else -> return
        }
    }

    @FXML
    private fun reloadMenuItemPressed(event: ActionEvent) = reload()

    private fun isAppSelected(list: ObservableList<App>): Boolean {
        if (list.isNotEmpty()) {
            for (app in list)
                if (app.selectedProperty().get())
                    return true
            return false
        } else return false
    }

    @FXML
    private fun uninstallButtonPressed(event: ActionEvent) {
        if (isAppSelected(uninstallerTableView.items) && checkADB())
            confirm("Uninstalling apps which aren't listed by default may brick your Device.\nAre you sure you want to proceed?") {
                setPanels()
                val selected = FXCollections.observableArrayList<App>()
                var n = 0
                uninstallerTableView.items.forEach {
                    if (it.selectedProperty().get()) {
                        selected.add(it)
                        n += it.packagenameProperty().get().lines().size
                    }
                }
                AppManager.uninstall(selected, n) {
                    setPanels()
                }
            }
    }

    @FXML
    private fun reinstallButtonPressed(event: ActionEvent) {
        if (isAppSelected(reinstallerTableView.items) && checkADB()) {
            setPanels()
            val selected = FXCollections.observableArrayList<App>()
            var n = 0
            reinstallerTableView.items.forEach {
                if (it.selectedProperty().get()) {
                    selected.add(it)
                    n += it.packagenameProperty().get().lines().size
                }
            }
            AppManager.reinstall(selected, n) {
                setPanels()
            }
        }
    }

    @FXML
    private fun disableButtonPressed(event: ActionEvent) {
        if (isAppSelected(disablerTableView.items) && checkADB())
            confirm("Disabling apps which aren't listed by default may brick your Device.\nAre you sure you want to proceed?") {
                setPanels()
                val selected = FXCollections.observableArrayList<App>()
                var n = 0
                disablerTableView.items.forEach {
                    if (it.selectedProperty().get()) {
                        selected.add(it)
                        n += it.packagenameProperty().get().lines().size
                    }
                }
                AppManager.disable(selected, n) {
                    setPanels()
                }
            }
    }

    @FXML
    private fun enableButtonPressed(event: ActionEvent) {
        if (isAppSelected(enablerTableView.items) && checkADB()) {
            setPanels()
            val selected = FXCollections.observableArrayList<App>()
            var n = 0
            enablerTableView.items.forEach {
                if (it.selectedProperty().get()) {
                    selected.add(it)
                    n += it.packagenameProperty().get().lines().size
                }
            }
            AppManager.enable(selected, n) {
                setPanels()
            }
        }
    }

    @FXML
    private fun secondSpaceButtonPressed(event: ActionEvent) {
        AppManager.user = if (secondSpaceButton.isSelected)
            10
        else 0
        AppManager.createTables()
    }

    @FXML
    private fun addAppsButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            val scene = Scene(FXMLLoader(javaClass.classLoader.getResource("AppAdder.fxml")).load<Parent>())
            val stage = Stage()
            stage.scene = scene
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.scene = scene
            stage.isResizable = false
            stage.showAndWait()
        }
    }

    @FXML
    private fun aboutMenuItemPressed(event: ActionEvent) {
        val linux = "linux" in System.getProperty("os.name").toLowerCase()
        val alert = Alert(AlertType.INFORMATION)
        alert.initStyle(StageStyle.UTILITY)
        alert.title = "About"
        alert.graphic = ImageView("icon.png")
        alert.headerText =
            "Xiaomi ADB/Fastboot Tools\nVersion $version\nCreated by Saki_EU"
        val vb = VBox()
        vb.alignment = Pos.CENTER
        val discord = Hyperlink("Xiaomi Community on Discord")
        discord.onAction = EventHandler {
            if (linux)
                Runtime.getRuntime().exec("xdg-open https://discord.gg/xiaomi")
            else Desktop.getDesktop().browse(URI("https://discord.gg/xiaomi"))
        }
        discord.font = Font(15.0)
        val twitter = Hyperlink("Saki_EU on Twitter")
        twitter.onAction = EventHandler {
            if (linux)
                Runtime.getRuntime().exec("xdg-open https://twitter.com/Saki_EU")
            else Desktop.getDesktop().browse(URI("https://twitter.com/Saki_EU"))
        }
        twitter.font = Font(15.0)
        val github = Hyperlink("Repository on GitHub")
        github.onAction = EventHandler {
            if (linux)
                Runtime.getRuntime().exec("xdg-open https://github.com/Saki-EU/XiaomiADBFastbootTools")
            else Desktop.getDesktop().browse(URI("https://github.com/Saki-EU/XiaomiADBFastbootTools"))
        }
        github.font = Font(15.0)
        vb.children.addAll(discord, twitter, github)
        alert.dialogPane.content = vb
        alert.isResizable = false
        alert.showAndWait()
    }
}

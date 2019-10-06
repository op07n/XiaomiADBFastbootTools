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
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.channels.Channels
import java.util.*
import kotlin.concurrent.thread

class MainController : Initializable {

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
    private lateinit var downloadProgress: Label
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
    @FXML
    private lateinit var downloaderPane: TitledPane

    private val version = "6.6.2"

    private var image: File? = null

    private val win = "win" in System.getProperty("os.name").toLowerCase()
    private val linux = "linux" in System.getProperty("os.name").toLowerCase()

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
                if (Device.camera2)
                    infoTextArea.appendText("Camera2:\t\t\tenabled")
            }
            Mode.FASTBOOT -> {
                infoTextArea.text = "Serial number:\t\t${Device.serial}\n" +
                        "Codename:\t\t${Device.codename}\n" +
                        "Bootloader:\t\t"
                if (Device.bootloader)
                    infoTextArea.appendText("unlocked")
                else infoTextArea.appendText("locked")
                if (Device.anti != -1)
                    infoTextArea.appendText("\nAnti version:\t\t${Device.anti}")
            }
            else -> infoTextArea.text = ""
        }
    }

    private fun versionToInt(ver: String): Int {
        val bits = "$ver.0".split('.')
        return bits[0].toInt() * 100 + bits[1].toInt() * 10 + bits[2].toInt()
    }

    private fun checkADBFastboot(): Boolean {
        return if (win) {
            when {
                (Command.setup(".\\bin\\")) -> true
                (Command.setup(".\\")) -> true
                (Command.setup("")) -> true
                else -> false
            }
        } else {
            when {
                (Command.setup("./bin/")) -> true
                (Command.setup("./")) -> true
                (Command.setup("")) -> true
                else -> false
            }
        }
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
        if (versionToInt(latest) > versionToInt(version))
            Platform.runLater {
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
                    if (linux)
                        Runtime.getRuntime().exec("xdg-open $link")
                    else Desktop.getDesktop().browse(URI(link))
                }
                download.font = Font(15.0)
                vb.children.add(download)
                alert.dialogPane.content = vb
                alert.showAndWait()
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
            outputTextArea.text = "Looking for devices...\n"
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
                outputTextArea.text = "Device connected in ADB mode!\n\n"
                if (Device.mode == Mode.ADB && (!Device.reinstaller || !Device.disabler))
                    outputTextArea.appendText("Note:\nThis device isn't fully supported by the App Manager.\nAs a result, some modules have been disabled.\n")
            }
            Device.readFastboot() -> {
                progressIndicator.isVisible = false
                codenameTextField.text = Device.codename
                outputTextArea.text = "Device connected in Fastboot mode!"
            }
            Device.mode == Mode.AUTH && "Unauthorised" !in outputTextArea.text -> {
                outputTextArea.text = "Unauthorised device found!\nPlease allow USB debugging!"
            }
            (Device.mode == Mode.ADB_ERROR || Device.mode == Mode.FB_ERROR) && "ERROR" !in outputTextArea.text -> {
                outputTextArea.text = "ERROR: Device cannot be loaded!"
            }
        }
        setUI()
    }

    private fun checkDevice() {
        thread(true, true) {
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
    }

    override fun initialize(url: URL, rb: ResourceBundle?) {
        outputTextArea.text = "Looking for devices...\n"
        progressIndicator.isVisible = true
        partitionComboBox.items.addAll(
            "boot", "cust", "modem", "persist", "recovery", "system"
        )
        scriptComboBox.items.addAll(
            "Clean install", "Clean install and lock", "Update"
        )
        branchComboBox.items.addAll(
            "Global Stable",
            "China Stable",
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

        Command.outputTextArea = outputTextArea
        Flasher.progressIndicator = progressIndicator
        Flasher.outputTextArea = outputTextArea
        ROMFlasher.progressBar = progressBar
        ROMFlasher.progressIndicator = progressIndicator
        ROMFlasher.outputTextArea = outputTextArea
        AppManager.uninstallerTableView = uninstallerTableView
        AppManager.reinstallerTableView = reinstallerTableView
        AppManager.disablerTableView = disablerTableView
        AppManager.enablerTableView = enablerTableView
        AppManager.progress = progressBar
        AppManager.progressInd = progressIndicator
        AppManager.outputTextArea = outputTextArea

        thread(true, true) {
            if (checkADBFastboot()) {
                checkVersion()
                checkDevice()
            } else {
                Platform.runLater {
                    val alert = Alert(AlertType.ERROR)
                    alert.title = "Fatal Error"
                    alert.headerText =
                        "ERROR: Can't find ADB/Fastboot!"
                    alert.showAndWait()
                    Platform.exit()
                }
            }
        }
    }

    private inline fun confirm(msg: String = "", func: () -> Unit) {
        val alert = Alert(AlertType.CONFIRMATION)
        alert.initStyle(StageStyle.UTILITY)
        alert.isResizable = false
        alert.headerText = "${msg.trim()}\nAre you sure you want to proceed?".trim()
        val yes = ButtonType("Yes")
        val no = ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE)
        alert.buttonTypes.setAll(yes, no)
        val result = alert.showAndWait()
        if (result.get() == yes)
            func()
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
                if (it.absolutePath.isNotBlank() && pcb.isNotBlank() && checkFastboot()) {
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
        romLabel.text = "-"
        ROMFlasher.directory = dc.showDialog((event.source as Node).scene.window)
        ROMFlasher.directory?.let {
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
                ROMFlasher.directory = null
            }
        }
    }

    @FXML
    private fun flashromButtonPressed(event: ActionEvent) {
        ROMFlasher.directory?.let {
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
            if (it.absolutePath.isNotBlank() && checkFastboot())
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
            confirm("All your data will be gone.") { Command.exec_displayed("fastboot erase userdata") }
    }

    @FXML
    private fun cachedataButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            confirm("All your data will be gone.") {
                Command.exec_displayed(
                    "fastboot erase cache",
                    "fastboot erase userdata"
                )
            }
    }

    @FXML
    private fun lockButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            confirm("Your partitions must be intact in order to successfully lock the bootloader.") {
                Command.exec_displayed("fastboot oem lock")
            }
    }

    @FXML
    private fun unlockButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            Command.exec_displayed("fastboot oem unlock")
    }

    private fun getLink(version: String, codename: String): String? {
        fun getLocation(codename: String, ending: String, region: String): String? {
            val huc =
                URL("http://update.miui.com/updates/v1/fullromdownload.php?d=$codename$ending&b=F&r=$region&n=").openConnection() as HttpURLConnection
            huc.requestMethod = "GET"
            huc.setRequestProperty("Referer", "http://en.miui.com/a-234.html")
            huc.instanceFollowRedirects = false
            try {
                huc.connect()
                huc.disconnect()
            } catch (e: IOException) {
                return null
            }
            return huc.getHeaderField("Location")
        }
        when (version) {
            "Global Stable" ->
                return getLocation(codename, "_global", "global")
            "China Stable" ->
                return getLocation(codename, "", "cn")
            "EEA Stable" ->
                return getLocation(codename, "_eea_global", "eea")
            "Russia Stable" -> {
                for (region in arrayOf("ru", "global")) {
                    val link = getLocation(codename, "_ru_global", region)
                    if (link != null && "bigota" in link)
                        return link
                }
                return null
            }
            else -> {
                for (region in arrayOf("in", "global"))
                    for (ending in arrayOf("_in_global", "_india_global", "_global")) {
                        if (region == "global" && ending == "_global")
                            break
                        val link = getLocation(codename, ending, region)
                        if (link != null && "bigota" in link)
                            return link
                    }
                return null
            }
        }
    }

    @FXML
    private fun getlinkButtonPressed(event: ActionEvent) {
        branchComboBox.value?.let {
            if (codenameTextField.text.isNotBlank()) {
                outputTextArea.appendText("\nLooking for $it...\n")
                thread(true, true) {
                    val link = getLink(it, codenameTextField.text.trim())
                    Platform.runLater {
                        if (link != null && "bigota" in link) {
                            versionLabel.text = link.substringAfter(".com/").substringBefore('/')
                            outputTextArea.appendText("$link\nLink copied to clipboard!\n")
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(link), null)
                        } else {
                            versionLabel.text = "-"
                            outputTextArea.appendText("Link not found!\n")
                        }
                    }
                }
            }
        }
    }

    @FXML
    private fun downloadromButtonPressed(event: ActionEvent) {
        branchComboBox.value?.let {
            if (codenameTextField.text.isNotBlank()) {
                outputTextArea.appendText("\nLooking for $it...\n")
                val link = getLink(it, codenameTextField.text.trim())
                if (link != null && "bigota" in link) {
                    val dc = DirectoryChooser()
                    dc.title = "Select the download location of the Fastboot ROM"
                    dc.showDialog((event.source as Node).scene.window)?.let {
                        versionLabel.text = link.substringAfter(".com/").substringBefore('/')
                        outputTextArea.appendText("Starting download...\n")
                        downloaderPane.isDisable = true
                        thread(true, true) {
                            var complete = false
                            val url = URL(link)
                            val size = url.openConnection().contentLengthLong * 1.0
                            val file = File(it, link.substringAfterLast('/'))
                            thread(true, true) {
                                var prev = 0L
                                while (!complete) {
                                    val length = file.length()
                                    val diff = (length - prev) / 1000.0
                                    val speed = if (diff < 1000.0)
                                        "${diff.toString().take(5)} KB/s"
                                    else "${(diff / 1000.0).toString().take(5)} MB/s"
                                    prev = length
                                    Platform.runLater {
                                        downloadProgress.text =
                                            "${(file.length() / size * 100.0).toString().take(5)} %\t\t$speed"
                                    }
                                    Thread.sleep(1000)
                                }
                            }
                            FileOutputStream(file).channel.transferFrom(
                                Channels.newChannel(url.openStream()),
                                0,
                                Long.MAX_VALUE
                            )
                            complete = true
                            Platform.runLater {
                                outputTextArea.appendText("Download complete!\n\n")
                                downloadProgress.text = ""
                                downloaderPane.isDisable = false
                            }
                        }
                    }
                } else {
                    versionLabel.text = "-"
                    outputTextArea.appendText("Link not found!\n\n")
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
            confirm {
                setPanels()
                val selected = FXCollections.observableArrayList<App>()
                var n = 0
                uninstallerTableView.items.forEach {
                    if (it.selectedProperty().get()) {
                        selected.add(it)
                        n += it.packagenameProperty().get().trim().lines().size
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
                    n += it.packagenameProperty().get().trim().lines().size
                }
            }
            AppManager.reinstall(selected, n) {
                setPanels()
            }
        }
    }

    @FXML
    private fun disableButtonPressed(event: ActionEvent) {
        if (isAppSelected(disablerTableView.items) && checkADB()) {
            setPanels()
            val selected = FXCollections.observableArrayList<App>()
            var n = 0
            disablerTableView.items.forEach {
                if (it.selectedProperty().get()) {
                    selected.add(it)
                    n += it.packagenameProperty().get().trim().lines().size
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
                    n += it.packagenameProperty().get().trim().lines().size
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
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.scene = scene
            stage.isResizable = false
            stage.showAndWait()
        }
    }

    @FXML
    private fun aboutMenuItemPressed(event: ActionEvent) {
        val alert = Alert(AlertType.INFORMATION)
        alert.initStyle(StageStyle.UTILITY)
        alert.title = "About"
        alert.graphic = ImageView("icon.png")
        alert.headerText =
            "Xiaomi ADB/Fastboot Tools\nVersion $version\nCreated by Saki_EU\n\n" +
                    "ADB/Fastboot\n${Command.exec("adb --version").lines()[1]}"
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

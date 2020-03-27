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
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.*
import kotlinx.coroutines.*
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

    private val version = "6.9.2"
    private val command = Command()
    private var image: File? = null
    private var romDirectory: File? = null
    private var loading = false

    companion object {
        val dir = File(System.getProperty("user.home"), "XiaomiADBFastbootTools")
        val workingDir = System.getProperty("user.dir")
        val win = "win" in System.getProperty("os.name").toLowerCase()
        val linux = "linux" in System.getProperty("os.name").toLowerCase()
    }

    private fun setPanels(mode: Mode = Device.mode) {
        when (mode) {
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
                        "Codename:\t\t${Device.codename}\n"
                if (Device.bootloader)
                    infoTextArea.appendText("Bootloader:\t\tunlocked\n")
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

    private fun checkADB(): Boolean {
        if (!Device.readADB()) {
            checkDevice()
            return false
        }
        setUI()
        return true
    }

    private fun checkFastboot(): Boolean {
        if (!Device.readFastboot()) {
            checkDevice()
            return false
        }
        setUI()
        return true
    }

    private fun checkDevice() {
        if (!loading) {
            loading = true
            GlobalScope.launch {
                while (loading) {
                    withContext(Dispatchers.Main) {
                        if ("Looking for devices" !in outputTextArea.text) {
                            outputTextArea.text = "Looking for devices...\n"
                            reloadMenuItem.isDisable = true
                            progressIndicator.isVisible = true
                        }
                        when {
                            Device.readADB() -> {
                                progressIndicator.isVisible = false
                                val support = command.exec("adb shell cmd package install-existing xaft")
                                Device.reinstaller = !("not found" in support || "Unknown command" in support)
                                Device.disabler = "enabled" in command.exec("adb shell pm enable com.android.settings")
                                AppManager.readPotentialApps()
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
                                outputTextArea.text = "Device connected in ADB mode!\n"
                                if (Device.mode == Mode.ADB && (!Device.reinstaller || !Device.disabler))
                                    outputTextArea.appendText("Note:\nThis device isn't fully supported by the App Manager.\nAs a result, some modules have been disabled.\n")
                                loading = false
                            }
                            Device.readFastboot() -> {
                                progressIndicator.isVisible = false
                                codenameTextField.text = Device.codename
                                outputTextArea.text = "Device connected in Fastboot mode!\n"
                                loading = false
                            }
                            Device.mode == Mode.AUTH && "Unauthorised" !in outputTextArea.text -> {
                                outputTextArea.text = "Unauthorised device found!\nPlease allow USB debugging!\n"
                            }
                            (Device.mode == Mode.ADB_ERROR || Device.mode == Mode.FB_ERROR) && "loaded" !in outputTextArea.text -> {
                                outputTextArea.text = "ERROR: Device cannot be loaded!\n"
                            }
                        }
                        setUI()
                    }
                    try {
                        delay(1000)
                    } catch (ie: InterruptedException) {
                        break
                    }
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
            "China Stable",
            "EEA Stable",
            "Global Stable",
            "India Stable",
            "Indonesia Stable",
            "Russia Stable"
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
        Command.progressIndicator = progressIndicator
        Command.progressBar = progressBar
        AppManager.uninstallerTableView = uninstallerTableView
        AppManager.reinstallerTableView = reinstallerTableView
        AppManager.disablerTableView = disablerTableView
        AppManager.enablerTableView = enablerTableView
        AppManager.progress = progressBar
        AppManager.progressInd = progressIndicator

        GlobalScope.launch(Dispatchers.IO) {
            if (command.check(win)) {
                try {
                    val link =
                        URL("https://api.github.com/repos/Szaki/XiaomiADBFastbootTools/releases/latest").readText()
                            .substringAfter("\"html_url\":\"").substringBefore('"')
                    val latest = link.substringAfterLast('/')
                    if (latest > version)
                        withContext(Dispatchers.Main) {
                            Alert(AlertType.INFORMATION).apply {
                                initStyle(StageStyle.UTILITY)
                                title = "New version available!"
                                graphic = ImageView("mitu.png")
                                headerText =
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
                                dialogPane.content = vb
                                showAndWait()
                            }
                        }
                } catch (ex: Exception) {
                    // OK
                }
                checkDevice()
            } else {
                withContext(Dispatchers.Main) {
                    Alert(AlertType.ERROR).apply {
                        title = "Fatal Error"
                        headerText =
                            "ERROR: Cannot find ADB/Fastboot!"
                        showAndWait()
                    }
                    Platform.exit()
                }
            }
        }
    }

    private inline fun confirm(msg: String = "", func: () -> Unit) {
        Alert(AlertType.CONFIRMATION).apply {
            initStyle(StageStyle.UTILITY)
            isResizable = false
            headerText = "${msg.trim()}\nAre you sure you want to proceed?".trim()
            val yes = ButtonType("Yes")
            val no = ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE)
            buttonTypes.setAll(yes, no)
            val result = showAndWait()
            if (result.get() == yes)
                func()
        }
    }

    private fun checkCamera2(): Boolean = "1" in command.exec("adb shell getprop persist.camera.HAL3.enabled")

    private fun checkEIS(): Boolean = "1" in command.exec("adb shell getprop persist.camera.eis.enable")

    @FXML
    private fun disableCamera2ButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (Device.mode != Mode.RECOVERY) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            command.exec("adb shell setprop persist.camera.HAL3.enabled 0")
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
            command.exec("adb shell setprop persist.camera.HAL3.enabled 1")
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
            command.exec("adb shell setprop persist.camera.eis.enable 0")
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
            command.exec("adb shell setprop persist.camera.eis.enable 1")
            outputTextArea.text = if (checkEIS())
                "EIS enabled!"
            else "ERROR: Couldn't enable EIS!"
        }
    }

    @FXML
    private fun openButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            val scene = Scene(FXMLLoader(javaClass.classLoader.getResource("FileExplorer.fxml")).load())
            Stage().apply {
                this.scene = scene
                initModality(Modality.APPLICATION_MODAL)
                title = "File Explorer"
                isResizable = false
                showAndWait()
            }
        }
    }

    @FXML
    private fun applyDpiButtonPressed(event: ActionEvent) {
        if (dpiTextField.text.isNotBlank() && checkADB()) {
            val attempt = command.execDisplayed("adb shell wm density ${dpiTextField.text.trim()}")
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
            val attempt = command.execDisplayed("adb shell wm density reset")
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
        if (widthTextField.text.isNotBlank() && heightTextField.text.isNotBlank() && checkADB()) {
            val attempt =
                command.execDisplayed("adb shell wm size ${widthTextField.text.trim()}x${heightTextField.text.trim()}")
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
            val attempt = command.execDisplayed("adb shell wm size reset")
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
                command.execDisplayed("adb shell getprop")
            Mode.FASTBOOT -> if (checkFastboot())
                command.execDisplayed("fastboot getvar all")
            else -> return
        }
    }

    @FXML
    private fun savePropertiesMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB()) {
                val props = command.exec("adb shell getprop")
                FileChooser().apply {
                    extensionFilters.add(FileChooser.ExtensionFilter("Text File", "*"))
                    title = "Save properties"
                    showSaveDialog((event.target as MenuItem).parentPopup.ownerWindow)?.let {
                        try {
                            it.writeText(props)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            ExceptionAlert(ex)
                        }
                    }
                }
            }
            Mode.FASTBOOT -> if (checkFastboot()) {
                val props = command.exec("fastboot getvar all")
                FileChooser().apply {
                    extensionFilters.add(FileChooser.ExtensionFilter("Text File", "*"))
                    title = "Save properties"
                    showSaveDialog((event.target as MenuItem).parentPopup.ownerWindow)?.let {
                        try {
                            it.writeText(props)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            ExceptionAlert(ex)
                        }
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
            if ("FAILED" in command.exec("fastboot oem ignore_anti")) {
                if ("FAILED" in command.exec("fastboot flash antirbpass dummy.img")) {
                    outputTextArea.text = "Couldn't disable anti-rollback safeguard!"
                } else outputTextArea.text = "Anti-rollback safeguard disabled!"
            } else outputTextArea.text = "Anti-rollback safeguard disabled!"
            dummy.delete()
        }
    }

    @FXML
    private fun browseimageButtonPressed(event: ActionEvent) {
        FileChooser().apply {
            extensionFilters.add(FileChooser.ExtensionFilter("Image File", "*.*"))
            title = "Select an image"
            image = showOpenDialog((event.source as Node).scene.window)
            imageLabel.text = image?.name
        }
    }

    @FXML
    private fun flashimageButtonPressed(event: ActionEvent) {
        image?.let {
            partitionComboBox.value?.let { pcb ->
                if (it.absolutePath.isNotBlank() && pcb.isNotBlank() && checkFastboot()) {
                    confirm {
                        if (autobootCheckBox.isSelected && pcb.trim() == "recovery")
                            command.exec("fastboot flash ${pcb.trim()}", "fastboot boot", image = it)
                        else command.exec("fastboot flash ${pcb.trim()}", image = it)
                    }
                }
            }
        }
    }

    @FXML
    private fun browseromButtonPressed(event: ActionEvent) {
        DirectoryChooser().apply {
            title = "Select the root directory of a Fastboot ROM"
            romLabel.text = "-"
            romDirectory = showDialog((event.source as Node).scene.window)
        }
        romDirectory?.let { dir ->
            when {
                ' ' in dir.absolutePath -> {
                    outputTextArea.text = "ERROR: Space found in the pathname!"
                    romDirectory = null
                }
                "images" in dir.list()!! -> {
                    romLabel.text = dir.name
                    outputTextArea.text = "Fastboot ROM found!"
                    dir.listFiles()?.forEach {
                        if (!it.isDirectory)
                            it.setExecutable(true, false)
                    }
                }
                else -> {
                    outputTextArea.text = "ERROR: Fastboot ROM not found!"
                    romDirectory = null
                }
            }
        }
    }

    @FXML
    private fun flashromButtonPressed(event: ActionEvent) {
        romDirectory?.let { dir ->
            scriptComboBox.value?.let { scb ->
                if (checkFastboot())
                    confirm {
                        setPanels(Mode.NONE)
                        when (scb) {
                            "Clean install" -> ROMFlasher(dir).flash("flash_all")
                            "Clean install and lock" -> ROMFlasher(dir).flash("flash_all_lock")
                            "Update" -> ROMFlasher(dir).flash(
                                dir.list()?.find { "flash_all_except" in it }?.substringBefore(
                                    '.'
                                )
                            )
                        }
                    }
            }
        }
    }

    @FXML
    private fun bootButtonPressed(event: ActionEvent) {
        image?.let {
            if (it.absolutePath.isNotBlank() && checkFastboot())
                command.exec("fastboot boot", image = it)
        }
    }

    @FXML
    private fun cacheButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            command.execDisplayed("fastboot erase cache")
    }

    @FXML
    private fun dataButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            confirm("All your data will be gone.") { command.execDisplayed("fastboot erase userdata") }
    }

    @FXML
    private fun cachedataButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            confirm("All your data will be gone.") {
                command.execDisplayed(
                    "fastboot erase cache",
                    "fastboot erase userdata"
                )
            }
    }

    @FXML
    private fun lockButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            confirm("Your partitions must be intact in order to successfully lock the bootloader.") {
                command.execDisplayed("fastboot oem lock")
            }
    }

    @FXML
    private fun unlockButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            command.execDisplayed("fastboot oem unlock")
    }

    private fun getLink(version: String, codename: String): String? {
        fun getLocation(codename: String, ending: String, region: String): String? {
            (URL("http://update.miui.com/updates/v1/fullromdownload.php?d=$codename$ending&b=F&r=$region&n=").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Referer", "http://en.miui.com/a-234.html")
                instanceFollowRedirects = false
                try {
                    connect()
                    disconnect()
                } catch (e: IOException) {
                    return null
                }
                return getHeaderField("Location")
            }
        }
        when (version) {
            "China Stable" ->
                return getLocation(codename, "", "cn")
            "EEA Stable" ->
                return getLocation(codename, "_eea_global", "eea")
            "Russia Stable" -> {
                arrayOf("ru", "global").forEach {
                    val link = getLocation(codename, "_ru_global", it)
                    if (link != null && "bigota" in link)
                        return link
                }
                return null
            }
            "Indonesia Stable" ->
                return getLocation(codename, "_id_global", "global")
            "India Stable" -> {
                arrayOf("in", "global").forEach {
                    for (ending in arrayOf("_in_global", "_india_global", "_global")) {
                        if (it == "global" && ending == "_global")
                            break
                        val link = getLocation(codename, ending, it)
                        if (link != null && "bigota" in link)
                            return link
                    }
                }
                return null
            }
            else -> return getLocation(codename, "_global", "global")
        }
    }

    @FXML
    private fun getlinkButtonPressed(event: ActionEvent) {
        branchComboBox.value?.let {
            if (codenameTextField.text.isNotBlank()) {
                outputTextArea.appendText("\nLooking for $it...\n")
                progressIndicator.isVisible = true
                GlobalScope.launch {
                    val link = getLink(it, codenameTextField.text.trim())
                    withContext(Dispatchers.Main) {
                        if (link != null && "bigota" in link) {
                            versionLabel.text = link.substringAfter(".com/").substringBefore('/')
                            progressIndicator.isVisible = false
                            outputTextArea.appendText("$link\nLink copied to clipboard!\n")
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(link), null)
                        } else {
                            versionLabel.text = "-"
                            progressIndicator.isVisible = false
                            outputTextArea.appendText("Link not found!\n")
                        }
                    }
                }
            }
        }
    }

    @FXML
    private fun downloadromButtonPressed(event: ActionEvent) {
        branchComboBox.value?.let { branch ->
            if (codenameTextField.text.isNotBlank()) {
                val dc = DirectoryChooser()
                dc.title = "Select the download location of the Fastboot ROM"
                dc.showDialog((event.source as Node).scene.window)?.let {
                    outputTextArea.appendText("\nLooking for $branch...\n")
                    progressIndicator.isVisible = true
                    GlobalScope.launch(Dispatchers.IO) {
                        val link = getLink(branch, codenameTextField.text.trim())
                        if (link != null && "bigota" in link) {
                            withContext(Dispatchers.Main) {
                                versionLabel.text = link.substringAfter(".com/").substringBefore('/')
                                outputTextArea.appendText("Starting download...\n")
                                downloaderPane.isDisable = true
                            }
                            var complete = false
                            val url = URL(link)
                            val size = url.openConnection().contentLengthLong * 1.0
                            val file = File(it, link.substringAfterLast('/'))
                            launch {
                                var prev = 0L
                                while (!complete) {
                                    val length = file.length()
                                    val diff = (length - prev) / 1000.0
                                    val speed = if (diff < 1000.0)
                                        "${diff.toString().take(5)} KB/s"
                                    else "${(diff / 1000.0).toString().take(4)} MB/s"
                                    prev = length
                                    withContext(Dispatchers.Main) {
                                        downloadProgress.text =
                                            "${(file.length() / size * 100.0).toString().take(4)} %\t\t$speed"
                                    }
                                    delay(1000)
                                }
                            }
                            FileOutputStream(file).channel.transferFrom(
                                Channels.newChannel(url.openStream()),
                                0,
                                Long.MAX_VALUE
                            )
                            complete = true
                            withContext(Dispatchers.Main) {
                                progressIndicator.isVisible = false
                                outputTextArea.appendText("Download complete!\n\n")
                                downloadProgress.text = ""
                                downloaderPane.isDisable = false
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                versionLabel.text = "-"
                                progressIndicator.isVisible = false
                                outputTextArea.appendText("Link not found!\n\n")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun reload() {
        outputTextArea.clear()
        infoTextArea.clear()
        checkDevice()
    }

    @FXML
    private fun systemMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB()) {
                command.exec("adb reboot")
                reload()
            }
            Mode.FASTBOOT -> if (checkFastboot()) {
                command.exec("fastboot reboot")
                reload()
            }
            else -> return
        }
    }

    @FXML
    private fun recoveryMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB()) {
                command.exec("adb reboot recovery")
                reload()
            }
            else -> return
        }
    }

    @FXML
    private fun fastbootMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB()) {
                command.exec("adb reboot bootloader")
                reload()
            }
            Mode.FASTBOOT -> if (checkFastboot()) {
                command.exec("fastboot reboot bootloader")
                reload()
            }
            else -> return
        }
    }

    @FXML
    private fun edlMenuItemPressed(event: ActionEvent) {
        when (Device.mode) {
            Mode.ADB, Mode.RECOVERY -> if (checkADB()) {
                command.exec("adb reboot edl")
                reload()
            }
            Mode.FASTBOOT -> if (checkFastboot()) {
                command.exec("fastboot oem edl")
                reload()
            }
            else -> return
        }
    }

    @FXML
    private fun reloadMenuItemPressed(event: ActionEvent) = reload()

    private fun isAppSelected(list: ObservableList<App>): Boolean {
        return if (list.isNotEmpty()) {
            list.any { it.selectedProperty().get() }
        } else false
    }

    private inline fun executeAppManagerAction(
        items: ObservableList<App>,
        func: (ObservableList<App>, Int, () -> Unit) -> Unit
    ) {
        if (isAppSelected(items) && checkADB())
            confirm {
                setPanels(Mode.NONE)
                val selected = FXCollections.observableArrayList<App>()
                var n = 0
                items.forEach {
                    if (it.selectedProperty().get()) {
                        selected.add(it)
                        n += it.packagenameProperty().get().trim().lines().size
                    }
                }
                func(selected, n) {
                    setPanels()
                }
            }
    }

    @FXML
    private fun uninstallButtonPressed(event: ActionEvent) {
        executeAppManagerAction(uninstallerTableView.items, AppManager::uninstall)
    }

    @FXML
    private fun reinstallButtonPressed(event: ActionEvent) {
        executeAppManagerAction(reinstallerTableView.items, AppManager::reinstall)
    }

    @FXML
    private fun disableButtonPressed(event: ActionEvent) {
        executeAppManagerAction(disablerTableView.items, AppManager::disable)
    }

    @FXML
    private fun enableButtonPressed(event: ActionEvent) {
        executeAppManagerAction(enablerTableView.items, AppManager::enable)
    }

    @FXML
    private fun secondSpaceButtonPressed(event: ActionEvent) {
        AppManager.apply {
            user = if (secondSpaceButton.isSelected)
                10
            else 0
            createTables()
        }
    }

    @FXML
    private fun addAppsButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            val scene = Scene(FXMLLoader(javaClass.classLoader.getResource("AppAdder.fxml")).load())
            Stage().apply {
                initModality(Modality.APPLICATION_MODAL)
                this.scene = scene
                isResizable = false
                showAndWait()
            }
        }
    }

    @FXML
    private fun aboutMenuItemPressed(event: ActionEvent) {
        Alert(AlertType.INFORMATION).apply {
            initStyle(StageStyle.UTILITY)
            title = "About"
            graphic = ImageView("icon.png")
            headerText =
                "Xiaomi ADB/Fastboot Tools\nVersion $version\nCreated by Szaki\n\n" +
                        "SDK Platform Tools\n${command.exec("adb --version").lines()[1]}"
            val vb = VBox()
            vb.alignment = Pos.CENTER
            val discord = Hyperlink("Xiaomi Community on Discord")
            discord.onAction = EventHandler {
                if (linux)
                    Runtime.getRuntime().exec("xdg-open https://discord.gg/xiaomi")
                else Desktop.getDesktop().browse(URI("https://discord.gg/xiaomi"))
            }
            discord.font = Font(15.0)
            val twitter = Hyperlink("Szaki on Twitter")
            twitter.onAction = EventHandler {
                if (linux)
                    Runtime.getRuntime().exec("xdg-open https://twitter.com/Szaki_EU")
                else Desktop.getDesktop().browse(URI("https://twitter.com/Szaki_EU"))
            }
            twitter.font = Font(15.0)
            val github = Hyperlink("Repository on GitHub")
            github.onAction = EventHandler {
                if (linux)
                    Runtime.getRuntime().exec("xdg-open https://github.com/Szaki/XiaomiADBFastbootTools")
                else Desktop.getDesktop().browse(URI("https://github.com/Szaki/XiaomiADBFastbootTools"))
            }
            github.font = Font(15.0)
            vb.children.addAll(discord, twitter, github)
            dialogPane.content = vb
            isResizable = false
            showAndWait()
        }
    }
}

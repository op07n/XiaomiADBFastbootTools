import javafx.application.Platform
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
import java.io.FileWriter
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.*

class MainController : Initializable {

    @FXML
    private lateinit var rebootMenu: Menu
    @FXML
    private lateinit var recoveryMenuItem: MenuItem
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
    private lateinit var uncustomappTextField: TextField
    @FXML
    private lateinit var recustomappTextField: TextField
    @FXML
    private lateinit var dpiTextField: TextField
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
    private lateinit var installerPane: TabPane
    @FXML
    private lateinit var fileExplorerPane: TitledPane
    @FXML
    private lateinit var camera2Pane: TitledPane
    @FXML
    private lateinit var devicepropertiesPane: TitledPane
    @FXML
    private lateinit var flasherPane: TitledPane
    @FXML
    private lateinit var wiperPane: TitledPane
    @FXML
    private lateinit var oemPane: TitledPane
    @FXML
    private lateinit var dpiPane: TitledPane

    private var image: File? = null
    private var rom: File? = null
    private val comm = Command()
    private val device = Device()
    private lateinit var displayedcomm: Command
    private lateinit var flasher: Flasher
    private lateinit var installer: Installer

    companion object {
        val version = "6.1"
        lateinit var thread: Thread
    }

    fun setUI() {
        when (device.mode) {
            0 -> {
                infoTextArea.text = ""
                installerPane.isDisable = true
                camera2Pane.isDisable = true
                fileExplorerPane.isDisable = true
                devicepropertiesPane.isDisable = true
                dpiPane.isDisable = true
                flasherPane.isDisable = true
                wiperPane.isDisable = true
                oemPane.isDisable = true
                rebootMenu.isDisable = true
                recoveryMenuItem.isDisable = true
            }
            1 -> {
                infoTextArea.text = ""
                infoTextArea.appendText("Serial number:\t\t${device.serial}\n")
                infoTextArea.appendText("Codename:\t\t${device.codename}\n")
                infoTextArea.appendText("Bootloader:\t\t")
                if (device.bootloader)
                    infoTextArea.appendText("unlocked\n")
                else infoTextArea.appendText("locked\n")
                codenameTextField.text = device.codename
                if (!device.recovery) {
                    if (device.dpi != -1)
                        dpiTextField.text = device.dpi.toString()
                    else dpiTextField.text = "ERROR"
                }

                installerPane.isDisable = false
                camera2Pane.isDisable = false
                fileExplorerPane.isDisable = false
                devicepropertiesPane.isDisable = false
                dpiPane.isDisable = device.recovery
                flasherPane.isDisable = true
                wiperPane.isDisable = true
                oemPane.isDisable = true
                rebootMenu.isDisable = false
                recoveryMenuItem.isDisable = false
            }
            2 -> {
                infoTextArea.text = ""
                infoTextArea.appendText("Serial number:\t\t${device.serial}\n")
                infoTextArea.appendText("Codename:\t\t${device.codename}\n")
                infoTextArea.appendText("Bootloader:\t\t")
                if (device.bootloader)
                    infoTextArea.appendText("unlocked\n")
                else infoTextArea.appendText("locked\n")
                if (device.anti != -1)
                    infoTextArea.appendText("Anti version:\t\t${device.anti}\n")
                codenameTextField.text = device.codename

                installerPane.isDisable = true
                camera2Pane.isDisable = true
                fileExplorerPane.isDisable = true
                devicepropertiesPane.isDisable = true
                dpiPane.isDisable = true
                flasherPane.isDisable = false
                wiperPane.isDisable = false
                oemPane.isDisable = false
                rebootMenu.isDisable = false
                recoveryMenuItem.isDisable = true
            }
        }
    }

    fun checkADB(): Boolean {
        val adb = device.readADB()
        setUI()
        return adb
    }

    fun checkFastboot(): Boolean {
        val fb = device.readFastboot()
        setUI()
        return fb
    }

    fun checkDevice() {
        thread = Thread {
            comm.exec("adb start-server")
            while (File(System.getProperty("user.home") + "/temp").exists()) {
                if (device.mode == 0) {
                    if (!outputTextArea.text.contains("Looking"))
                        outputTextArea.text = "Looking for devices..."
                    progressIndicator.isVisible = true
                    if (device.readADB()) {
                        progressIndicator.isVisible = false
                        outputTextArea.text = "Device found in ADB mode!"
                        installer.loadApps(device)
                        Platform.runLater { setUI() }
                        continue
                    }
                    if (device.readFastboot()) {
                        progressIndicator.isVisible = false
                        outputTextArea.text = "Device found in Fastboot mode!"
                        Platform.runLater { setUI() }
                        continue
                    }
                    Platform.runLater { setUI() }
                    if (device.auth && !outputTextArea.text.contains("Unauthorised"))
                        outputTextArea.text = "Unauthorised device found!\nPlease allow USB debugging!"
                }
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

    override fun initialize(url: URL, rb: ResourceBundle?) {
        outputTextArea.text = "Looking for devices..."
        progressIndicator.isVisible = true
        checkDevice()
        partitionComboBox.items.addAll(
            "boot", "cust", "modem", "persist", "recovery", "system"
        )
        scriptComboBox.items.addAll(
            "Clean install", "Clean install and lock", "Update"
        )
        branchComboBox.items.addAll(
            "Global Stable", "Global Developer", "China Stable", "China Developer"
        )

        uncheckTableColumn.cellValueFactory = PropertyValueFactory("selected")
        uncheckTableColumn.setCellFactory { CheckBoxTableCell() }
        unappTableColumn.cellValueFactory = PropertyValueFactory("appname")
        unpackageTableColumn.cellValueFactory = PropertyValueFactory("packagename")
        recheckTableColumn.cellValueFactory = PropertyValueFactory("selected")
        recheckTableColumn.setCellFactory { CheckBoxTableCell() }
        reappTableColumn.cellValueFactory = PropertyValueFactory("appname")
        repackageTableColumn.cellValueFactory = PropertyValueFactory("packagename")
        uninstallerTableView.columns.setAll(uncheckTableColumn, unappTableColumn, unpackageTableColumn)
        reinstallerTableView.columns.setAll(recheckTableColumn, reappTableColumn, repackageTableColumn)

        displayedcomm = Command(outputTextArea)
        flasher = Flasher(outputTextArea, progressIndicator)
        installer =
            Installer(uninstallerTableView, reinstallerTableView, progressBar, progressIndicator, outputTextArea)
    }

    @FXML
    private fun reboottwrpButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (device.recovery) {
                outputTextArea.text = "Device already in recovery mode!"
            } else {
                comm.exec("adb reboot recovery")
                checkADB()
            }
        }
    }

    private fun checkcamera2(): Boolean = comm.exec("adb shell getprop persist.camera.HAL3.enabled").contains("1")

    private fun checkEIS(): Boolean = comm.exec("adb shell getprop persist.camera.eis.enable").contains("1")

    @FXML
    private fun disableButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (!device.recovery) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            comm.exec("adb shell setprop persist.camera.HAL3.enabled 0")
            if (!checkcamera2())
                outputTextArea.text = "Camera2 disabled!"
            else outputTextArea.text = "ERROR: Couldn't disable Camera2!"
        }
    }

    @FXML
    private fun enableButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (!device.recovery) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            comm.exec("adb shell setprop persist.camera.HAL3.enabled 1")
            if (checkcamera2())
                outputTextArea.text = "Camera2 enabled!"
            else outputTextArea.text = "ERROR: Couldn't enable Camera2!"
        }
    }

    @FXML
    private fun disableEISButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (!device.recovery) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            comm.exec("adb shell setprop persist.camera.eis.enable 0")
            if (!checkEIS())
                outputTextArea.text = "EIS disabled!"
            else outputTextArea.text = "ERROR: Couldn't disable EIS!"
        }
    }

    @FXML
    private fun enableEISButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (!device.recovery) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            comm.exec("adb shell setprop persist.camera.eis.enable 1")
            if (checkEIS())
                outputTextArea.text = "EIS enabled!"
            else outputTextArea.text = "ERROR: Couldn't enable EIS!"
        }
    }

    @FXML
    private fun openButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            val fxmlLoader = FXMLLoader(javaClass.classLoader.getResource("FileExplorer.fxml"))
            val parent = fxmlLoader.load<Parent>()
            fxmlLoader.getController<FileExplorerController>().device = device
            val scene = Scene(parent)
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
    private fun dpiButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            val attempt = displayedcomm.exec("adb shell wm density ${dpiTextField.text.trim()}")
            when {
                attempt.contains("permission") -> {
                    outputTextArea.text = "ERROR: Please allow USB debugging (Security settings)!"
                }
                attempt.contains("bad number") -> {
                    outputTextArea.text = "ERROR: Invalid value!"
                }
                attempt.isEmpty() -> {
                    outputTextArea.text = "Done!\nIf you notice any weird behaviour, reboot the device."
                }
                else -> {
                    outputTextArea.text = "ERROR: Unexpected result!\n\n$attempt"
                }
            }
        }
    }

    @FXML
    private fun readpropertiesButtonPressed(event: ActionEvent) {
        if (checkADB())
            displayedcomm.exec("adb shell getprop")
    }

    @FXML
    private fun savepropertiesButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            val fc = FileChooser()
            val fileExtensions = FileChooser.ExtensionFilter("Text File (.txt)", "*.txt")
            fc.extensionFilters.add(fileExtensions)
            fc.title = "Save properties"
            val f = fc.showSaveDialog((event.source as Node).scene.window)
            if (f != null) {
                try {
                    val fw = FileWriter(f)
                    fw.write(comm.exec("adb shell getprop"))
                    fw.flush()
                    fw.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    ExceptionAlert(ex)
                }
            }
        }
    }

    @FXML
    private fun antirbButtonPressed(event: ActionEvent) {
        if (checkFastboot()) {
            val result = comm.exec("fastboot flash antirbpass dummy.img")
            if (result.contains("FAILED"))
                displayedcomm.exec("fastboot oem ignore_anti")
            else outputTextArea.text = result
        }
    }

    @FXML
    private fun browseimageButtonPressed(event: ActionEvent) {
        val fc = FileChooser()
        val fileExtensions = FileChooser.ExtensionFilter("Image File", "*.*")
        fc.extensionFilters.add(fileExtensions)
        fc.title = "Select an image"
        image = fc.showOpenDialog((event.source as Node).scene.window)
        imageLabel.text = image?.name
    }

    @FXML
    private fun flashimageButtonPressed(event: ActionEvent) {
        if (image != null && partitionComboBox.value != null && image!!.absolutePath.isNotEmpty() && partitionComboBox.value.trim().isNotEmpty() && checkFastboot()) {
            if (autobootCheckBox.isSelected && partitionComboBox.value.trim() == "recovery")
                flasher.exec(image, "fastboot flash ${partitionComboBox.value.trim()}", "fastboot boot")
            else flasher.exec(image, "fastboot flash ${partitionComboBox.value.trim()}")
        }
    }

    @FXML
    private fun browseromButtonPressed(event: ActionEvent) {
        val dc = DirectoryChooser()
        dc.title = "Select the root directory of a Fastboot ROM"
        rom = dc.showDialog((event.source as Node).scene.window)
        outputTextArea.text = ""
        romLabel.text = "-"
        if (rom != null) {
            if (File(rom, "images").exists()) {
                romLabel.text = rom?.name
                outputTextArea.text = "Fastboot ROM found!"
                File(rom, "flash_all.sh").setExecutable(true, false)
                File(rom, "flash_all_lock.sh").setExecutable(true, false)
                File(rom, "flash_all_except_storage.sh").setExecutable(true, false)
                File(rom, "flash_all_except_data.sh").setExecutable(true, false)
                File(rom, "flash_all_except_data_storage.sh").setExecutable(true, false)
            } else {
                outputTextArea.text = "ERROR: Fastboot ROM not found!"
                romLabel.text = "-"
                rom = null
            }
        }
    }

    @FXML
    private fun flashromButtonPressed(event: ActionEvent) {
        if (rom != null && scriptComboBox.value != null && checkFastboot()) {
            val rf = ROMFlasher(progressBar, progressIndicator, outputTextArea, rom!!)
            infoTextArea.text = ""
            installerPane.isDisable = true
            camera2Pane.isDisable = true
            fileExplorerPane.isDisable = true
            devicepropertiesPane.isDisable = true
            dpiPane.isDisable = true
            flasherPane.isDisable = true
            wiperPane.isDisable = true
            oemPane.isDisable = true
            rebootMenu.isDisable = true
            recoveryMenuItem.isDisable = true
            when (scriptComboBox.value) {
                "Clean install" -> rf.exec("flash_all")
                "Clean install and lock" -> rf.exec("flash_all_lock")
                "Update" -> when {
                    File(rom, "flash_all_except_storage.sh").exists() -> rf.exec("flash_all_except_storage")
                    File(rom, "flash_all_except_data.sh").exists() -> rf.exec("flash_all_except_data")
                    File(
                        rom,
                        "flash_all_except_data_storage.sh"
                    ).exists() -> rf.exec("flash_all_except_data_storage")
                }
            }
        }
    }

    @FXML
    private fun bootButtonPressed(event: ActionEvent) {
        if (image != null && image!!.absolutePath.isNotEmpty() && checkFastboot())
            flasher.exec(image, "fastboot boot")
    }

    @FXML
    private fun cacheButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            displayedcomm.exec("fastboot erase cache")
    }

    @FXML
    private fun dataButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            displayedcomm.exec("fastboot erase userdata")
    }

    @FXML
    private fun cachedataButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            displayedcomm.exec("fastboot erase cache", "fastboot erase userdata")
    }

    @FXML
    private fun lockButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            displayedcomm.exec("fastboot oem lock")
    }

    @FXML
    private fun unlockButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            displayedcomm.exec("fastboot oem unlock")
    }

    @FXML
    private fun getlinkButtonPressed(event: ActionEvent) {
        if (codenameTextField.text.trim().isNotEmpty() && branchComboBox.value != null) {
            val codename = codenameTextField.text.trim()
            var url = URL("http://google.com")
            when (branchComboBox.value) {
                "Global Stable" -> url =
                    URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_global&b=F&r=global&n=")
                "Global Developer" -> url =
                    URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_global&b=X&r=global&n=")
                "China Stable" -> url =
                    URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}&b=F&r=cn&n=")
                "China Developer" -> url =
                    URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}&b=X&r=cn&n=")
            }
            val huc = url.openConnection() as HttpURLConnection
            huc.requestMethod = "GET"
            huc.setRequestProperty("Referer", "http://en.miui.com/a-234.html")
            huc.instanceFollowRedirects = false
            huc.connect()
            huc.disconnect()
            val link = huc.getHeaderField("Location")
            if (link != null && link.contains("bigota")) {
                versionLabel.text = link.substringAfter(".com/").substringBefore("/")
                outputTextArea.text += "\n\n${link}\n\nLink copied to clipboard!"
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(link), null)
            } else {
                versionLabel.text = "-"
                outputTextArea.text += "\n\nLink not found!"
            }
        }
    }

    @FXML
    private fun downloadromButtonPressed(event: ActionEvent) {
        if (codenameTextField.text.trim().isNotEmpty() && branchComboBox.value != null) {
            val codename = codenameTextField.text.trim()
            var url = URL("http://google.com")
            when (branchComboBox.value) {
                "Global Stable" -> url =
                    URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_global&b=F&r=global&n=")
                "Global Developer" -> url =
                    URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}_global&b=X&r=global&n=")
                "China Stable" -> url =
                    URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}&b=F&r=cn&n=")
                "China Developer" -> url =
                    URL("http://update.miui.com/updates/v1/fullromdownload.php?d=${codename}&b=X&r=cn&n=")
            }
            val huc = url.openConnection() as HttpURLConnection
            huc.requestMethod = "GET"
            huc.setRequestProperty("Referer", "http://en.miui.com/a-234.html")
            huc.instanceFollowRedirects = false
            huc.connect()
            huc.disconnect()
            val link = huc.getHeaderField("Location")
            if (link != null && link.contains("bigota")) {
                versionLabel.text = link.substringAfter(".com/").substringBefore("/")
                outputTextArea.text += "\n\nStarting download in browser..."
                if (System.getProperty("os.name").toLowerCase().contains("linux"))
                    Runtime.getRuntime().exec("xdg-open ${link}")
                else Desktop.getDesktop().browse(URI(link))
            } else {
                versionLabel.text = "-"
                outputTextArea.text += "\n\nLink not found!"
            }
        }
    }

    @FXML
    private fun systemMenuItemPressed(event: ActionEvent) {
        when (device.mode) {
            1 -> if (checkADB()) {
                comm.exec("adb reboot")
                checkADB()
            }
            2 -> if (checkFastboot()) {
                comm.exec("fastboot reboot")
                checkFastboot()
            }
        }
    }

    @FXML
    private fun recoveryMenuItemPressed(event: ActionEvent) {
        when (device.mode) {
            1 -> if (checkADB()) {
                comm.exec("adb reboot recovery")
                checkADB()
            }
        }
    }

    @FXML
    private fun fastbootMenuItemPressed(event: ActionEvent) {
        when (device.mode) {
            1 -> if (checkADB()) {
                comm.exec("adb reboot bootloader")
                checkADB()
            }
            2 -> if (checkFastboot()) {
                comm.exec("fastboot reboot bootloader")
                checkFastboot()
            }
        }
    }

    @FXML
    private fun edlMenuItemPressed(event: ActionEvent) {
        when (device.mode) {
            1 -> if (checkADB()) {
                comm.exec("adb reboot edl")
                checkADB()
            }
            2 -> if (checkFastboot()) {
                comm.exec("fastboot oem edl")
                checkFastboot()
            }
        }
    }

    @FXML
    private fun uninstallButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            installerPane.isDisable = true
            camera2Pane.isDisable = true
            fileExplorerPane.isDisable = true
            devicepropertiesPane.isDisable = true
            dpiPane.isDisable = true
            flasherPane.isDisable = true
            wiperPane.isDisable = true
            oemPane.isDisable = true
            rebootMenu.isDisable = true
            recoveryMenuItem.isDisable = true
            installer.uninstall {
                installerPane.isDisable = false
                camera2Pane.isDisable = false
                fileExplorerPane.isDisable = false
                devicepropertiesPane.isDisable = false
                dpiPane.isDisable = device.recovery
                flasherPane.isDisable = true
                wiperPane.isDisable = true
                oemPane.isDisable = true
                rebootMenu.isDisable = false
                recoveryMenuItem.isDisable = false
            }
        }
    }

    @FXML
    private fun reinstallButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            installerPane.isDisable = true
            camera2Pane.isDisable = true
            fileExplorerPane.isDisable = true
            devicepropertiesPane.isDisable = true
            dpiPane.isDisable = true
            flasherPane.isDisable = true
            wiperPane.isDisable = true
            oemPane.isDisable = true
            rebootMenu.isDisable = true
            recoveryMenuItem.isDisable = true
            installer.reinstall {
                installerPane.isDisable = false
                camera2Pane.isDisable = false
                fileExplorerPane.isDisable = false
                devicepropertiesPane.isDisable = false
                dpiPane.isDisable = device.recovery
                flasherPane.isDisable = true
                wiperPane.isDisable = true
                oemPane.isDisable = true
                rebootMenu.isDisable = false
                recoveryMenuItem.isDisable = false
            }
        }
    }

    @FXML
    private fun unaddButtonPressed(event: ActionEvent) {
        if (uncustomappTextField.text != null && uncustomappTextField.text.trim().isNotEmpty())
            installer.uninstallTableView.items.add(App("Custom app", uncustomappTextField.text.trim(), true))
        uncustomappTextField.text = null
        installer.uninstallTableView.refresh()
    }

    @FXML
    private fun readdButtonPressed(event: ActionEvent) {
        if (recustomappTextField.text != null && recustomappTextField.text.trim().isNotEmpty())
            installer.reinstallTableView.items.add(App("Custom app", recustomappTextField.text.trim(), true))
        recustomappTextField.text = null
        installer.reinstallTableView.refresh()
    }

    @FXML
    private fun aboutMenuItemPressed(event: ActionEvent) {
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
            if (System.getProperty("os.name").toLowerCase().contains("linux"))
                Runtime.getRuntime().exec("xdg-open https://discord.gg/xiaomi")
            else Desktop.getDesktop().browse(URI("https://discord.gg/xiaomi"))
        }
        discord.font = Font(14.0)
        val twitter = Hyperlink("Saki_EU on Twitter")
        twitter.onAction = EventHandler {
            if (System.getProperty("os.name").toLowerCase().contains("linux"))
                Runtime.getRuntime().exec("xdg-open https://twitter.com/Saki_EU")
            else Desktop.getDesktop().browse(URI("https://twitter.com/Saki_EU"))
        }
        twitter.font = Font(14.0)
        val github = Hyperlink("Repository on GitHub")
        github.onAction = EventHandler {
            if (System.getProperty("os.name").toLowerCase().contains("linux"))
                Runtime.getRuntime().exec("xdg-open https://github.com/Saki-EU/XiaomiADBFastbootTools")
            else Desktop.getDesktop().browse(URI("https://github.com/Saki-EU/XiaomiADBFastbootTools"))
        }
        github.font = Font(14.0)
        vb.children.addAll(discord, twitter, github)
        alert.dialogPane.content = vb
        alert.isResizable = false
        alert.showAndWait()
    }
}

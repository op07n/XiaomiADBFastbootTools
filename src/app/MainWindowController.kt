package app

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.StageStyle
import java.awt.Desktop
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*

class MainWindowController : Initializable {

    @FXML
    private lateinit var optionsMenu: Menu
    @FXML
    private lateinit var checkMenuItem: MenuItem
    @FXML
    private lateinit var rebootMenu: Menu
    @FXML
    private lateinit var systemMenuItem: MenuItem
    @FXML
    private lateinit var recoveryMenuItem: MenuItem
    @FXML
    private lateinit var fastbootMenuItem: MenuItem
    @FXML
    private lateinit var edlMenuItem: MenuItem
    @FXML
    private lateinit var aboutMenuItem: MenuItem
    @FXML
    private lateinit var serialLabel: Label
    @FXML
    private lateinit var codenameLabel: Label
    @FXML
    private lateinit var bootloaderLabel: Label
    @FXML
    private lateinit var antiLabel: Label
    @FXML
    private lateinit var outputTextArea: TextArea
    @FXML
    private lateinit var progressBar: ProgressBar
    @FXML
    private lateinit var uninstallerTableView: TableView<App>
    @FXML
    private lateinit var checkTableColumn: TableColumn<App, Boolean>
    @FXML
    private lateinit var appTableColumn: TableColumn<App, String>
    @FXML
    private lateinit var packageTableColumn: TableColumn<App, String>
    @FXML
    private lateinit var customappTextField: TextField
    @FXML
    private lateinit var uninstallButton: Button
    @FXML
    private lateinit var addButton: Button
    @FXML
    private lateinit var reboottwrpButton: Button
    @FXML
    private lateinit var disableButton: Button
    @FXML
    private lateinit var enableButton: Button
    @FXML
    private lateinit var disableEISButton: Button
    @FXML
    private lateinit var enableEISButton: Button
    @FXML
    private lateinit var readpropertiesButton: Button
    @FXML
    private lateinit var savepropertiesButton: Button
    @FXML
    private lateinit var dpiTextField: TextField
    @FXML
    private lateinit var dpiButton: Button
    @FXML
    private lateinit var antirbButton: Button
    @FXML
    private lateinit var browseimageButton: Button
    @FXML
    private lateinit var browseromButton: Button
    @FXML
    private lateinit var partitionComboBox: ComboBox<String>
    @FXML
    private lateinit var scriptComboBox: ComboBox<String>
    @FXML
    private lateinit var flashimageButton: Button
    @FXML
    private lateinit var flashromButton: Button
    @FXML
    private lateinit var autobootCheckBox: CheckBox
    @FXML
    private lateinit var imageLabel: Label
    @FXML
    private lateinit var romLabel: Label
    @FXML
    private lateinit var bootButton: Button
    @FXML
    private lateinit var cacheButton: Button
    @FXML
    private lateinit var cachedataButton: Button
    @FXML
    private lateinit var unlockButton: Button
    @FXML
    private lateinit var lockButton: Button
    @FXML
    private lateinit var adbTab: Tab
    @FXML
    private lateinit var fastbootTab: Tab

    lateinit var device: Device
    var image: File? = null
    var rom: File? = null
    lateinit var comm: Command
    lateinit var displayedcomm: Command
    lateinit var uninstaller: Uninstaller

    fun setLabels() {
        serialLabel.text = device.serial
        codenameLabel.text = device.codename
        if (device.bootloader)
            bootloaderLabel.text = "unlocked"
        else
            bootloaderLabel.text = "locked"
        if (device.anti != -1)
            antiLabel.text = Integer.toString(device.anti)
        else
            antiLabel.text = "-"
    }

    fun clear() {
        serialLabel.text = "-"
        bootloaderLabel.text = "-"
        codenameLabel.text = "-"
        antiLabel.text = "-"
        dpiTextField.text = ""

        fastbootTab.isDisable = true
        adbTab.isDisable = true
        rebootMenu.isDisable = true
        recoveryMenuItem.isDisable = true
    }

    fun checkFastboot(): Boolean {
        val fb = device.readFastboot()
        if (fb) {
            setLabels()
            adbTab.isDisable = true
            fastbootTab.isDisable = false
            recoveryMenuItem.isDisable = true
            rebootMenu.isDisable = false
        } else {
            outputTextArea.text = "No device found in Fastboot mode!"
            clear()
        }
        return fb
    }

    fun checkADB(): Boolean {
        val adb = device.readADB()
        if (adb) {
            setLabels()
            adbTab.isDisable = false
            fastbootTab.isDisable = true
            recoveryMenuItem.isDisable = false
            rebootMenu.isDisable = false
            antiLabel.text = "unknown"
        } else {
            if (device.auth)
                outputTextArea.text = "ERROR: Device unauthorised!\nPlease allow USB debugging!"
            else outputTextArea.text = "No device found in ADB mode!"
            clear()
        }
        return adb
    }

    override fun initialize(url: URL, rb: ResourceBundle?) {
        clear()
        partitionComboBox.items.addAll(
                "boot", "cust", "modem", "persist", "recovery", "system")
        scriptComboBox.items.addAll(
                "Clean install", "Clean install and lock", "Update")
        comm = Command()
        displayedcomm = Command(outputTextArea)
        image = null
        rom = null
        device = Device()
        uninstaller = Uninstaller(uninstallerTableView, progressBar, outputTextArea)

        checkTableColumn.cellValueFactory = PropertyValueFactory("selected")
        checkTableColumn.setCellFactory { tc -> CheckBoxTableCell() }
        appTableColumn.cellValueFactory = PropertyValueFactory("appname")
        packageTableColumn.cellValueFactory = PropertyValueFactory("packagename")
        uninstallerTableView.columns.setAll(checkTableColumn, appTableColumn, packageTableColumn)
    }

    @FXML
    private fun checkMenuItemPressed(event: ActionEvent) {
        val fb = device.readFastboot()
        val adb = device.readADB()
        if (adb || fb) {
            setLabels()
            rebootMenu.isDisable = false
            if (fb) {
                outputTextArea.text = "Device found in Fastboot mode!"
                adbTab.isDisable = true
                fastbootTab.isDisable = false
                recoveryMenuItem.isDisable = true
            }
            if (adb) {
                uninstaller.loadApps(device)
                antiLabel.text = "unknown"
                dpiTextField.text = Integer.toString(device.dpi)
                outputTextArea.text = "Device found in ADB mode!"
                adbTab.isDisable = false
                fastbootTab.isDisable = true
                recoveryMenuItem.isDisable = false
            }
        } else {
            if (device.auth)
                outputTextArea.text = "Device unauthorised!\nPlease allow USB debugging!"
            else outputTextArea.text = "No device found!"
            clear()
        }
    }

    @FXML
    private fun reboottwrpButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (comm.exec("adb devices").contains("recovery")) {
                outputTextArea.text = "Device already in recovery mode!"
            } else {
                comm.exec("adb reboot recovery")
            }
        }
    }

    fun checkcamera2(): Boolean {
        return comm.exec("adb shell getprop persist.camera.HAL3.enabled").contains("1")
    }

    fun checkEIS(): Boolean {
        return comm.exec("adb shell getprop persist.camera.eis.enable").contains("1")
    }

    @FXML
    private fun disableButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (!comm.exec("adb devices").contains("recovery")) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            comm.exec("adb shell setprop persist.camera.HAL3.enabled 0")
            if (!checkcamera2())
                outputTextArea.text = "Disabled!"
            else outputTextArea.text = "ERROR: Couldn't disable!"
        }
    }

    @FXML
    private fun enableButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (!comm.exec("adb devices").contains("recovery")) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            comm.exec("adb shell setprop persist.camera.HAL3.enabled 1")
            if (checkcamera2())
                outputTextArea.text = "Enabled!"
            else outputTextArea.text = "ERROR: Couldn't enable!"
        }
    }

    @FXML
    private fun disableEISButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (!comm.exec("adb devices").contains("recovery")) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            comm.exec("adb shell setprop persist.camera.eis.enable 0")
            if (!checkEIS())
                outputTextArea.text = "Disabled!"
            else outputTextArea.text = "ERROR: Couldn't disable!"
        }
    }

    @FXML
    private fun enableEISButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            if (!comm.exec("adb devices").contains("recovery")) {
                outputTextArea.text = "ERROR: No device found in recovery mode!"
                return
            }
            comm.exec("adb shell setprop persist.camera.eis.enable 1")
            if (checkEIS())
                outputTextArea.text = "Enabled!"
            else outputTextArea.text = "ERROR: Couldn't enable!"
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
            when {
                f != null -> try {
                    val fw = FileWriter(f)
                    fw.write(comm.exec("adb shell getprop"))
                    fw.flush()
                    fw.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    @FXML
    private fun dpiButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            val attempt = displayedcomm.exec("adb shell wm density " + dpiTextField.text.trim())
            if (attempt.contains("permission"))
                outputTextArea.text = "ERROR: Please allow USB debugging (Security settings)!"
            if (attempt.contains("bad number"))
                outputTextArea.text = "ERROR: Invalid value!"
            if (attempt.isEmpty()) {
                outputTextArea.text = "Done! Rebooting..."
                comm.exec("adb reboot")
            }
        }
    }

    @FXML
    private fun antirbButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            displayedcomm.exec("fastboot flash antirbpass dummy.img")
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
        when {
            image != null && partitionComboBox.value != null ->
                if (image!!.absolutePath.isNotEmpty() && partitionComboBox.value.trim().isNotEmpty() && checkFastboot()) {
                    if (autobootCheckBox.isSelected && partitionComboBox.value.trim() == "recovery")
                        displayedcomm.exec(image, "fastboot flash " + partitionComboBox.value.trim(), "fastboot boot")
                    else displayedcomm.exec(image, "fastboot flash " + partitionComboBox.value.trim())
                }
        }
    }

    @FXML
    private fun browseromButtonPressed(event: ActionEvent) {
        val dc = DirectoryChooser()
        dc.title = "Select the root directory of a Fastboot ROM"
        rom = dc.showDialog((event.source as Node).scene.window)
        outputTextArea.text = ""
        when {
            rom != null ->
                if (File(rom, "images").exists()) {
                    romLabel.text = rom?.name
                    outputTextArea.text = "Fastboot ROM found!"
                    File(rom, "flash_all.sh").setExecutable(true, false)
                    File(rom, "flash_all_lock.sh").setExecutable(true, false)
                    if (File(rom, "flash_all_except_storage.sh").exists())
                        File(rom, "flash_all_except_storage.sh").setExecutable(true, false)
                    else File(rom, "flash_all_except_data.sh").setExecutable(true, false)
                } else {
                    outputTextArea.text = "ERROR: Fastboot ROM not found!"
                    rom = null
                }
        }
    }

    @FXML
    private fun flashromButtonPressed(event: ActionEvent) {
        when {
            rom != null && scriptComboBox.value != null ->
                if (checkFastboot()) {
                    val fs = FastbootFlasher(progressBar, outputTextArea, rom!!)
                    progressBar.progress = 0.0
                    if (scriptComboBox.value == "Clean install") fs.exec("flash_all")
                    if (scriptComboBox.value == "Clean install and lock") fs.exec("flash_all_lock")
                    if (scriptComboBox.value == "Update") {
                        if (File(rom, "flash_all_except_storage.sh").exists())
                            fs.exec("flash_all_except_storage")
                        else fs.exec("flash_all_except_data")
                    }
                }
        }
    }

    @FXML
    private fun bootButtonPressed(event: ActionEvent) {
        when {
            image != null ->
                if (image!!.absolutePath.isNotEmpty() && checkFastboot())
                    displayedcomm.exec(image, "fastboot boot")
        }
    }

    @FXML
    private fun cacheButtonPressed(event: ActionEvent) {
        if (checkFastboot())
            displayedcomm.exec("fastboot erase cache")
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
    private fun systemMenuItemPressed(event: ActionEvent) {
        if (checkADB())
            comm.exec("adb reboot")
        else if (checkFastboot())
            comm.exec("fastboot reboot")
    }

    @FXML
    private fun recoveryMenuItemPressed(event: ActionEvent) {
        if (checkADB())
            comm.exec("adb reboot recovery")
    }

    @FXML
    private fun fastbootMenuItemPressed(event: ActionEvent) {
        if (checkADB())
            comm.exec("adb reboot bootloader")
        else if (checkFastboot())
            comm.exec("fastboot reboot bootloader")
    }

    @FXML
    private fun edlMenuItemPressed(event: ActionEvent) {
        if (checkADB())
            comm.exec("adb reboot edl")
        else if (checkFastboot())
            displayedcomm.exec("fastboot oem edl")
    }

    @FXML
    private fun uninstallButtonPressed(event: ActionEvent) {
        if (checkADB()) {
            progressBar.progress = 0.0
            uninstaller.uninstall()
        }
    }

    @FXML
    private fun addButtonPressed(event: ActionEvent) {
        when {
            customappTextField.text != null ->
                if (customappTextField.text.trim().isNotEmpty())
                    uninstaller.apps.add(App("Custom app", customappTextField.text.trim()))
        }
        customappTextField.text = null
        uninstaller.tv.refresh()
    }

    @FXML
    private fun aboutMenuItemPressed(event: ActionEvent) {
        val alert = Alert(AlertType.INFORMATION)
        alert.initStyle(StageStyle.UTILITY)
        alert.title = "About"
        alert.graphic = ImageView(Image(this.javaClass.classLoader.getResource("res/smallicon.png").toString()))
        alert.headerText = "Xiaomi ADB/Fastboot Tools" + System.lineSeparator() + "Version 4.3.1" + System.lineSeparator() + "Created by Saki_EU"
        val vb = VBox()
        vb.alignment = Pos.CENTER

        val reddit = Hyperlink("r/Xiaomi on Reddit")
        reddit.onAction = EventHandler {
            try {
                Desktop.getDesktop().browse(URI("https://www.reddit.com/r/Xiaomi"))
            } catch (e1: IOException) {
                e1.printStackTrace()
            } catch (e1: URISyntaxException) {
                e1.printStackTrace()
            }
        }
        reddit.font = Font(14.0)
        val discord = Hyperlink("r/Xiaomi on Discord")
        discord.onAction = EventHandler {
            try {
                Desktop.getDesktop().browse(URI("https://discord.gg/xiaomi"))
            } catch (e1: IOException) {
                e1.printStackTrace()
            } catch (e1: URISyntaxException) {
                e1.printStackTrace()
            }
        }
        discord.font = Font(14.0)
        val github = Hyperlink("This project on GitHub")
        github.onAction = EventHandler {
            try {
                Desktop.getDesktop().browse(URI("https://github.com/Saki-EU/XiaomiADBFastbootTools"))
            } catch (e1: IOException) {
                e1.printStackTrace()
            } catch (e1: URISyntaxException) {
                e1.printStackTrace()
            }
        }
        github.font = Font(14.0)

        vb.children.addAll(reddit, discord, github)
        alert.dialogPane.content = vb
        alert.showAndWait()
    }

}

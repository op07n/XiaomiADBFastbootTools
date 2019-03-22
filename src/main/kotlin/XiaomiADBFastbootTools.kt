import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Hyperlink
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class XiaomiADBFastbootTools : Application() {

    private val command = Command()

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(XiaomiADBFastbootTools::class.java)
        }
    }

    fun createFile(file: String, exec: Boolean) {
        val temp = File(System.getProperty("user.home") + "/temp")
        temp.mkdir()
        var bytes: ByteArray? = null
        try {
            bytes = IOUtils.toByteArray(this.javaClass.classLoader.getResourceAsStream(file))
        } catch (ex: IOException) {
            ex.printStackTrace()
            ExceptionAlert(ex)
        }
        val newfile = if (file.lastIndexOf("/") != -1)
            File(System.getProperty("user.home") + "/temp/${file.substring(file.lastIndexOf("/") + 1)}")
        else File(System.getProperty("user.home") + "/temp/$file")
        if (!newfile.exists()) {
            try {
                newfile.createNewFile()
                val fos = FileOutputStream(newfile)
                fos.write(bytes)
                fos.flush()
                fos.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
                ExceptionAlert(ex)
            }

        }
        newfile.setExecutable(exec, false)
    }

    fun setupFiles() {
        val os = System.getProperty("os.name").toLowerCase()
        createFile("dummy.img", false)
        if (os.contains("win")) {
            createFile("windows/adb.exe", true)
            createFile("windows/fastboot.exe", true)
            createFile("windows/AdbWinApi.dll", false)
            createFile("windows/AdbWinUsbApi.dll", false)
        }
        if (os.contains("mac")) {
            createFile("macos/adb", true)
            createFile("macos/fastboot", true)
        }
        if (os.contains("linux")) {
            createFile("linux/adb", true)
            createFile("linux/fastboot", true)
        }
    }

    fun versionToInt(ver: String): Int {
        val bits = ("$ver.0").split('.')
        return bits[0].toInt() * 100 + bits[1].toInt() * 10 + bits[2].toInt()
    }

    fun checkVersion() {
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
        if (versionToInt(latest) > versionToInt(MainController.version)) {
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.initStyle(StageStyle.UTILITY)
            alert.title = "New version available!"
            alert.graphic = ImageView("mitu.png")
            alert.headerText =
                "Version $latest is available!"
            val vb = VBox()
            vb.alignment = Pos.CENTER
            val download = Hyperlink("Download")
            download.onAction = EventHandler {
                if (System.getProperty("os.name").toLowerCase().contains("linux"))
                    Runtime.getRuntime().exec("xdg-open $link")
                else Desktop.getDesktop().browse(URI(link))
            }
            download.font = Font(14.0)
            vb.children.add(download)
            alert.dialogPane.content = vb
            alert.showAndWait()
        }
    }

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        if (File(System.getProperty("user.home") + "/temp").exists()) {
            try {
                FileUtils.deleteDirectory(File(System.getProperty("user.home") + "/temp"))
            } catch (ex: IOException) {
                ex.printStackTrace()
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Fatal Error"
                alert.headerText = "ERROR: Please kill adb in Task Manager and try again!"
                alert.showAndWait()
                Platform.exit()
            }
        }
        setupFiles()
        val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("Main.fxml"))
        val scene = Scene(root)
        stage.scene = scene
        stage.title = "Xiaomi ADB/Fastboot Tools"
        stage.icons.add(Image("icon.png"))
        stage.show()
        stage.isResizable = false
        if (!File(System.getProperty("user.home") + "/temp/adb").exists() && !File(System.getProperty("user.home") + "/temp/adb.exe").exists()) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Fatal Error"
            alert.headerText = "ERROR: Couldn't initialize ADB!"
            alert.showAndWait()
            Platform.exit()
        }
        checkVersion()
    }

    override fun stop() {
        MainController.thread.interrupt()
        command.exec("adb kill-server")
        while (File(System.getProperty("user.home") + "/temp").exists()) {
            try {
                FileUtils.deleteDirectory(File(System.getProperty("user.home") + "/temp"))
            } catch (ex: IOException) {
                Thread.sleep(500)
                continue
            }
        }
    }

}

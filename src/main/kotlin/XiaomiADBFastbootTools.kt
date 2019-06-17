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
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class XiaomiADBFastbootTools : Application() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(XiaomiADBFastbootTools::class.java)
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

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        try {
            ProcessBuilder("adb", "--version").start()
            ProcessBuilder("fastboot", "--version").start()
        } catch (e: Exception) {
            if ((File("adb").exists() || File("adb.exe").exists()) && (File("fastboot").exists() || File("fastboot.exe").exists())) {
                Command.prefix = if ("win" in System.getProperty("os.name").toLowerCase())
                    System.getProperty("user.dir") + '/'
                else "./"
            } else {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Fatal Error"
                alert.headerText = "ERROR: Can't find ADB/Fastboot!\nPlease install them system-wide or put the JAR next to them!"
                alert.showAndWait()
                Platform.exit()
            }
        }
        val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("Main.fxml"))
        val scene = Scene(root)
        stage.scene = scene
        stage.title = "Xiaomi ADB/Fastboot Tools"
        stage.icons.add(Image("icon.png"))
        stage.show()
        checkVersion()
    }

    override fun stop() {
        MainController.thread.interrupt()
        Command.exec("adb kill-server")
    }

}

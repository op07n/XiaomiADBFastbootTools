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

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("Main.fxml"))
        val scene = Scene(root)
        stage.scene = scene
        stage.title = "Xiaomi ADB/Fastboot Tools"
        stage.icons.add(Image("icon.png"))
        stage.show()
    }

    override fun stop() {
        MainController.thread.interrupt()
        Command.exec("adb kill-server")
    }

}

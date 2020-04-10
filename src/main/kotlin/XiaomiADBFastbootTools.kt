import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class XiaomiADBFastbootTools : Application() {

    companion object {
        val version = "7.0"
        val dir = File(System.getProperty("user.home"), "XiaomiADBFastbootTools")
        val win = "win" in System.getProperty("os.name").toLowerCase()
        val linux = "linux" in System.getProperty("os.name").toLowerCase()

        @JvmStatic
        fun main(args: Array<String>) {
            launch(XiaomiADBFastbootTools::class.java)
        }
    }

    init {
        dir.mkdir()
    }

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        stage.scene = Scene(FXMLLoader.load(javaClass.classLoader.getResource("Main.fxml")))
        stage.title = "Xiaomi ADB/Fastboot Tools"
        stage.icons.add(Image("icon.png"))
        stage.show()
    }

    override fun stop() {
        GlobalScope.launch {
            try {
                Command.exec(mutableListOf("adb", "kill-server"))
            } catch (e: Exception) {
                // OK
            }
        }
    }

}

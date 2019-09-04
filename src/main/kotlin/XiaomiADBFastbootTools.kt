import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage


class XiaomiADBFastbootTools : Application() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(XiaomiADBFastbootTools::class.java)
        }
    }

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        stage.scene = Scene(FXMLLoader.load<Parent>(javaClass.classLoader.getResource("Main.fxml")))
        stage.title = "Xiaomi ADB/Fastboot Tools"
        stage.icons.add(Image("icon.png"))
        stage.show()
    }

    override fun stop() {
        try {
            Command.exec("adb kill-server")
        } catch (e: Exception) {
            //
        }
    }

}

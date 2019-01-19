import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.stage.Stage
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class XiaomiADBFastbootTools : Application() {

    val command = Command()

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
        }
        var newfile: File
        if (file.lastIndexOf("/") != -1)
            newfile = File(System.getProperty("user.home") + "/temp/${file.substring(file.lastIndexOf("/") + 1)}")
        else newfile = File(System.getProperty("user.home") + "/temp/$file")
        if (!newfile.exists()) {
            try {
                newfile.createNewFile()
                val fos = FileOutputStream(newfile)
                fos.write(bytes)
                fos.flush()
                fos.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
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

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        if (File(System.getProperty("user.home") + "/temp").exists()) {
            command.exec("adb kill-server")
            try {
                FileUtils.deleteDirectory(File(System.getProperty("user.home") + "/temp"))
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
        setupFiles()
        val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("MainWindow.fxml"))
        val scene = Scene(root)
        stage.scene = scene
        stage.title = "Xiaomi ADB/Fastboot Tools"
        stage.icons.add(Image(javaClass.classLoader.getResource("icon.png").toString()))
        stage.show()
        stage.isResizable = false
        if (!File(System.getProperty("user.home") + "/temp/adb").exists() && !File(System.getProperty("user.home") + "/temp/adb.exe").exists()) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Fatal Error"
            alert.headerText = "ERROR: Couldn't initialize ADB!"
            alert.showAndWait()
            Platform.exit()
        }
    }

    override fun stop() {
        MainWindowController.thread.interrupt()
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

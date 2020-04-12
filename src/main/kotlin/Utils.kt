import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.StageStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL

enum class Mode {
    ADB, FASTBOOT, AUTH, RECOVERY, ADB_ERROR, FASTBOOT_ERROR
}

fun isAppSelected(list: ObservableList<App>) = list.isNotEmpty() && list.any { it.selectedProperty().get() }

fun String.escape(): String = "'$this'"

fun MutableMap<String, MutableList<String>>.add(key: String, value: String) {
    if (this[key] == null) {
        this[key] = mutableListOf(value)
    } else this[key]!!.add(value)
}

fun startProcess(vararg command: String, redirectErrorStream: Boolean = false) =
    ProcessBuilder(*command).directory(XiaomiADBFastbootTools.dir).redirectErrorStream(redirectErrorStream).start()

fun startProcess(command: List<String?>, redirectErrorStream: Boolean = false) =
    ProcessBuilder(command).directory(XiaomiADBFastbootTools.dir).redirectErrorStream(redirectErrorStream).start()

suspend fun Exception.alert() {
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter)
    this.printStackTrace(printWriter)
    withContext(Dispatchers.Main) {
        Alert(Alert.AlertType.ERROR).apply {
            initStyle(StageStyle.UTILITY)
            title = "ERROR"
            headerText =
                "Unexpected exception!"
            val vb = VBox()
            vb.alignment = Pos.CENTER
            val textArea = TextArea(stringWriter.toString()).apply {
                isEditable = false
                isWrapText = true
                maxWidth = Double.MAX_VALUE
                maxHeight = Double.MAX_VALUE
            }
            vb.children.add(textArea)
            dialogPane.content = vb
            isResizable = false
            showAndWait()
        }
        Platform.exit()
    }
}

suspend fun confirm(msg: String = ""): Boolean = withContext(Dispatchers.Main) {
    Alert(Alert.AlertType.CONFIRMATION).run {
        initStyle(StageStyle.UTILITY)
        isResizable = false
        headerText = "${msg.trim()}\nAre you sure you want to proceed?".trim()
        val yes = ButtonType("Yes")
        val no = ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE)
        buttonTypes.setAll(yes, no)
        val result = showAndWait()
        result.get() == yes
    }
}

fun getLink(version: String, codename: String): String? {
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
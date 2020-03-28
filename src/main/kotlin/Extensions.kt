import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.StageStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

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
    }
    Platform.exit()
}

fun File.getCmdCount(): Int = this.readText().split("fastboot").size - 1

fun MutableMap<String, MutableList<String>>.add(key: String, value: String) {
    if (this[key] == null) {
        this[key] = mutableListOf(value)
    } else this[key]!!.add(value)
}
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.StageStyle
import java.io.PrintWriter
import java.io.StringWriter

class ExceptionAlert(ex: Exception) {

    private val alert = Alert(Alert.AlertType.ERROR)
    private val vb = VBox()
    private val stringWriter = StringWriter()
    private val printWriter = PrintWriter(stringWriter)

    init {
        alert.apply {
            initStyle(StageStyle.UTILITY)
            title = "ERROR"
            headerText =
                "Unexpected exception!"
            vb.alignment = Pos.CENTER
            ex.printStackTrace(printWriter)
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
}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.net.URL
import java.util.*

class AppAdderController : Initializable {

    @FXML
    private lateinit var appTextArea: TextArea

    override fun initialize(location: URL?, resources: ResourceBundle?) {
    }

    @FXML
    private fun loadButtonPressed(event: ActionEvent) {
        val fc = FileChooser()
        fc.extensionFilters.add(FileChooser.ExtensionFilter("Text File", "*"))
        fc.title = "Select a text file"
        val file = fc.showOpenDialog((event.source as Node).scene.window)
        appTextArea.text = file?.readText()
    }

    @FXML
    private fun okButtonPressed(event: ActionEvent) {
        appTextArea.text?.let {
            if (it.isNotBlank()) {
                it.trim().lines().forEach { line ->
                    AppManager.addApp(line.trim())
                }
                AppManager.createTables()
            }
        }
        ((event.source as Node).scene.window as Stage).close()
    }
}
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
        appTextArea.text = fc.showOpenDialog((event.source as Node).scene.window)?.readText()
    }

    @FXML
    private fun okButtonPressed(event: ActionEvent) {
        if (!appTextArea.text.isNullOrBlank()) {
            appTextArea.text.trim().lines().forEach {
                AppManager.addApp(it.trim())
            }
            AppManager.createTables()
        }
        ((event.source as Node).scene.window as Stage).close()
    }
}
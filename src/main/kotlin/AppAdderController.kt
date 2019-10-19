import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.TextArea
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.net.URL
import java.util.*

class AppAdderController : Initializable {

    @FXML
    private lateinit var appTextArea: TextArea

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        if (AppManager.appsFile.exists())
            appTextArea.text = AppManager.appsFile.readText()
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
        if (appTextArea.text != null) {
            if (appTextArea.text.isNotBlank()) {
                Alert(Alert.AlertType.WARNING).apply {
                    initStyle(StageStyle.UTILITY)
                    isResizable = false
                    headerText = "Uninstalling apps which aren't listed by default may brick your device."
                    showAndWait()
                }
            }
            AppManager.apply {
                appsFile.writeText(appTextArea.text.trim())
                readPotentialApps()
                createTables()
            }
        }
        ((event.source as Node).scene.window as Stage).close()
    }
}
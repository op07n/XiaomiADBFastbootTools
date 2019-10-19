import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.net.URL
import java.util.*

class FileExplorerController : Initializable {

    @FXML
    private lateinit var listView: ListView<AndroidFile>
    @FXML
    private lateinit var pathTextField: TextField
    @FXML
    private lateinit var backButton: Button
    @FXML
    private lateinit var statusTextField: TextField
    @FXML
    private lateinit var progressBar: ProgressBar

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        backButton.graphic = ImageView("back.png")
        backButton.setOnMouseClicked {
            navigate("..")
        }
        pathTextField.setOnAction {
            pathTextField.text = pathTextField.text.trim()
            if (!pathTextField.text.endsWith('/'))
                pathTextField.text += '/'
            FileExplorer.path = pathTextField.text
            listView.items = FileExplorer.getFiles()
            listView.refresh()
        }

        FileExplorer.statusTextField = statusTextField
        FileExplorer.progressBar = progressBar
        pathTextField.text = FileExplorer.path
        listView.apply {
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            setCellFactory { FileListCell() }
            items = FileExplorer.getFiles()
            refresh()
        }
    }

    private fun loadList() {
        pathTextField.text = FileExplorer.path
        listView.items = FileExplorer.getFiles()
        listView.refresh()
    }

    private fun navigate(where: String) {
        FileExplorer.navigate(where)
        loadList()
    }

    @FXML
    private fun pushButtonPressed(event: ActionEvent) {
        if (Device.readADB()) {
            val fc = FileChooser()
            fc.title = "Select files to copy"
            fc.showOpenMultipleDialog((event.source as Node).scene.window)?.let {
                FileExplorer.push(it) {
                    loadList()
                }
            }
        } else close(event)
    }

    @FXML
    private fun pullButtonPressed(event: ActionEvent) {
        if (Device.readADB()) {
            val dc = DirectoryChooser()
            dc.title = "Select the destination"
            dc.showDialog((event.source as Node).scene.window)?.let {
                FileExplorer.pull(listView.selectionModel.selectedItems, it) {
                    loadList()
                }
            }
        } else close(event)
    }

    @FXML
    private fun newFolderButtonPressed(event: ActionEvent) {
        if (Device.readADB()) {
            val dialog = TextInputDialog().apply {
                initStyle(StageStyle.UTILITY)
                isResizable = false
                title = "New Folder"
                contentText = "Folder name:"
                headerText = null
                graphic = null
            }
            val result = dialog.showAndWait()
            if (result.isPresent && result.get().isNotBlank())
                FileExplorer.mkdir(result.get().trim()) {
                    loadList()
                }
        } else close(event)
    }

    @FXML
    private fun deleteButtonPressed(event: ActionEvent) {
        if (Device.readADB()) {
            if (listView.selectionModel.selectedItems.isEmpty())
                return
            val alert = Alert(Alert.AlertType.CONFIRMATION).apply {
                initStyle(StageStyle.UTILITY)
                isResizable = false
                dialogPane.prefWidth *= 0.6
                dialogPane.prefHeight *= 0.6
                title = "Delete"
                headerText = "Are you sure?"
                graphic = ImageView("delete.png")
            }
            val yes = ButtonType("Yes")
            val no = ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE)
            alert.buttonTypes.setAll(yes, no)
            val result = alert.showAndWait()
            if (result.get() == yes)
                FileExplorer.delete(listView.selectionModel.selectedItems) {
                    loadList()
                }
        } else close(event)
    }

    @FXML
    private fun renameButtonPressed(event: ActionEvent) {
        if (Device.readADB()) {
            if (listView.selectionModel.selectedItems.size != 1)
                return
            val item = listView.selectionModel.selectedItems[0]
            val dialog = TextInputDialog(item.name).apply {
                initStyle(StageStyle.UTILITY)
                isResizable = false
                title = "Rename"
                contentText = if (item.dir)
                    "Folder name:"
                else "File name:"
                headerText = null
                graphic = null
            }
            val result = dialog.showAndWait()
            if (result.isPresent && result.get().isNotBlank() && result.get().trim() != item.name)
                FileExplorer.rename(item, result.get().trim()) {
                    loadList()
                }
        } else close(event)
    }

    @FXML
    private fun listViewMouseClicked(event: MouseEvent) {
        if (event.clickCount > 1) {
            val item = listView.selectionModel.selectedItem
            if (item.dir)
                navigate(item.name)
        }
    }

    private fun close(event: ActionEvent) {
        ((event.source as Node).scene.window as Stage).close()
    }
}
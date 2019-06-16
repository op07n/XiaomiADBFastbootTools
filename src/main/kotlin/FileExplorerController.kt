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

    private lateinit var fileExplorer: FileExplorer

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        backButton.graphic = ImageView("back.png")
        backButton.setOnMouseClicked {
            navigate("..")
        }
        pathTextField.setOnAction {
            pathTextField.text = pathTextField.text.trim()
            if (!pathTextField.text.endsWith('/'))
                pathTextField.text += '/'
            fileExplorer.path = pathTextField.text
            listView.items = fileExplorer.getFiles()
            listView.refresh()
        }

        fileExplorer = FileExplorer(statusTextField, progressBar)

        listView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        listView.setCellFactory { FileListCell() }
        pathTextField.text = fileExplorer.path
        listView.items = fileExplorer.getFiles()
        listView.refresh()
    }

    private fun loadList() {
        pathTextField.text = fileExplorer.path
        listView.items = fileExplorer.getFiles()
        listView.refresh()
    }

    private fun navigate(where: String) {
        fileExplorer.navigate(where)
        loadList()
    }

    @FXML
    private fun pushButtonPressed(event: ActionEvent) {
        if (Device.readADB()) {
            val fc = FileChooser()
            fc.title = "Select files to copy"
            fc.showOpenMultipleDialog((event.source as Node).scene.window)?.let {
                fileExplorer.push(it) {
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
                fileExplorer.pull(listView.selectionModel.selectedItems, it) {
                    loadList()
                }
            }
        } else close(event)
    }

    @FXML
    private fun newFolderButtonPressed(event: ActionEvent) {
        if (Device.readADB()) {
            val dialog = TextInputDialog()
            dialog.initStyle(StageStyle.UTILITY)
            dialog.isResizable = false
            dialog.title = "New Folder"
            dialog.contentText = "Folder name:"
            dialog.headerText = null
            dialog.graphic = null
            val result = dialog.showAndWait()
            if (result.isPresent && result.get().trim().isNotEmpty())
                fileExplorer.mkdir(result.get().trim()) {
                    loadList()
                }
        } else close(event)
    }

    @FXML
    private fun deleteButtonPressed(event: ActionEvent) {
        if (Device.readADB()) {
            if (listView.selectionModel.selectedItems.isEmpty())
                return
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.initStyle(StageStyle.UTILITY)
            alert.isResizable = false
            alert.dialogPane.prefWidth *= 0.6
            alert.dialogPane.prefHeight *= 0.6
            alert.title = "Delete"
            alert.headerText = "Are you sure?"
            alert.graphic = ImageView("delete.png")
            val yes = ButtonType("Yes")
            val no = ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE)
            alert.buttonTypes.setAll(yes, no)
            val result = alert.showAndWait()
            if (result.get() == yes)
                fileExplorer.delete(listView.selectionModel.selectedItems) {
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
            val dialog = TextInputDialog(item.name)
            dialog.initStyle(StageStyle.UTILITY)
            dialog.isResizable = false
            dialog.title = "Rename"
            if (item.dir)
                dialog.contentText = "Folder name:"
            else dialog.contentText = "File name:"
            dialog.headerText = null
            dialog.graphic = null
            val result = dialog.showAndWait()
            if (result.isPresent && result.get().trim().isNotEmpty() && result.get().trim() != item.name)
                fileExplorer.rename(item, result.get().trim()) {
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
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import java.io.IOException

class FileListCell : ListCell<AndroidFile>() {

    @FXML
    private lateinit var gridPane: GridPane
    @FXML
    private lateinit var dir: ImageView
    @FXML
    private lateinit var name: Label
    @FXML
    private lateinit var size: Label
    @FXML
    private lateinit var time: Label

    private val folderimg = Image("folder.png")
    private val fileimg = Image("file.png")

    init {
        val fxmlLoader = FXMLLoader(javaClass.classLoader.getResource("File.fxml"))
        fxmlLoader.setController(this)
        try {
            fxmlLoader.load<Parent>()
        } catch (e: IOException) {
            e.printStackTrace()
            ExceptionAlert(e)
        }
    }

    override fun updateItem(item: AndroidFile?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item == null) {
            text = null
            graphic = null
        } else {
            dir.image = if (item.dir)
                folderimg
            else fileimg
            name.text = item.name
            size.text = item.getSize()
            time.text = item.time
            text = null
            graphic = gridPane
        }
    }
}
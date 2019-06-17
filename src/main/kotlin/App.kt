import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty


class App(name: String, packages: List<String>, checked: Boolean = false) {

    //TODO
    private val appname = SimpleStringProperty()
    private val packagename = SimpleStringProperty()
    private val selected = SimpleBooleanProperty()

    init {
        appname.set(name)
        var pkgs = ""
        packages.forEach {
            pkgs += "$it\n"
        }
        packagename.set(pkgs.trim())
        selected.set(checked)
    }

    fun appnameProperty(): StringProperty = appname

    fun packagenameProperty(): StringProperty = packagename

    fun selectedProperty(): BooleanProperty = selected
}

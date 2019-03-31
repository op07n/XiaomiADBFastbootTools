import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty


class App(name: String, packages: List<String>, checked: Boolean = false) {
    private val appname = SimpleStringProperty()
    private val packagename = SimpleStringProperty()
    private val selected = SimpleBooleanProperty()

    constructor(name: String, pkg: String, checked: Boolean = false) : this(name, listOf(pkg), checked)

    init {
        appname.set(name)
        var pkgs = ""
        for (pkg in packages)
            pkgs += "$pkg\n"
        packagename.set(pkgs.trim())
        selected.set(checked)
    }

    fun appnameProperty(): StringProperty = appname

    fun packagenameProperty(): StringProperty = packagename

    fun selectedProperty(): BooleanProperty = selected
}

package app

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty


class App(a: String, b: String) {
    private val appname = SimpleStringProperty()
    private val packagename = SimpleStringProperty()
    private val selected = SimpleBooleanProperty()

    init {
        appname.set(a)
        packagename.set(b)
        selected.set(false)
    }

    fun appnameProperty(): StringProperty = appname

    fun packagenameProperty(): StringProperty = packagename

    fun selectedProperty(): BooleanProperty = selected
}

package tools;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class App {
    private StringProperty appname = new SimpleStringProperty();
    private StringProperty packagename = new SimpleStringProperty();
    private BooleanProperty selected = new SimpleBooleanProperty();

    public App(String a, String b) {
        appname.set(a);
        packagename.set(b);
        selected.set(false);
    }

    public StringProperty appnameProperty() {
        return appname;
    }

    public StringProperty packagenameProperty() {
        return packagename;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }
}

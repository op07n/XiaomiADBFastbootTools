package xiaomiadbfastboottools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import javafx.scene.control.TextInputControl;


public class Uninstaller extends Command {
    
    public Uninstaller(TextInputControl control){
        super(control);
        tic.setText("");
    }
    
    public void uninstall(App app){
        pb.command(Arrays.asList((prefix + "adb shell pm uninstall --user 0 " + app.packagenameProperty().get()).split(" ")));
        pb.redirectErrorStream(false);
        output = "";
        try {
            proc = pb.start();
        } catch (IOException ex) {
        	ex.getMessage();
        }
        scan = new Scanner(proc.getInputStream());
        while (scan.hasNext()) {
            output += scan.nextLine() + System.lineSeparator();
        }
        tic.appendText("App: " + app.appnameProperty().get() + System.lineSeparator());
        tic.appendText("Package: " + app.packagenameProperty().get() + System.lineSeparator());
        tic.appendText("Result: " + output + System.lineSeparator());
        scan.close();
    }
}

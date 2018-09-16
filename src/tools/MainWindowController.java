package tools;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    File image;
    File rom;
    Command comm;
    Command displayedcomm;
    Uninstaller uninstaller;
    @FXML
    private Menu optionsMenu;
    @FXML
    private MenuItem checkMenuItem;
    @FXML
    private Menu rebootMenu;
    @FXML
    private MenuItem systemMenuItem;
    @FXML
    private MenuItem recoveryMenuItem;
    @FXML
    private MenuItem fastbootMenuItem;
    @FXML
    private MenuItem edlMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private Label serialLabel;
    @FXML
    private Label codenameLabel;
    @FXML
    private Label bootloaderLabel;
    @FXML
    private Label antiLabel;
    @FXML
    private TextArea outputTextArea;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TableView<App> debloaterTableView;
    @FXML
    private TableColumn<App, Boolean> checkTableColumn;
    @FXML
    private TableColumn<App, String> appTableColumn;
    @FXML
    private TableColumn<App, String> packageTableColumn;
    @FXML
    private TextField customappTextField;
    @FXML
    private Button uninstallButton;
    @FXML
    private Button addButton;
    @FXML
    private Button reboottwrpButton;
    @FXML
    private Button disableButton;
    @FXML
    private Button enableButton;
    @FXML
    private Button disableEISButton;
    @FXML
    private Button enableEISButton;
    @FXML
    private Button readpropertiesButton;
    @FXML
    private Button savepropertiesButton;
    @FXML
    private Button antirbButton;
    @FXML
    private Button browseimageButton;
    @FXML
    private Button browseromButton;
    @FXML
    private ComboBox<String> partitionComboBox;
    @FXML
    private ComboBox<String> scriptComboBox;
    @FXML
    private Button flashimageButton;
    @FXML
    private Button flashromButton;
    @FXML
    private CheckBox autobootCheckBox;
    @FXML
    private Label imageLabel;
    @FXML
    private Label romLabel;
    @FXML
    private Button bootButton;
    @FXML
    private Button cacheButton;
    @FXML
    private Button cachedataButton;
    @FXML
    private Button unlockButton;
    @FXML
    private Button lockButton;
    @FXML
    private Tab adbTab;
    @FXML
    private Tab fastbootTab;

    public void setLabels(String serial, String codename, String bl, String anti) {
        serialLabel.setText(serial);
        codenameLabel.setText(codename);
        bootloaderLabel.setText(bl);
        antiLabel.setText(anti);
    }

    public void setADB(boolean adb) {
        adbTab.setDisable(!adb);
        recoveryMenuItem.setDisable(!adb);
        if (adb) {
            outputTextArea.setText("Device found!");
            rebootMenu.setDisable(false);
            fastbootTab.setDisable(true);
        } else {
            rebootMenu.setDisable(true);
            outputTextArea.setText("No device found!");
            setLabels("-", "-", "-", "-");
        }
    }

    public void setFastboot(boolean fastboot) {
        recoveryMenuItem.setDisable(fastboot);
        fastbootTab.setDisable(!fastboot);
        if (fastboot) {
            outputTextArea.setText("Device found!");
            rebootMenu.setDisable(false);
            adbTab.setDisable(true);
        } else {
            rebootMenu.setDisable(true);
            outputTextArea.setText("No device found!");
            setLabels("-", "-", "-", "-");
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setLabels("-", "-", "-", "-");
        partitionComboBox.getItems().addAll(
                "boot", "cust", "modem", "persist", "recovery", "system");
        scriptComboBox.getItems().addAll(
                "Clean install", "Clean install and lock", "Update");
        comm = new Command();
        displayedcomm = new Command(outputTextArea);
        image = null;
        rom = null;

        checkTableColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        checkTableColumn.setCellFactory(tc -> new CheckBoxTableCell<>());
        appTableColumn.setCellValueFactory(new PropertyValueFactory<>("appname"));
        packageTableColumn.setCellValueFactory(new PropertyValueFactory<>("packagename"));
        debloaterTableView.getColumns().setAll(checkTableColumn, appTableColumn, packageTableColumn);

        uninstaller = new Uninstaller(debloaterTableView, progressBar, outputTextArea);
    }

    public boolean checkFastboot() {
        String op = comm.exec("fastboot devices", false);
        if (op.length() < 1) {
            setFastboot(false);
            return false;
        }
        String codename = comm.exec("fastboot getvar product", true);
        setFastboot(true);
        setLabels(op.substring(0, op.indexOf("fa")).trim(), codename.substring(9, codename.indexOf(System.lineSeparator())).trim(), "-", "-");
        op = comm.exec("fastboot oem device-info", true);
        if (op.contains("unlocked: true")) {
            bootloaderLabel.setText("unlocked");
        }
        if (op.contains("unlocked: false")) {
            bootloaderLabel.setText("locked");
        }
        op = comm.exec("fastboot getvar anti", true);
        op = op.substring(0, op.indexOf(System.lineSeparator()));
        if (op.length() == 6)
            antiLabel.setText("-");
        else antiLabel.setText(op.substring(6));
        return true;
    }

    public boolean checkADB() {
        String op = comm.exec("adb get-serialno", true);
        if (op.contains("no devices")) {
            setADB(false);
            return false;
        }
        if (op.contains("unauthorized")) {
            setFastboot(false);
            outputTextArea.setText("Device unauthorised!\nPlease allow USB debugging!");
            return false;
        }
        setADB(true);
        setLabels(comm.exec("adb get-serialno", false).trim(), comm.exec("adb shell getprop ro.build.product", false).trim(), "-", "unknown");
        op = comm.exec("adb shell getprop ro.boot.flash.locked", false);
        if (op.contains("0")) {
            bootloaderLabel.setText("unlocked");
        }
        if (op.contains("1")) {
            bootloaderLabel.setText("locked");
        }
        return true;
    }

    public boolean checkDevice() {
        if (!checkFastboot()) {
            if (checkADB()) {
                uninstaller.createTable();
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean checkcamera2() {
        return comm.exec("adb shell getprop persist.camera.HAL3.enabled").contains("1");
    }

    public boolean checkEIS() {
        return comm.exec("adb shell getprop persist.camera.eis.enable").contains("1");
    }

    @FXML
    private void checkMenuItemPressed(ActionEvent event) {
        checkDevice();
    }

    @FXML
    private void reboottwrpButtonPressed(ActionEvent event) {
        if (checkADB()) {
            if (comm.exec("adb devices").contains("recovery")) {
                outputTextArea.setText("Device already in recovery mode!");
            } else {
                displayedcomm.exec("adb reboot recovery");
            }
        }
    }

    @FXML
    private void disableButtonPressed(ActionEvent event) {
        if (checkADB()) {
            if (!comm.exec("adb devices").contains("recovery")) {
                outputTextArea.setText("ERROR: No device found in recovery mode!");
                return;
            }
            comm.exec("adb shell setprop persist.camera.HAL3.enabled 0");
            if (!checkcamera2())
                outputTextArea.setText("Disabled!");
            else outputTextArea.setText("ERROR: Couldn't disable!");
        }
    }

    @FXML
    private void enableButtonPressed(ActionEvent event) {
        if (checkADB()) {
            if (!comm.exec("adb devices").contains("recovery")) {
                outputTextArea.setText("ERROR: No device found in recovery mode!");
                return;
            }
            comm.exec("adb shell setprop persist.camera.HAL3.enabled 1");
            if (checkcamera2())
                outputTextArea.setText("Enabled!");
            else outputTextArea.setText("ERROR: Couldn't enable!");
        }
    }

    @FXML
    private void disableEISButtonPressed(ActionEvent event) {
        if (checkADB()) {
            if (!comm.exec("adb devices").contains("recovery")) {
                outputTextArea.setText("ERROR: No device found in recovery mode!");
                return;
            }
            comm.exec("adb shell setprop persist.camera.eis.enable 0");
            if (!checkEIS())
                outputTextArea.setText("Disabled!");
            else outputTextArea.setText("ERROR: Couldn't disable!");
        }
    }

    @FXML
    private void enableEISButtonPressed(ActionEvent event) {
        if (checkADB()) {
            if (!comm.exec("adb devices").contains("recovery")) {
                outputTextArea.setText("ERROR: No device found in recovery mode!");
                return;
            }
            comm.exec("adb shell setprop persist.camera.eis.enable 1");
            if (checkEIS())
                outputTextArea.setText("Enabled!");
            else outputTextArea.setText("ERROR: Couldn't enable!");
        }
    }

    @FXML
    private void readpropertiesButtonPressed(ActionEvent event) {
        if (checkADB()) {
            displayedcomm.exec("adb shell getprop");
        }
    }

    @FXML
    private void savepropertiesButtonPressed(ActionEvent event) {
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("Text File (.txt)", "*.txt");
        fc.getExtensionFilters().add(fileExtensions);
        fc.setTitle("Save properties");
        File f = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        if (f != null) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(f);
                fw.write(comm.exec("adb shell getprop"));
                fw.flush();
                fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void antirbButtonPressed(ActionEvent event) {
        if (checkFastboot()) {
            displayedcomm.exec("fastboot flash antirbpass dummy.img");
        }
    }

    @FXML
    private void browseimageButtonPressed(ActionEvent event) {
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("Image File", "*.*");
        fc.getExtensionFilters().add(fileExtensions);
        fc.setTitle("Select an image");
        image = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (image != null) {
            imageLabel.setText(image.getName());
        }
    }

    @FXML
    private void flashimageButtonPressed(ActionEvent event) {
        if (image != null && image.getAbsolutePath().length() > 1 && partitionComboBox.getValue() != null && partitionComboBox.getValue().trim().length() > 0 && checkFastboot()) {
            if (autobootCheckBox.isSelected() && partitionComboBox.getValue().trim() == "recovery") {
                displayedcomm.exec(image, "fastboot flash " + partitionComboBox.getValue().trim(), "fastboot boot");
            } else displayedcomm.exec(image, "fastboot flash " + partitionComboBox.getValue().trim());
        }
    }

    @FXML
    private void browseromButtonPressed(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select the root directory of a Fastboot ROM");
        rom = dc.showDialog(((Node) event.getSource()).getScene().getWindow());
        outputTextArea.setText("");
        if (rom != null) {
            if (new File(rom, "images").exists()) {
                romLabel.setText(rom.getName());
                outputTextArea.setText("Fastboot ROM found!");
                new File(rom, "flash_all.sh").setExecutable(true, false);
                new File(rom, "flash_all_lock.sh").setExecutable(true, false);
                if (new File(rom, "flash_all_except_storage.sh").exists())
                    new File(rom, "flash_all_except_storage.sh").setExecutable(true, false);
                else new File(rom, "flash_all_except_data.sh").setExecutable(true, false);
            } else {
                outputTextArea.setText("ERROR: Fastboot ROM not found!");
                rom = null;
            }
        }
    }

    @FXML
    private void flashromButtonPressed(ActionEvent event) {
        if (rom != null && scriptComboBox.getValue() != null && checkFastboot()) {
            FastbootFlasher fs = new FastbootFlasher(progressBar, outputTextArea, rom);
            progressBar.setProgress(0);
            if (scriptComboBox.getValue().equals("Clean install")) fs.exec("flash_all");
            if (scriptComboBox.getValue().equals("Clean install and lock")) fs.exec("flash_all_lock");
            if (scriptComboBox.getValue().equals("Update")) {
                if (new File(rom, "flash_all_except_storage.sh").exists())
                    fs.exec("flash_all_except_storage");
                else fs.exec("flash_all_except_data");
            }
        }
    }

    @FXML
    private void bootButtonPressed(ActionEvent event) {
        if (image != null && image.getAbsolutePath().length() > 1 && checkFastboot()) {
            displayedcomm.exec(image, "fastboot boot");
        }
    }

    @FXML
    private void cacheButtonPressed(ActionEvent event) {
        if (checkFastboot()) {
            displayedcomm.exec("fastboot erase cache");
        }
    }

    @FXML
    private void cachedataButtonPressed(ActionEvent event) {
        if (checkFastboot()) {
            displayedcomm.exec("fastboot erase cache", "fastboot erase userdata");
        }
    }

    @FXML
    private void lockButtonPressed(ActionEvent event) {
        if (checkFastboot()) {
            displayedcomm.exec("fastboot oem lock");
        }
    }

    @FXML
    private void unlockButtonPressed(ActionEvent event) {
        if (checkFastboot()) {
            displayedcomm.exec("fastboot oem unlock");
        }
    }

    @FXML
    private void systemMenuItemPressed(ActionEvent event) {
        if (checkADB()) {
            displayedcomm.exec("adb reboot");
        } else if (checkFastboot()) {
            displayedcomm.exec("fastboot reboot");
        }
    }

    @FXML
    private void recoveryMenuItemPressed(ActionEvent event) {
        if (checkADB()) {
            displayedcomm.exec("adb reboot recovery");
        }
    }

    @FXML
    private void fastbootMenuItemPressed(ActionEvent event) {
        if (checkADB()) {
            displayedcomm.exec("adb reboot bootloader");
        } else if (checkFastboot()) {
            displayedcomm.exec("fastboot reboot bootloader");
        }
    }

    @FXML
    private void edlMenuItemPressed(ActionEvent event) {
        if (checkADB()) {
            displayedcomm.exec("adb reboot edl");
        } else if (checkFastboot()) {
            displayedcomm.exec("fastboot oem edl");
        }
    }

    @FXML
    private void uninstallButtonPressed(ActionEvent event) {
        if (checkADB()) {
            progressBar.setProgress(0);
            uninstaller.uninstall();
        }
    }

    @FXML
    private void addButtonPressed(ActionEvent event) {
        if (customappTextField.getText() != null && customappTextField.getText().trim().length() > 1)
            uninstaller.tv.getItems().add(new App("Custom app", customappTextField.getText().trim()));
        customappTextField.setText(null);
        uninstaller.tv.refresh();
    }

    @FXML
    private void aboutMenuItemPressed(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("About");
        alert.setGraphic(new ImageView(new Image(this.getClass().getClassLoader().getResource("smallicon.png").toString())));
        alert.setHeaderText("Xiaomi ADB/Fastboot Tools" + System.lineSeparator() + "Version 4.1.0" + System.lineSeparator() + "Created by Saki_EU");
        VBox vb = new VBox();
        vb.setAlignment(Pos.CENTER);

        Hyperlink reddit = new Hyperlink("r/Xiaomi on Reddit");
        reddit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.reddit.com/r/Xiaomi"));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
        reddit.setFont(new Font(14));
        Hyperlink discord = new Hyperlink("r/Xiaomi on Discord");
        discord.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://discord.gg/xiaomi"));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
        discord.setFont(new Font(14));
        Hyperlink github = new Hyperlink("This project on GitHub");
        github.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/Saki-EU/XiaomiADBFastbootTools"));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
        github.setFont(new Font(14));

        vb.getChildren().addAll(reddit, discord, github);
        alert.getDialogPane().setContent(vb);
        alert.showAndWait();
    }

}

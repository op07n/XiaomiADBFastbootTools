package app;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;


public class Uninstaller extends Command {

    Thread t;
    TableView<App> tv;
    ProgressBar progress;
    ObservableList<App> apps;
    int n;

    public Uninstaller(TableView<App> table, ProgressBar progress, TextInputControl control) {
        super(control);
        this.progress = progress;
        tv = table;
        pb.redirectErrorStream(false);
    }

    public void loadApps(Device device) {
        apps = FXCollections.observableArrayList();
        apps.add(new App("Analytics", "com.miui.analytics"));
        apps.add(new App("App Vault", "com.miui.personalassistant"));
        apps.add(new App("App Vault", "com.mi.android.globalpersonalassistant"));
        apps.add(new App("Browser", "com.android.browser"));
        apps.add(new App("Calculator", "com.miui.calculator"));
        apps.add(new App("Calendar", "com.android.calendar"));
        apps.add(new App("Cleaner", "com.miui.cleanmaster"));
        apps.add(new App("Clock", "com.android.deskclock"));
        apps.add(new App("Compass", "com.miui.compass"));
        apps.add(new App("Downloads", "com.android.providers.downloads.ui"));
        apps.add(new App("Facebook", "com.facebook.katana"));
        apps.add(new App("Facebook App Installer", "com.facebook.system"));
        apps.add(new App("Facebook App Manager", "com.facebook.appmanager"));
        apps.add(new App("Facebook Services", "com.facebook.services"));
        apps.add(new App("Feedback", "com.miui.bugreport"));
        apps.add(new App("FM Radio", "com.miui.fm"));
        apps.add(new App("Games", "com.xiaomi.glgm"));
        apps.add(new App("Gmail", "com.google.android.gm"));
        apps.add(new App("Google App", "com.google.android.googlequicksearchbox"));
        apps.add(new App("Google Assistant", "com.google.android.apps.googleassistant"));
        apps.add(new App("Google Calculator", "com.google.android.calculator"));
        apps.add(new App("Google Calendar", "com.google.android.calendar"));
        apps.add(new App("Google Chrome", "com.android.chrome"));
        apps.add(new App("Google Clock", "com.google.android.deskclock"));
        apps.add(new App("Google Drive", "com.google.android.apps.docs"));
        apps.add(new App("Google Duo", "com.google.android.apps.tachyon"));
        apps.add(new App("Google Hangouts", "com.google.android.talk"));
        apps.add(new App("Google Indic Keyboard", "com.google.android.apps.inputmethod.hindi"));
        apps.add(new App("Google Keep", "com.google.android.keep"));
        apps.add(new App("Google Korean Input", "com.google.android.inputmethod.korean"));
        apps.add(new App("Google Maps", "com.google.android.apps.maps"));
        apps.add(new App("Google Photos", "com.google.android.apps.photos"));
        apps.add(new App("Google Pinyin Input", "com.google.android.inputmethod.pinyin"));
        apps.add(new App("Google Play Books", "com.google.android.apps.books"));
        apps.add(new App("Google Play Games", "com.google.android.play.games"));
        apps.add(new App("Google Play Movies", "com.google.android.videos"));
        apps.add(new App("Google Play Music", "com.google.android.music"));
        apps.add(new App("Google Zhuyin Input", "com.google.android.apps.inputmethod.zhuyin"));
        apps.add(new App("KLO Bugreport", "com.miui.klo.bugreport"));
        apps.add(new App("MAB", "com.xiaomi.ab"));
        apps.add(new App("Mail", "com.android.email"));
        apps.add(new App("Mi Account", "com.xiaomi.account"));
        apps.add(new App("Mi AI", "com.miui.voiceassist"));
        apps.add(new App("Mi App Store", "com.xiaomi.mipicks"));
        apps.add(new App("Mi Cloud", "com.miui.cloudservice"));
        apps.add(new App("Mi Cloud Backup", "com.miui.cloudbackup"));
        apps.add(new App("Mi Credit", "com.xiaomi.payment"));
        apps.add(new App("Mi Drop", "com.xiaomi.midrop"));
        apps.add(new App("Mi File Manager", "com.mi.android.globalFileexplorer"));
        apps.add(new App("Mi Recycle", "com.xiaomi.mirecycle"));
        apps.add(new App("Mi Roaming", "com.miui.virtualsim"));
        apps.add(new App("Mi Video", "com.miui.video"));
        apps.add(new App("Mi Video", "com.miui.videoplayer"));
        apps.add(new App("Mi Wallet", "com.mipay.wallet"));
        apps.add(new App("MiuiDaemon", "com.miui.daemon"));
        apps.add(new App("Mobile Device Information Provider", "com.amazon.appmanager"));
        apps.add(new App("MSA", "com.miui.msa.global"));
        apps.add(new App("MSA", "com.miui.systemAdSolution"));
        apps.add(new App("Music", "com.miui.player"));
        apps.add(new App("Notes", "com.miui.notes"));
        apps.add(new App("PAI", "android.autoinstalls.config.Xiaomi." + device.codename));
        apps.add(new App("Quick Apps", "com.miui.hybrid"));
        apps.add(new App("Recorder", "com.android.soundrecorder"));
        apps.add(new App("Scanner", "com.xiaomi.scanner"));
        apps.add(new App("Screen Recorder", "com.miui.screenrecorder"));
        apps.add(new App("Search", "com.android.quicksearchbox"));
        apps.add(new App("Weather", "com.miui.weather2"));
        apps.add(new App("Xiaomi Account", "com.xiaomi.vipaccount"));
        apps.add(new App("Xiaomi Service Framework", "com.xiaomi.xmsf"));
        apps.add(new App("Xiaomi SIM Activate Service", "com.xiaomi.simactivate.service"));
        apps.add(new App("Yellow Pages", "com.miui.yellowpage"));
        apps.add(new App("YouTube", "com.google.android.youtube"));

        createTable();
    }

    public void createTable() {
        String installed = new Command().exec("adb shell pm list packages");
        for (Iterator<App> iterator = apps.iterator(); iterator.hasNext(); ) {
            if (!installed.contains(iterator.next().packagenameProperty().get() + System.lineSeparator()))
                iterator.remove();
        }
        tv.setItems(apps);
        tv.refresh();
    }

    public void uninstall() {
        ObservableList<App> undesirable = FXCollections.observableArrayList();
        if (tv.getItems().size() != 0) {
            for (App app : tv.getItems()) {
                if (app.selectedProperty().get())
                    undesirable.add(app);
            }
            if (undesirable.size() == 0)
                return;
        } else return;
        n = undesirable.size();
        tic.setText("");
        t = new Thread(() -> {
            for (App app : undesirable) {
                arguments = ("adb shell pm uninstall --user 0 " + app.packagenameProperty().get()).split(" ");
                arguments[0] = prefix + arguments[0];
                pb.command(arguments);
                try {
                    proc = pb.start();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                scan = new Scanner(proc.getInputStream());
                String line = "";
                while (scan.hasNext())
                    line += scan.nextLine() + System.lineSeparator();
                scan.close();
                final String finalline = line;
                Platform.runLater(() -> {
                    tic.appendText("App: " + app.appnameProperty().get() + System.lineSeparator());
                    tic.appendText("Package: " + app.packagenameProperty().get() + System.lineSeparator());
                    tic.appendText("Result: " + finalline + System.lineSeparator());
                    progress.setProgress(progress.getProgress() + (1.0 / n));
                });
            }
            Platform.runLater(() -> {
                tic.appendText("Done!");
                progress.setProgress(0);
                createTable();
            });
        });
        t.setDaemon(true);
        t.start();
    }
}
package app;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputControl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class FastbootFlasher {

    ProcessBuilder pb;
    Process proc;
    TextInputControl tic;
    Scanner scan;
    File directory;
    Thread t;
    ProgressBar progress;

    public FastbootFlasher(ProgressBar prog, TextInputControl control, File dir) {
        pb = new ProcessBuilder();
        directory = dir;
        pb.directory(new File(System.getProperty("user.home") + "/temp"));
        tic = control;
        progress = prog;
        pb.redirectErrorStream(true);
    }

    private int getCmdCount(File file) {
        try {
            scan = new Scanner(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int cnt = 0;
        while (scan.hasNext())
            if (scan.nextLine().contains("fastboot"))
                cnt++;
        scan.close();
        return cnt;
    }

    public void exec(String arg) {
        tic.setText("");
        File script;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            script = new File(directory, arg + ".bat");
            pb.command("cmd.exe", "/c", script.getAbsolutePath());
        } else {
            script = new File(directory, arg + ".sh");
            pb.command("sh", "-c", script.getAbsolutePath());
        }
        ;
        int n = getCmdCount(script);
        try {
            proc = pb.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        scan = new Scanner(proc.getInputStream());
        t = new Thread(() -> {
            while (scan.hasNext()) {
                String line = scan.nextLine() + System.lineSeparator();
                if (line.contains("pause"))
                    break;
                Platform.runLater(() -> {
                    tic.appendText(line);
                    if (line.contains("fastboot"))
                        progress.setProgress(progress.getProgress() + (1.0 / n));
                });
            }
            scan.close();
            Platform.runLater(() -> {
                tic.appendText(System.lineSeparator() + "Done!");
                progress.setProgress(0);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    public void waitFor() {
        try {
            proc.waitFor();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}

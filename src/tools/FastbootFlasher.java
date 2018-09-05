package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputControl;

public class FastbootFlasher {
	
	ProcessBuilder pb;
    Process proc;
    TextInputControl tic;
    String prefix;
    Scanner scan;
    Thread t;
    ProgressBar progress;
    
    public FastbootFlasher(ProgressBar prog, TextInputControl control, File dir){
        pb = new ProcessBuilder();
        pb.directory(dir);
        tic = control;
        progress = prog;
        pb.redirectErrorStream(true);
    }
    
    public void exec(String arg){
    	tic.setText("");
        if (System.getProperty("os.name").toLowerCase().contains("win"))
        	pb.command("cmd.exe", "/c", arg + ".bat");
        else pb.command("sh", "-c", "./" + arg + ".sh");
        try {
            proc = pb.start();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        t = new Thread(() -> {
            while(true){
                try {
                    final int c = br.read();
                    if (c == -1)
                        break;
                    Platform.runLater(() -> {
                        tic.appendText("" + (char)c);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(() -> {
                tic.appendText(System.lineSeparator() + "Done!");
                progress.setProgress(0);
            });
        });
        t.setDaemon(true);
        t.start();
    }
    
    public void waitFor(){
        try {
            proc.waitFor();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}

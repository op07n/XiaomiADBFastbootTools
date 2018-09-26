package tools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javafx.scene.control.TextInputControl;

public class Command {

    protected ProcessBuilder pb;
    protected Process proc;
    protected Scanner scan;
    protected String output;
    protected TextInputControl tic;
    protected String prefix;
    protected String[] arguments;

    public Command() {
        pb = new ProcessBuilder();
        pb.directory(new File(System.getProperty("user.home") + "/temp"));
        tic = null;
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            prefix = System.getProperty("user.home") + "/temp/";
        else prefix = "./";
    }

    public Command(TextInputControl control) {
        pb = new ProcessBuilder();
        pb.directory(new File(System.getProperty("user.home") + "/temp"));
        tic = control;
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            prefix = System.getProperty("user.home") + "/temp/";
        else prefix = "./";
    }

    public String exec(String arg) {
        arguments = arg.split(" ");
        arguments[0] = prefix + arguments[0];
        pb.command(arguments);
        pb.redirectErrorStream(true);
        output = "";
        if (tic != null)
            tic.setText("");
        try {
            proc = pb.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        scan = new Scanner(proc.getInputStream());
        String line;
        while (scan.hasNext()) {
            line = scan.nextLine() + System.lineSeparator();
            output += line;
            if (tic != null)
                tic.appendText(line);
        }
        scan.close();
        return output;
    }

    public String exec(File image, String arg) {
        arguments = arg.split(" ");
        arguments[0] = prefix + arguments[0];
        pb.command(arguments);
        if (image != null)
            pb.command().add(image.getAbsolutePath());
        pb.redirectErrorStream(true);
        output = "";
        if (tic != null)
            tic.setText("");
        try {
            proc = pb.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        scan = new Scanner(proc.getInputStream());
        String line;
        while (scan.hasNext()) {
            line = scan.nextLine() + System.lineSeparator();
            output += line;
            if (tic != null)
                tic.appendText(line);
        }
        scan.close();
        return output;
    }

    public String exec(String arg, boolean err) {
        arguments = arg.split(" ");
        arguments[0] = prefix + arguments[0];
        pb.command(arguments);
        pb.redirectErrorStream(false);
        output = "";
        if (tic != null)
            tic.setText("");
        try {
            proc = pb.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (err)
            scan = new Scanner(proc.getErrorStream());
        else
            scan = new Scanner(proc.getInputStream());
        String line;
        while (scan.hasNext()) {
            line = scan.nextLine() + System.lineSeparator();
            output += line;
            if (tic != null) {
                tic.appendText(line);
            }
        }
        scan.close();
        return output;
    }

    public String exec(String... args) {
        pb.redirectErrorStream(true);
        output = "";
        if (tic != null)
            tic.setText("");
        for (String s : args) {
            arguments = s.split(" ");
            arguments[0] = prefix + arguments[0];
            pb.command(arguments);
            try {
                proc = pb.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            scan = new Scanner(proc.getInputStream());
            String line;
            while (scan.hasNext()) {
                line = scan.nextLine() + System.lineSeparator();
                output += line;
                if (tic != null)
                    tic.appendText(line);
            }
            scan.close();
            try {
                proc.waitFor();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return output;
    }

    public String exec(File image, String... args) {
        pb.redirectErrorStream(true);
        output = "";
        if (tic != null)
            tic.setText("");
        for (String s : args) {
            arguments = s.split(" ");
            arguments[0] = prefix + arguments[0];
            pb.command(arguments);
            if (image != null)
                pb.command().add(image.getAbsolutePath());
            try {
                proc = pb.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            scan = new Scanner(proc.getInputStream());
            String line;
            while (scan.hasNext()) {
                line = scan.nextLine() + System.lineSeparator();
                output += line;
                if (tic != null)
                    tic.appendText(line);
            }
            scan.close();
            try {
                proc.waitFor();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return output;
    }

    public void waitFor() {
        try {
            proc.waitFor();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void kill() {
        proc.destroy();
        if (proc.isAlive())
            proc.destroyForcibly();
    }
}

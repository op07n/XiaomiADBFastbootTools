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
    protected List<String> comm;
    
    public Command(){
        pb = new ProcessBuilder();
        pb.directory(new File(System.getProperty("user.home") + "/temp"));
        comm = null;
        tic = null;
        if (System.getProperty("os.name").toLowerCase().contains("win"))
        	prefix = System.getProperty("user.home") + "/temp/";
        else prefix = "./";
    }
    
    public Command(TextInputControl control){
        pb = new ProcessBuilder();
        pb.directory(new File(System.getProperty("user.home") + "/temp"));
        comm = null;
        tic = control;
        if (System.getProperty("os.name").toLowerCase().contains("win"))
        	prefix = System.getProperty("user.home") + "/temp/";
        else prefix = "./";
    }
    
    public String exec(String arg){
        pb.command(Arrays.asList((prefix + arg).split(" ")));
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
        while (scan.hasNext()){
            line = scan.nextLine() + System.lineSeparator();
            output += line;
            if (tic != null)
             tic.appendText(line);
        }
        scan.close();
        return output;
    }
    
    public String exec(File image, String arg){
    	comm = new LinkedList<String>(Arrays.asList((prefix + arg).split(" ")));
    	if (image != null)
    		comm.add(image.getAbsolutePath());
        pb.command(comm);
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
        while (scan.hasNext()){
            line = scan.nextLine() + System.lineSeparator();
            output += line;
            if (tic != null)
             tic.appendText(line);
        }
        scan.close();
        return output;
    }
    
    public String exec(String arg, boolean err){
        pb.command(Arrays.asList((prefix + arg).split(" ")));
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
    
    public String exec(String... args){
        pb.redirectErrorStream(true);
        output = "";
        if (tic != null)
            tic.setText("");
        for (String s : args){
            pb.command(Arrays.asList((prefix + s).split(" ")));
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
    
    public String exec(File image, String... args){
        pb.redirectErrorStream(true);
        output = "";
        if (tic != null)
            tic.setText("");
        for (String s : args){
        	comm = new LinkedList<String>(Arrays.asList((prefix + s).split(" ")));
        	if (image != null)
        		comm.add(image.getAbsolutePath());
            pb.command(comm);
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
    
    public void waitFor(){
        try {
            proc.waitFor();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void kill(){
        proc.destroy();
        if (proc.isAlive())
            proc.destroyForcibly();
    }
}

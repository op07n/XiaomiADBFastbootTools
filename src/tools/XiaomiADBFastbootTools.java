package tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


public class XiaomiADBFastbootTools extends Application {
    
	public void createFile(String file, boolean exec){
        File temp = new File(System.getProperty("user.dir") + "/temp");
        temp.mkdir();
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(file));
        } catch (IOException ex) {
        	ex.getMessage();
        }
        if (file.lastIndexOf("/") != -1)
            file = file.substring(file.lastIndexOf("/")+1);
        File newfile = new File(System.getProperty("user.dir") + "/temp/" + file);
        if (!newfile.exists()) {
            try {
            	newfile.createNewFile();
                FileOutputStream fos = new FileOutputStream(newfile);
                fos.write(bytes);
                fos.flush();
                fos.close();
            } catch (IOException ex) {
            	ex.getMessage();
            }
	  }
        newfile.setExecutable(exec, false);
    }
    
    public void setupFiles(){
        String os = System.getProperty("os.name").toLowerCase();
        createFile("dummy.img", false);
        if (os.contains("win")){
            createFile("windows/adb.exe", true);
            createFile("windows/fastboot.exe", true);
            createFile("windows/AdbWinApi.dll", false);
            createFile("windows/AdbWinUsbApi.dll", false);
        }
        if (os.contains("mac")){
            createFile("macos/adb", true);
            createFile("macos/fastboot", true);
        }
        if (os.contains("linux")){
            createFile("linux/adb", true);
            createFile("linux/fastboot", true);
        }
        Thread t = new Thread(() -> {
        	Command comm = new Command();
            comm.exec("adb start-server");
        });
        t.start();
    }
	
	@Override
    public void start(Stage stage) throws Exception {
        setupFiles();
		Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
		
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("Xiaomi ADB/Fastboot Tools");
        stage.getIcons().add(new Image(this.getClass().getClassLoader().getResource("icon.png").toString()));
        stage.show();
        stage.setResizable(false);
    }
    
    
    @Override
    public void stop(){
        Command comm = new Command();
        comm.exec("adb kill-server");
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        	ex.getMessage();
        }
        try {
            FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/temp"));
        } catch (IOException ex) {
        	ex.getMessage();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}

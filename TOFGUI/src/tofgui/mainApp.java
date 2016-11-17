/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tofgui;

import java.time.Duration;
import java.util.Calendar;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tof1.*;

/**
 *
 * @author admin
 */
public class mainApp extends Application {
    
    public static CPU cpu;
    private int _interval = 1000;
    public TOFGUIController _controller;
    @Override
    public void start(Stage stage) throws Exception {
        cpu = new CPU("COM4");
        cpu.readConfig();
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(mainApp.class.getResource("TOFGUI.fxml"));
        
        Parent root = loader.load();
        
        _controller = loader.getController();
        _controller.setMain(this);
        
        _controller.addLog("interval: " + _interval);
        _controller.printLog();
        
        Scene scene = new Scene(root);
        stage.setTitle("TOFGUI");
        stage.setScene(scene);
        stage.show();
        Thread thread = new Thread(task);
        thread.start();
        
//        try{
//        // Give the controller access to the main app.
//        TOFGUIController controller = loader.getController();
////        controller.setMain(this);
//        controller.run();
//        }catch(Exception e){
//            System.out.println("Error: " + e);
//        }
        
//        controller.setMain(this);
//        Thread updateThread = new Thread(() -> {
//        while (true) {
//            try {
//              Thread.sleep(1000);
//              controller.run();
//            } catch (InterruptedException e) {
//              throw new RuntimeException(e);
//            }
//        }
//        });
//        updateThread.setDaemon(true);
//        updateThread.start();
    }

    Task task = new Task() {
    @Override protected Integer call() throws Exception {
        while(true){
            try{             
                _controller.update();
            }catch(Exception e){
                System.out.println("Error: " + e);
            }
//            if(isCancelled())
//                break;
            Thread.sleep(_interval);
        }
//        return 0;
    }
    };
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}



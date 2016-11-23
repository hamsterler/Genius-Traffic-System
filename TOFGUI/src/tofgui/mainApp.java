/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tofgui;

import javafx.scene.canvas.*;
import javafx.scene.canvas.GraphicsContext;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tof1.*;

/**
 *
 * @author admin
 */
public class mainApp extends Application {

    public int interval = 300;
    public TOFGUIController _controller;
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader;
        AnchorPane root;

        loader = new FXMLLoader();
        loader.setLocation(mainApp.class.getResource("TOFGUI.fxml"));       
        root = loader.load();

        _controller = loader.getController();  //<<Note:Have to assign this after loader.load()
        _controller.setMain(this);
        
        _controller.addLog("interval: " + interval + '\n');
        
        _controller.anchorPane.setStyle("-fx-background-color: #5D6D7E");
        

        Scene scene = new Scene(root);
        stage.setTitle("TOFGUI");
        stage.setScene(scene);
        stage.show();
       
        
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    Task task = new Task<Void>() {
    @Override public Void call() {
        while(true){
            try{             
                _controller.update();
            }catch(Exception e){
                System.out.println("Error: " + e);
            }
            if(isCancelled())
                break;
        }
        return null;
    }
    };
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tofgui;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
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
//        try{
        FXMLLoader loader;
        AnchorPane root;
        loader = new FXMLLoader();
        loader.setLocation(mainApp.class.getResource("TOFGUI.fxml"));       
        root = loader.load();

        _controller = loader.getController();  //<<Note:Have to assign this after loader.load()
        _controller.setMain(this);    
        _controller.addLog("interval: " + interval + '\n');  
        _controller.anchorPane.setStyle("-fx-background-color:	#808B96");//#5D6D7E
        
        Scene scene = new Scene(root);
        stage.setTitle("TOFGUI");
        stage.setScene(scene);
        stage.show();
              
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
//        }catch(Exception ex){
//            System.out.println("Error: " + ex.getMessage());
//        }
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
    
 
    public static void main(String[] args) {
        launch(args);
    }
}



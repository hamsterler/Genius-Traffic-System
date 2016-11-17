/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tofgui;

import javafx.scene.paint.*;
import javafx.scene.canvas.*;
import javafx.scene.canvas.GraphicsContext;
import java.awt.Rectangle;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tof1.CPU;

/**
 *
 * @author admin
 */
public class mainApp extends Application {
    public Draw draw;
    public static CPU cpu;
    private int _interval = 1000;
    public TOFGUIController _controller;
    @Override
    public void start(Stage stage) throws Exception {
        cpu = new CPU("COM4");
        cpu.readConfig();
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(mainApp.class.getResource("TOFGUI.fxml"));
        
        AnchorPane root = loader.load();
        
        _controller = loader.getController();  //<<Note:Have to assign this after loader.load()
        _controller.setMain(this);
        
        _controller.addLog("interval: " + _interval);
        _controller.printLog();
        
        _controller.anchorPane.setStyle("-fx-background-color: #b3d9ff");
        Pane canvasPane = new Pane();
        canvasPane.setPrefWidth(777);
        canvasPane.setPrefHeight(323);
        canvasPane.setStyle("-fx-background-color: #ffffff");
        draw = new Draw(777, 323, 300, 8);
//        draw.draw();
        canvasPane.getChildren().add(draw.getCanvas());
        _controller.canvasPane.getChildren().add(canvasPane);
//        Circle circle = new Circle(50,Color.BLUE);
//        circle.relocate(20, 20);
//        Rectangle rectangle = new Rectangle(100,100,Color.RED);
//        rectangle.relocate(70,70);
//        canvas.getChildren().addAll(circle,rectangle);
        
        
        Scene scene = new Scene(root);
        stage.setTitle("TOFGUI");
        stage.setScene(scene);
        stage.show();
        
        
        Thread thread = new Thread(task);
        thread.start();
      
    }

   
    
    
    Task task = new Task() {
    @Override protected Integer call() throws Exception {
        while(true){
            try{             
                _controller.update();
            }catch(Exception e){
                System.out.println("Error: " + e);
            }
            if(isCancelled())
                break;

        }
        return 0;
    }
    };
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}



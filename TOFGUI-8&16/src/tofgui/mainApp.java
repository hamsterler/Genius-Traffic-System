package tofgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class mainApp extends Application {

  
    public TOFGUIController _controller;
    @Override
    public void start(Stage stage) throws Exception {
        try{
            FXMLLoader loader;
            AnchorPane root;
            loader = new FXMLLoader();
            loader.setLocation(mainApp.class.getResource("TOFGUI.fxml"));       
            root = loader.load();
            _controller = loader.getController();  //<<Note:must assign this after loader.load()          
            _controller.anchorPane.setStyle("-fx-background-color:	#808B96");

            Scene scene = new Scene(root);
            stage.setTitle("TOFGUI");
            stage.setScene(scene);
            stage.show();
        }catch(Exception ex){
            System.out.println("Error: " + ex.getMessage());
        }          
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}



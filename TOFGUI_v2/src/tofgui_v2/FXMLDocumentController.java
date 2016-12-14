/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tofgui_v2;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;

/**
 *
 * @author admin
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML public AnchorPane ap;
    
    @FXML public Canvas canvasLine;
    @FXML public Canvas canvasDetect;
    @FXML public Canvas canvasCam;
    @FXML public Canvas canvasSquare;
    @FXML public Pane detectPane;
    @FXML public Pane linePane;
    @FXML public Button pauseButton;
    public boolean pause = false;
    @FXML
    private void handlePauseButtonAction(ActionEvent event) {
        if(!pause){
            
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        linePane.setStyle("-fx-background-color: #FFFFFF");
        detectPane.setStyle("-fx-background-color: #FFFFFF");
        pauseButton.setOnAction(this::handlePauseButtonAction); 
    }    
    
}

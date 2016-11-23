/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package randompassword;

import java.awt.Insets;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author admin
 */
public class FXMLDocumentController implements Initializable {
    
    
    @FXML private TextArea textArea;
    @FXML
    private Label label;
    @FXML private TextField length;
    @FXML private TextField number;
    @FXML private TextField path;    
    @FXML
    private void handleButtonAction(ActionEvent event) throws UnsupportedEncodingException, IOException {
//        System.out.println("You clicked me!");
        
        String status = "Done!!";
         
        try{ 
            int len = Integer.parseInt(length.getText());
            int num = Integer.parseInt(number.getText()); 
            if(len < 4){
                status = "Please Enter length >= 4";
                label.setText(status);
                return;
            }               
            String pa = path.getText();
            byte[] write = new GenPassword().genPasswordToFile(num, len, pa);
            textArea.setText(new String(write,"ISO-8859-1"));
        }
        catch(Exception e){
            status = "Error: " + e.getMessage();
        }
        
        label.setText(status);
               
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        textArea.setEditable(false);
    }    
    
}

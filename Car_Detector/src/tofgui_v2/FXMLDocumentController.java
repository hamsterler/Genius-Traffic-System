package tofgui_v2;


import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

public class FXMLDocumentController implements Initializable {
    
    @FXML public AnchorPane ap;
    @FXML public Text status; 
    @FXML public Canvas canvasLine;
    @FXML public Canvas canvasDetect;
    @FXML public Canvas canvasCam;
    @FXML public Canvas canvasSquare;
    @FXML public Pane detectPane;
    @FXML public Pane linePane;
    @FXML public Button pauseButton;
    @FXML public Button resetButton;
    @FXML public Button countResetButton;
    @FXML public Button loadConfigButton;
    @FXML public TextField lane1;
    @FXML public TextField lane2;
    @FXML public TextField lane3;
    @FXML public TextField lane4;
    @FXML public TextField lane5;
    @FXML public TextField lane6;
    @FXML public Text text1;
    @FXML public Text text2;
    @FXML public Text text3;
    @FXML public Text text4;
    @FXML public Text text5;
    @FXML public Text text6;
    @FXML public TextArea distanceTextArea;
    @FXML public TextField total;
    
    @FXML public TextField max1;
    @FXML public TextField max2;
    @FXML public TextField max3;
    @FXML public TextField max4;
    @FXML public TextField max5;
    @FXML public TextField max6;
    @FXML public TextField max7;
    @FXML public TextField max8;
    @FXML public TextField max9;
    @FXML public TextField max10;
    @FXML public TextField max11;
    @FXML public TextField max12;
    @FXML public TextField max13;
    @FXML public TextField max14;
    @FXML public TextField max15;
    @FXML public TextField max16;
    
    @FXML public TextField min1;
    @FXML public TextField min2;
    @FXML public TextField min3;
    @FXML public TextField min4;
    @FXML public TextField min5;
    @FXML public TextField min6;
    @FXML public TextField min7;
    @FXML public TextField min8;
    @FXML public TextField min9;
    @FXML public TextField min10;
    @FXML public TextField min11;
    @FXML public TextField min12;
    @FXML public TextField min13;
    @FXML public TextField min14;
    @FXML public TextField min15;
    @FXML public TextField min16;
    
    public boolean pause = false;
    @FXML
    private void handlePauseButtonAction(ActionEvent event) {
        if(!pause){
            
        }
    }
    
    public TextField[] lane;
    public Text[] text;
    public TextField[] max;
    public TextField[] min;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        linePane.setStyle("-fx-background-color: #FFFFFF");
        detectPane.setStyle("-fx-background-color: #FFFFFF");
        pauseButton.setOnAction(this::handlePauseButtonAction); 
        
        lane = new TextField[6];
        lane[0] = lane1;
        lane[1] = lane2;
        lane[2] = lane3;
        lane[3] = lane4;
        lane[4] = lane5;
        lane[5] = lane6;
        
        text = new Text[6];
        text[0] = text1;
        text[1] = text2;
        text[2] = text3;
        text[3] = text4;
        text[4] = text5;
        text[5] = text6;
        
        max = new TextField[16];
        max[0] = max1;
        max[1] = max2;
        max[2] = max3;
        max[3] = max4;
        max[4] = max5;
        max[5] = max6;
        max[6] = max7;
        max[7] = max8;
        max[8] = max9;
        max[9] = max10;
        max[10] = max11;
        max[11] = max12;
        max[12] = max13;
        max[13] = max14;
        max[14] = max15;
        max[15] = max16;
        
        min= new TextField[16];
        min[0] = min1;
        min[1] = min2;
        min[2] = min3;
        min[3] = min4;
        min[4] = min5;
        min[5] = min6;
        min[6] = min7;
        min[7] = min8;
        min[8] = min9;
        min[9] = min10;
        min[10] = min11;
        min[11] = min12;
        min[12] = min13;
        min[13] = min14;
        min[14] = min15;
        min[15] = min16;
//        
        for(int i = 0; i < 16; i++){
            max[i].setEditable(false);
            min[i].setEditable(false);
        }
        for(int i = 0; i < 6; i++){
            text[i].setVisible(false);
            lane[i].setVisible(false);
        }
    }    
    
}

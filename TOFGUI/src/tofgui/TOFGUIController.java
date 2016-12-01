/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tofgui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import tof1.*;
/**
 *
 * @author admin
 */
public class TOFGUIController implements Initializable {
    
    @FXML
    public StackPane canvasPane;
    @FXML
    public AnchorPane anchorPane;
    
    @FXML GridPane  gridpane;
    
    @FXML private Text detect;
    @FXML private Pane drawPane;
//    @FXML public Text detect2 = new Text();
    
    
    @FXML private TextField value1 = new TextField();
    @FXML private TextField value2 = new TextField();
    @FXML private TextField value3 = new TextField();
    @FXML private TextField value4 = new TextField();
    @FXML private TextField value5 = new TextField();
    @FXML private TextField value6 = new TextField();
    @FXML private TextField value7 = new TextField();
    @FXML private TextField value8 = new TextField();
    
    @FXML private TextField max1 = new TextField();
    @FXML private TextField max2 = new TextField();
    @FXML private TextField max3 = new TextField();
    @FXML private TextField max4 = new TextField();
    @FXML private TextField max5 = new TextField();
    @FXML private TextField max6 = new TextField();
    @FXML private TextField max7 = new TextField();
    @FXML private TextField max8 = new TextField();
    
    @FXML private TextField min1 = new TextField();
    @FXML private TextField min2 = new TextField();
    @FXML private TextField min3 = new TextField();
    @FXML private TextField min4 = new TextField();
    @FXML private TextField min5 = new TextField();
    @FXML private TextField min6 = new TextField();
    @FXML private TextField min7 = new TextField();
    @FXML private TextField min8 = new TextField();

    @FXML private TextArea textArea;
    
    String log = "";
    
    @FXML private Text t1;
    @FXML private Text t2;
    @FXML private Text t3;
    @FXML private Text t4;
    @FXML private Text t5;
    @FXML private Text t6;
    @FXML private Text t7;
    @FXML private Text t8;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        String[] mx = new String[8];
        String[] mn = new String[8];
        mx[0] = max1.getText().trim();
        mx[1] = max2.getText().trim();
        mx[2] = max3.getText().trim();
        mx[3] = max4.getText().trim();
        mx[4] = max5.getText().trim();
        mx[5] = max6.getText().trim();
        mx[6] = max7.getText().trim();
        mx[7] = max8.getText().trim();
        
        mn[0] = min1.getText().trim();
        mn[1] = min2.getText().trim();
        mn[2] = min3.getText().trim();
        mn[3] = min4.getText().trim();
        mn[4] = min5.getText().trim();
        mn[5] = min6.getText().trim();
        mn[6] = min7.getText().trim();
        mn[7] = min8.getText().trim();
                
        byte[] min = new byte[16];
        byte[] max = new byte[16];
        try{
            for(int i = 0; i < 8;i++){
                min[2*i] = (byte)Integer.parseInt(mn[i]);
                min[2*i + 1] = (byte)Integer.parseInt(mn[i]);
                if(mn[i].isEmpty()){
                    min[2*i] = 0;
                    min[2*i + 1] = 0;
                }
                else{
                    min[2*i] = (byte)Integer.parseInt(mn[i]);
                    min[2*i + 1] = (byte)Integer.parseInt(mn[i]);
                }
                if(mx[i].isEmpty()){
                    max[2*i] = 0;
                    max[2*i + 1] = 0;
                }
                else{
                    max[2*i] = (byte)Integer.parseInt(mx[i]);
                    max[2*i + 1] = (byte)Integer.parseInt(mx[i]);
                }
            }
//            for(int i = 0; i < min.length; i++){
//                if(i >= 8){
//                    min[i] = (byte)this.cpu.getMin()[i];
//                    max[i] = (byte)this.cpu.getMax()[i];
//                }
//                else{               
//                    if(mn[i].isEmpty())
//                        min[i] = 0;
//                    else
//                        min[i] = (byte)(Integer.parseInt(mn[i]));
//
//                    if(mx[i].isEmpty())
//                        max[i] = 0;
//                    else
//                        max[i] = (byte)(Integer.parseInt(mx[i]));
//                }
//            }
        }catch(NumberFormatException e){
           addLog("Please enter numbers only." + '\n');
           showMinMax();
           return;
        }        
        CPU cpu = this.cpu;
        //--set min max and send data to board--
        this.cpu.setMinMax(min, max);
        while(!this.cpu.sendData()){}
        addLog("Set Min&Max success!!" + '\n');
        //--------------------------------------
        
        System.out.print("Min = ");
        for(int i = 0; i < cpu.Min.length ; i++){
            System.out.print((int)cpu.Min[i] + " ");
        }
        System.out.println();
        System.out.print("Max = ");
        for(int i = 0; i < cpu.Max.length ; i++){
            System.out.print((int)cpu.Max[i] + " ");
        }
        System.out.println();
        
       
//        System.out.println("Update");
//        update();
        showMinMax();
        System.out.println("status: " + cpu.getStatus());
    }
    
    private mainApp _main;  
    public void setMain(mainApp main)
    {
        this._main = main;
    }
    
    public void addLog(String message){
        textArea.appendText(message);
    }
    
    
    
    public synchronized void update() {
        int[] buffer = null;
        try{
            buffer = this.cpu.getDistanceInt(this._main.interval);
        }catch(NullPointerException e){ 
            System.out.println("Error: getDistance");
            return;
        }
        int[] data = new int[16];
        System.arraycopy(buffer, 0, data, 0,data.length);
        int isDetect = buffer[16];
        if(data == null){
            System.out.println("Data = null!!");
            return;
        }
        
        int num_line = this._draw.max_num_line;
        this._draw.clearCanvas();
        this._draw.draw();
        this._draw.drawMinMaxLine(16, this.cpu.getMin(), this.cpu.getMax(), Color.valueOf("#3498DB"), 4);
        this._draw.drawDistancePoint(16, data, this.cpu.getMax(), Color.RED.brighter());
        try{
            if(isDetect == 1){ //found
                detect.setText("Detected!");
                detect.setFill(Color.valueOf("#E74C3C"));
            }else{
                detect.setText("No Object Found");
                detect.setFill(Color.valueOf("#5D6D7E"));
            }     
            t1.setText(Integer.toString(data[0]));
            t2.setText(Integer.toString(data[1]));
            t3.setText(Integer.toString(data[2]));
            t4.setText(Integer.toString(data[3]));
            t5.setText(Integer.toString(data[4]));
            t6.setText(Integer.toString(data[5]));
            t7.setText(Integer.toString(data[6]));
            t8.setText(Integer.toString(data[7]));

        }
        catch(NullPointerException e){  
            System.out.println("Error: " +  e.getMessage());        
        }
    }
    
    public void run(){
//        int count = 1;
//        while(count < 10){
//            String print = count + "";
//            value1.setText(new String(print));
//            count++;
//        }   
        System.out.println("Hello");
    }
    public void showMinMax(){  //ver2
        CPU cpu = this.cpu;
        int[] min = cpu.getMin();
        int[] max = cpu.getMax();
        try{
        min1.setText("" + min[0]);
        min2.setText("" + min[2]);
        min3.setText("" + min[4]);
        min4.setText("" + min[6]);
        min5.setText("" + min[8]);
        min6.setText("" + min[10]);
        min7.setText("" + min[12]);
        min8.setText("" + min[14]);
        
        max1.setText("" + max[0]);
        max2.setText("" + max[2]);
        max3.setText("" + max[4]);
        max4.setText("" + max[6]);
        max5.setText("" + max[8]);
        max6.setText("" + max[10]);
        max7.setText("" + max[12]);
        max8.setText("" + max[14]);
        }catch(NullPointerException e){
            System.out.println("Error: " +  e.getMessage());
        }
    } 
//    public void showMinMax(){
//        CPU cpu = this.cpu;
//        int[] min = cpu.getMin();
//        int[] max = cpu.getMax();
//        try{
//        min1.setText("" + min[0]);
//        min2.setText("" + min[1]);
//        min3.setText("" + min[2]);
//        min4.setText("" + min[3]);
//        min5.setText("" + min[4]);
//        min6.setText("" + min[5]);
//        min7.setText("" + min[6]);
//        min8.setText("" + min[7]);
//        
//        max1.setText("" + max[0]);
//        max2.setText("" + max[1]);
//        max3.setText("" + max[2]);
//        max4.setText("" + max[3]);
//        max5.setText("" + max[4]);
//        max6.setText("" + max[5]);
//        max7.setText("" + max[6]);
//        max8.setText("" + max[7]);
//        }catch(NullPointerException e){
//            System.out.println("Error: " +  e.getMessage());
//        }
//    }   
    
    
    public CPU cpu;
    private Draw _draw;
//    public Text detect = new Text();
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cpu = new CPU("COM4");
        cpu.readConfig();
        
        
//        Pane canvasPane = new Pane();
//        canvasPane.setPrefWidth(777);
//        canvasPane.setPrefHeight(323);
//        canvasPane.setStyle("-fx-background-color: #EBEDEF");
//        drawPane.setPrefWidth(777);
//        drawPane.setPrefHeight(323);
        drawPane.setStyle("-fx-background-color: #EBEDEF");
        _draw = new Draw(777, 323, 300, 16);
        drawPane.getChildren().add(_draw.getCanvas());
        
        textArea.setEditable(false);
        value1.setEditable(false);
        value2.setEditable(false);
        value3.setEditable(false);
        value4.setEditable(false);
        value5.setEditable(false);
        value6.setEditable(false);
        value7.setEditable(false);
        value8.setEditable(false);
        
        showMinMax();
        this.cpu.sendData();
    }  
    
    public void isDetect(int value){
        if(value == 0){ //found
//            detect.setTextFill(Color.valueOf("#F1948A"));
            detect.setText("Detected!");           
        }else{
            detect.setText("Not Found");
        }       
    }
    
}

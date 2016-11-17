/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tofgui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import tof1.*;
/**
 *
 * @author admin
 */
public class TOFGUIController implements Initializable {
    
    @FXML
    public Pane canvasPane;
    @FXML
    public AnchorPane anchorPane;
    
    @FXML
    private TextField value1;
    @FXML
    private TextField value2;
    @FXML
    private TextField value3;
    @FXML
    private TextField value4;
    @FXML
    private TextField value5;
    @FXML
    private TextField value6;
    @FXML
    private TextField value7;
    @FXML
    private TextField value8;
    @FXML
    private TextField max1;
    @FXML
    private TextField max2;
    @FXML
    private TextField max3;
    @FXML
    private TextField max4;
    @FXML
    private TextField max5;
    @FXML
    private TextField max6;
    @FXML
    private TextField max7;
    @FXML
    private TextField max8;
    
    @FXML
    private TextField min1;
    @FXML
    private TextField min2;
    @FXML
    private TextField min3;
    @FXML
    private TextField min4;
    @FXML
    private TextField min5;
    @FXML
    private TextField min6;
    @FXML
    private TextField min7;
    @FXML
    private TextField min8;

    @FXML 
    private TextArea textArea;
    
    String log = "";
    
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
            for(int i = 0; i < min.length; i++){
                if(i >= 8){
                    min[i] = (byte)i;
                    max[i] = (byte)i;
                }
                else{               
                    if(mn[i].isEmpty())
                        min[i] = 0;
                    else
                        min[i] = (byte)(Integer.parseInt(mn[i]));

                    if(mx[i].isEmpty())
                        max[i] = 0;
                    else
                        max[i] = (byte)(Integer.parseInt(mx[i]));
                }
            }
        }catch(NumberFormatException e){
           log += "Please enter numbers only." + '\n';
           printLog();
           showMinMax();
           return;
        }        
        CPU cpu = _main.cpu;
        cpu.setMinMax(min, max);
            
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
        log += "Set Min&Max success!!" + '\n';
        printLog();
//        System.out.println("Update");
//        update();
        showMinMax();
    }
    
    private mainApp _main;  
    public void setMain(mainApp main)
    {
        this._main = main;
    }
    
    public void addLog(String message){
        log = log + message + '\n';
    }
    
    public void printLog(){
        textArea.setText(log);
    }
    
    public void update(){
        
        int[] data = this._main.cpu.getDistanceInt();
        if(data == null){
//            update();
            return;
        }
        int num_line = this._main.draw.max_num_line;
        this._main.draw.clearCanvas();
        this._main.draw.draw();
        CPU cpu = this._main.cpu;
        for(int i = 0; i < num_line; i++){
            int scale = (int)(5 *(cpu.getMax()[i] - cpu.getMin()[i]));
            this._main.draw.drawEachLine(i, num_line, data[i], scale);
        }
        
        value1.setText("" + data[0]);
        value2.setText("" + data[1]);
        value3.setText("" + data[2]);
        value4.setText("" + data[3]);
        value5.setText("" + data[4]);
        value6.setText("" + data[5]);
        value7.setText("" + data[6]);
        value8.setText("" + data[7]);
//        value1.setText("abc");
//        value2.setText("abc");
//        value3.setText("abc");
//        value4.setText("abc");
//        value5.setText("abc");
//        value6.setText("abc");
//        value7.setText("abc");
//        value8.setText("abc");
//        System.out.println(" ");
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
    
    public void showMinMax(){
        int[] min = this._main.cpu.getMin();
        int[] max = this._main.cpu.getMax();
        min1.setText("" + min[0]);
        min2.setText("" + min[1]);
        min3.setText("" + min[2]);
        min4.setText("" + min[3]);
        min5.setText("" + min[4]);
        min6.setText("" + min[5]);
        min7.setText("" + min[6]);
        min8.setText("" + min[7]);
        
        max1.setText("" + max[0]);
        max2.setText("" + max[1]);
        max3.setText("" + max[2]);
        max4.setText("" + max[3]);
        max5.setText("" + max[4]);
        max6.setText("" + max[5]);
        max7.setText("" + max[6]);
        max8.setText("" + max[7]);
    }   
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textArea.setEditable(false);
//        value1.setMouseTransparent(true);
//        value2.setMouseTransparent(true);
//        value3.setMouseTransparent(true);
//        value4.setMouseTransparent(true);
//        value5.setMouseTransparent(true);
//        value6.setMouseTransparent(true);
//        value7.setMouseTransparent(true);
//        value8.setMouseTransparent(true);
        value1.setEditable(false);
        value2.setEditable(false);
        value3.setEditable(false);
        value4.setEditable(false);
        value5.setEditable(false);
        value6.setEditable(false);
        value7.setEditable(false);
        value8.setEditable(false);
        showMinMax();
    }    
    
}

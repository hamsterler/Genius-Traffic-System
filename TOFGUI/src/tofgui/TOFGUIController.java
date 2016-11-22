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
import javafx.scene.control.Button;
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
    private Label value;
    @FXML
    private Button updateButton;
    
    @FXML
    private synchronized void handleButtonAction(ActionEvent event) {
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
//        mx[0] = new String(max1.getText().trim());
//        mx[1] = new String(max2.getText().trim());
//        mx[2] = new String(max3.getText().trim());
//        mx[3] = new String(max4.getText().trim());
//        mx[4] = new String(max5.getText().trim());
//        mx[5] = new String(max6.getText().trim());
//        mx[6] = new String(max7.getText().trim());
//        mx[7] = new String(max8.getText().trim());
//        
//        mn[0] = new String(min1.getText().trim());
//        mn[1] = new String(min2.getText().trim());
//        mn[2] = new String(min3.getText().trim());
//        mn[3] = new String(min4.getText().trim());
//        mn[4] = new String(min5.getText().trim());
//        mn[5] = new String(min6.getText().trim());
//        mn[6] = new String(min7.getText().trim());
//        mn[7] = new String(min8.getText().trim());
        

        int[] old_min = this._main.cpu.getMin();
        int[] old_max = this._main.cpu.getMax();
        byte[] min = new byte[16];
        byte[] max = new byte[16];
        try{
            for(int i = 0; i < min.length; i++){
                if(i >= 8){
                    min[i] = (byte)old_min[i];
                    max[i] = (byte)old_max[i];
                }
                else{               
                    if(mn[i].isEmpty())
                        min[i] = (byte)old_min[i];
                    else{
                        if(Integer.parseInt(mn[i]) > 255)
                            min[i] = (byte)(255);  
                        else if(Integer.parseInt(mn[i]) < 0)
                            min[i] = (byte)old_min[i];
                        else
                            min[i] = (byte)Integer.parseInt(mn[i]); 
                    }
                    if(mx[i].isEmpty())
                        max[i] = (byte)old_max[i];
                    else{
                        if(Integer.parseInt(mx[i]) > 255)
                            max[i] = (byte)(255);  
                        else if(Integer.parseInt(mx[i]) < 0)
                            max[i] = (byte)old_max[i];
                        else
                            max[i] = (byte)Integer.parseInt(mx[i]);
                    }
                    if(max[i] <= min[i]){
                        max[i] = (byte)old_max[i];
                        min[i] = (byte)old_min[i];
                        addLog("At line " + i + ": Do not set Min greater than Max!!" + '\n');
                    }
                }
            }
        }catch(NumberFormatException e){
            addLog("Please enter numbers only." + '\n');
            showMinMax();
            return;
        }        
//        CPU cpu = _main.cpu;
        _main.cpu.setMinMax(min, max);
        _main.cpu.sendData();  //send min max to the board
        String min_log = "Set Min: ";
        for(int i = 0; i < _main.cpu.Min.length ; i++){
            min_log += (int)_main.cpu.Min[i] + " ";
        }
        min_log += '\n';
        String max_log = "Set Max: ";
        for(int i = 0; i < _main.cpu.Max.length ; i++){
            max_log += (int)_main.cpu.Max[i] + " ";
        }
        max_log += '\n';
        addLog(min_log + max_log);
//        System.out.println("Update");
//        update();
        showMinMax();
        
    }
    
    private mainApp _main;  
    public void setMain(mainApp main)
    {
        this._main = main;
    }
    
    
    public void printLog(){
        textArea.setText(log);
    }
    
    public void addLog(String message){
        textArea.appendText(message);
    }
    public synchronized boolean update(){ 
    
    
        int[] data2 = this._main.cpu.getDistanceInt();
        int[] data = new int[data2.length];
        System.arraycopy(data2, 0, data, 0, data2.length);
        if(data == null){
            return true;
        }
        System.out.println("bla bla");
        int num_line = this._main.draw.max_num_line;
        this._main.draw.clearCanvas();
        this._main.draw.draw();
//        CPU cpu = this._main.cpu;
        for(int i = 0; i < num_line; i++){
            int scale = (int)(5 *(_main.cpu.getMax()[i] - _main.cpu.getMin()[i]));
            this._main.draw.drawEachLine(i, num_line, data[i], scale);
        }
        String[] text = new String[data.length];
        System.out.print("Distance: ");
        for(int i = 0; i<text.length; i++){
            text[i] = Integer.toString(data[i]);
            System.out.print(text[i] + " ");
        }
        System.out.println();
        
        value1.setText("" + data[0]);
        value2.setText("" + data[1]);
        value3.setText("" + data[2]);
        value4.setText("" + data[3]);
        value5.setText("" + data[4]);
        value6.setText("" + data[5]);
        value7.setText("" + data[6]);
        value8.setText("" + data[7]);
    
//        System.out.println("setText");
        return true;
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
        value1.setMouseTransparent(true);
        value2.setMouseTransparent(true);
        value3.setMouseTransparent(true);
        value4.setMouseTransparent(true);
        value5.setMouseTransparent(true);
        value6.setMouseTransparent(true);
        value7.setMouseTransparent(true);
        value8.setMouseTransparent(true);
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

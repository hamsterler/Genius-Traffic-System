package tofgui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class TOFGUIController implements Initializable {
    
    @FXML public StackPane canvasPane;
    @FXML public AnchorPane anchorPane;  
    @FXML private Text detect;
    @FXML private Pane drawPane;    
    
    @FXML private TextField value1;
    @FXML private TextField value2;
    @FXML private TextField value3;
    @FXML private TextField value4;
    @FXML private TextField value5;
    @FXML private TextField value6;
    @FXML private TextField value7;
    @FXML private TextField value8;
    
    @FXML private TextField max1;
    @FXML private TextField max2;
    @FXML private TextField max3;
    @FXML private TextField max4;
    @FXML private TextField max5;
    @FXML private TextField max6;
    @FXML private TextField max7;
    @FXML private TextField max8;
    
    @FXML private TextField min1;
    @FXML private TextField min2;
    @FXML private TextField min3;
    @FXML private TextField min4;
    @FXML private TextField min5;
    @FXML private TextField min6;
    @FXML private TextField min7;
    @FXML private TextField min8;

    @FXML private Text t1;
    @FXML private Text t2;
    @FXML private Text t3;
    @FXML private Text t4;
    @FXML private Text t5;
    @FXML private Text t6;
    @FXML private Text t7;
    @FXML private Text t8;
    
    @FXML private TextField interval;
    @FXML private Text portText;
    @FXML private TextField port;
    @FXML private Button startButton;
    @FXML private Button updateButton;
    @FXML private Pane logo;
    @FXML private CheckBox checkBox;
    @FXML private TextFlow textFlow;
    @FXML private ScrollPane scrollPane;   
    
    public String log = "";
    public int data_size = 16;
    public CPU cpu;
    private Draw _draw;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try{
            textFlow.getChildren().addListener(
                (ListChangeListener<Node>) ((change) -> {
                    textFlow.layout();
                    scrollPane.layout();
                    scrollPane.setVvalue(1.0f);
                }));
            scrollPane.setContent(textFlow);
            
            this.cpu = new CPU();
            drawPane.setStyle("-fx-background-color: #FFFFFF");
            _draw = new Draw(777, 323, 275, data_size);
            drawPane.getChildren().add(_draw.getCanvas());
            
            ImageView image = new ImageView(new Image("file:logo.jpg"));
            logo.getChildren().add(image);  
            
            value1.setEditable(false);
            value2.setEditable(false);
            value3.setEditable(false);
            value4.setEditable(false);
            value5.setEditable(false);
            value6.setEditable(false);
            value7.setEditable(false);
            value8.setEditable(false);                        
            interval.setEditable(false);
            startButton.setOnAction(this::handleStartButton);   
            
        }catch(Exception ex){
            addErrorLog(ex.getMessage() + '\n');
        }
    }  
    
    Task task = new Task<Void>() {
        @Override public Void call() {
            while(true){
                try{             
                    update();
                }catch(Exception ex){
                    System.out.println("Error: " + ex);
                }
                if(isCancelled())
                    break;
            }
            return null;
        }
    };
    
    private void handleStartButton(ActionEvent event){
        try{
            String comPort = port.getText();
            this.cpu.setPort(comPort);
            this.cpu.reconnect();
        //if serial connection is fail
            if(!this.cpu.isSerialConnected()){
                addErrorLog("Error: Wrong COM port. Try again.\n");
                return;
            }
        //if serial connection is correct            
            if(checkBox.isSelected()){
                if(!cpu.readConfig()){
                    addErrorLog("Config file not found.\nGet data from board.\n");
                    cpu.getData();
                }
            }
            else
                cpu.getData(); 
            
            checkBox.setVisible(false);
            updateButton.setOnAction(this::handleUpdateButton);
            cpu.getVersion();
            this.addLog(cpu.getVersionString());  //<<show getVersion data           
            this.data_size = cpu.line_num;  //<<8 or 16
            this.interval.setText("" + this.cpu.interval);
            showMinMax();
            this.cpu.sendData();    //set min max that read from config.json to board

            port.setDisable(true);
            startButton.setDisable(true);
            startButton.setVisible(false);
            portText.setFill(Color.valueOf("#ABB2B9"));

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        //--------------------------------        
        }catch(Exception ex){
            addErrorLog("Error | startButton: " + ex.getMessage());
        }   
    }
       
    private void handleUpdateButton(ActionEvent event) {
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
        
        byte[] min = new byte[data_size * 2];
        byte[] max = new byte[data_size * 2];
        try{
//            this.cpu.interval = Integer.parseInt(interval.getText().trim());
            //foe 16 line sensors
            if(data_size == 16){
                for(int i = 0; i < 8;i++){
                    if(mn[i].isEmpty()){
                        min[4*i] = (byte)(cpu.getMin()[2*i] >> 8);
                        min[4*i + 1] = (byte)cpu.getMin()[2*i];
                        min[4*i + 2] = (byte)(cpu.getMin()[2*i + 1] >> 8);
                        min[4*i + 3] = (byte)cpu.getMin()[2*i + 1];
                    }
                    else{
                        min[4*i] = (byte)(Integer.parseInt(mn[i]) >> 8);
                        min[4*i + 1] = (byte)Integer.parseInt(mn[i]);
                        min[4*i + 2] = (byte)(Integer.parseInt(mn[i]) >> 8);
                        min[4*i + 3] = (byte)Integer.parseInt(mn[i]);             
                    }
                    if(mx[i].isEmpty()){
                        max[4*i] = (byte)(cpu.getMax()[2*i] >> 8);
                        max[4*i + 1] = (byte)cpu.getMax()[2*i];
                        max[4*i + 2] = (byte)(cpu.getMax()[2*i + 1] >> 8);
                        max[4*i + 3] = (byte)cpu.getMax()[2*i + 1];
                    }
                    else{
                        max[4*i] = (byte)(Integer.parseInt(mx[i]) >> 8);
                        max[4*i + 1] = (byte)Integer.parseInt(mx[i]);
                        max[4*i + 2] = (byte)(Integer.parseInt(mx[i]) >> 8);
                        max[4*i + 3] = (byte)Integer.parseInt(mx[i]);
                    }
                    int minn = (int)((min[4*i] & 0xff) << 8) + (int)(min[4*i + 1] & 0xff);
                    int maxx = (int)((max[4*i] & 0xff) << 8) + (int)(max[4*i + 1] & 0xff);
                    if( minn > maxx){
                        min[4*i] = (byte)(cpu.getMin()[2*i] >> 8);
                        min[4*i + 1] = (byte)cpu.getMin()[2*i];
                        max[4*i] = (byte)(cpu.getMax()[2*i] >> 8);
                        max[4*i + 1] = (byte)cpu.getMax()[2*i];
                    }
                    minn = (int)((min[4*i + 2] & 0xff) << 8) + (int)(min[4*i + 3] & 0xff);
                    maxx = (int)((max[4*i + 2] & 0xff) << 8) + (int)(max[4*i + 3] & 0xff);
                    if(minn > maxx){
                        min[4*i + 2] = (byte)(cpu.getMin()[2*i + 1] >> 8);
                        min[4*i + 3] = (byte)cpu.getMin()[2*i + 1];
                        max[4*i + 2] = (byte)(cpu.getMax()[2*i + 1] >> 8);
                        max[4*i + 3] = (byte)cpu.getMax()[2*i + 1];
                    }
                        
                }
            //for 8 line sensors
            }else if(data_size == 8){
                for(int i = 0; i < 8;i++){
                    if(mn[i].isEmpty()){
                        min[2*i] = (byte)(cpu.getMin()[i] >> 8);
                        min[2*i + 1] = (byte)(cpu.getMin()[i]);
                    }
                    else{
                        min[2*i] = (byte)(Integer.parseInt(mn[i]) >> 8);    
                        min[2*i + 1] = (byte)Integer.parseInt(mn[i]);
                    }
                    
                    if(mx[i].isEmpty()){
                        max[2*i] = (byte)(cpu.getMax()[i] >> 8);
                        max[2*i + 1] = (byte)(cpu.getMax()[i]);
                    }
                    else{
                        max[2*i] = (byte)(Integer.parseInt(mx[i]) >> 8);    
                        max[2*i + 1] = (byte)Integer.parseInt(mx[i]);
                    }
                    int minn = (int)((min[2*i] << 8) & 0xff) + (int)(min[2*i + 1]);
                    int maxx = (int)((max[2*i] << 8) & 0xff) + (int)(max[2*i + 1]);
                    if(minn > maxx){
                        min[2*i] = (byte)(cpu.getMin()[i] >> 8);
                        min[2*i + 1] = (byte)(cpu.getMin()[i]);
                        max[2*i] = (byte)(cpu.getMax()[i] >> 8);
                        max[2*i + 1] = (byte)(cpu.getMax()[i]);
                    }
                }
            }
        }catch(NumberFormatException e){
           addErrorLog("Please enter numbers only." + '\n');
           showMinMax();
           return;
        }   
        try{
            //--set min max and send data to board--
            this.cpu.setMinMax(min, max);
            if(!this.cpu.sendData()){
                addErrorLog("Updating Fail!!\n");
                return;
            }
            addLog("Updating Success!!\n");
            //--------------------------------------  
            showMinMax();
        }catch(Exception ex){
            addErrorLog("Something Wrong with sendData()\n");
            return;
        }
    }
   
    public void addErrorLog(String message){  
        Text t = new Text(message);
        t.setFill(Color.valueOf("#E74C3C"));
        textFlow.getChildren().add(t);
    }
    
    public void addLog(String message){
        textFlow.getChildren().add(new Text(message));
    }

    public synchronized void update() {
        //recieve data 
        int[] buffer;
        int[] data;
        int isDetect;
        try{
            buffer = this.cpu.getDistanceInt();        
            data = new int[data_size];
            System.arraycopy(buffer, 0, data, 0,data.length);
            isDetect = buffer[data_size];       
            int num_line = this._draw.line_num;
            this._draw.clearCanvas();
            this._draw.draw();      //<< draw default line (a green one)
            this._draw.drawMinMaxLine(this.cpu.getMin(), this.cpu.getMax(), Color.valueOf("#3498DB"), 4);  //<< (blue line)
            this._draw.drawDistancePoint(data, this.cpu.getMax(), Color.valueOf("#E74C3C"));    //<<draw red dot        
            if(isDetect == 1){ //found
                detect.setText("Detected!");
                detect.setFill(Color.valueOf("#E74C3C"));
            }else{
                detect.setText("No Object Found");
                detect.setFill(Color.valueOf("#5D6D7E"));
            }     
            //update distance in the monitor
            if(data_size == 8){
                t1.setText(Integer.toString(data[0]));
                t2.setText(Integer.toString(data[1]));
                t3.setText(Integer.toString(data[2]));
                t4.setText(Integer.toString(data[3]));
                t5.setText(Integer.toString(data[4]));
                t6.setText(Integer.toString(data[5]));
                t7.setText(Integer.toString(data[6]));
                t8.setText(Integer.toString(data[7]));
            }
            else if(data_size == 16){
                t1.setText(Integer.toString(data[0]) + "  |  " + data[1]);
                t2.setText(Integer.toString(data[2]) + "  |  " + data[3]);
                t3.setText(Integer.toString(data[4]) + "  |  " + data[5]);
                t4.setText(Integer.toString(data[6]) + "  |  " + data[7]);
                t5.setText(Integer.toString(data[8]) + "  |  " + data[9]);
                t6.setText(Integer.toString(data[10]) + "  |  " + data[11]);
                t7.setText(Integer.toString(data[12]) + "  |  " + data[13]);
                t8.setText(Integer.toString(data[14]) + "  |  " + data[15]);
            }
        }
        catch(NullPointerException e){  
            System.out.println("Error | TOFGUIController | update(): " +  e.getMessage());  
            System.out.println(this.cpu.getError());
            System.out.println(this._draw.getError());
        }
    }
    
    public void showMinMax(){  //ver2
        int[] min = this.cpu.getMin();
        int[] max = this.cpu.getMax();
        try{
            if(data_size == 16){
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
            }
            else if(data_size == 8){
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
        }catch(NullPointerException ex){
            System.out.println("Error | TOFGUIController | showMinMax(): " +  ex.getMessage());
        }
    }    
    
}

package tofgui;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
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
    @FXML private Pane logo;
    @FXML private CheckBox checkBox;
    @FXML private TextFlow textFlow;
    @FXML private ScrollPane scrollPane;   
    
    @FXML private Button startButton;
    @FXML private Button updateButton;
    @FXML private Button detectedButton;
    
    @FXML private TextField start_delay;
    @FXML private TextField end_delay;
    @FXML private Text leddarStatus;
    
    @FXML private Pane leddarStatusPane;
    @FXML private Text watchdogText;
    @FXML private Text uptimeText;
    
    @FXML private CheckBox showAutoMinMax;
    @FXML private CheckBox showAutoLine;
    
    @FXML private Button setRefDistanceButton;
    @FXML private CheckBox autoAssignCheckBox;    
    @FXML Pane controlPane;
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
            
            ImageView image = new ImageView(new Image("file:logo.jpg"));
            image.setFitHeight(100);
            image.setFitWidth(180);
            image.setPreserveRatio(true);
            logo.getChildren().add(image);  
            
            leddarStatusPane.setVisible(false);
            
            value1.setEditable(false);
            value2.setEditable(false);
            value3.setEditable(false);
            value4.setEditable(false);
            value5.setEditable(false);
            value6.setEditable(false);
            value7.setEditable(false);
            value8.setEditable(false);                        
//            interval.setEditable(false);
            startButton.setOnAction(this::handleStartButton);  
            String path = "COMPORT.txt";
            List<String> lines = Files.readAllLines(Paths.get(path),
                    Charset.defaultCharset());
            String comport = lines.get(0).toString();
            port.setText(comport);
//            controlPane.setVisible(false);
        }catch(Exception ex){
            addErrorLog(ex.getMessage() + '\n');
            ex.printStackTrace();
        }
    }  
    //------------------------Set Ref. Length Button------------------------
    private void handleSetRefDistanceButton(ActionEvent event){
        try{
            cpu.setRefDistance();
            cpu.resetAutoMinMax();
            int[] max = new int[8];
            for (int i = 0; i < 8; i++) {
                max[i] = cpu.reference_distance[i] - 20;
            }
            for (int i = 0; i < 5; i++) {
                if(cpu.setConfig(cpu.getMin(), max, cpu.getInSense(), cpu.getOutSense())){
                    addLog("SetConfig Success!!\n");
                    break;
                }
            }
//            cpu.setConfig(cpu.getMin(), max, cpu.getInSense(), cpu.getOutSense());
            cpu.getConfig();
            showConfig();
            addLog("RefDistance: ");
            for (int i = 0; i < 8; i++) {
                addLog(cpu.reference_distance[7 - i] + "    ");
            }
            addLog("\r\n");
        }catch(Exception ex){
            ex.printStackTrace();
            addErrorLog("Error | setRefDistanceButton: " + ex.getMessage());
        }   
    }
    //----------------------------Detect Button----------------------------
    private void handleDetectedButton(ActionEvent event){
        try{
            addLog("Detect!!\n");
            cpu.auto_assign_minmax(cpu.getDistance());
            cpu.drawAutoLine();
            showConfig();
        }catch(Exception ex){
            ex.printStackTrace();
            addErrorLog("Error | detectedButton: " + ex.getMessage());
        }   
    }
    
    //----------------------------Start Button----------------------------
    private void handleStartButton(ActionEvent event){
        try{
//            controlPane.setVisible(true);
            
            String comPort = port.getText();
            this.cpu.setPort(comPort);
            this.cpu.reconnect();
        //if serial connection is fail
            if(!this.cpu.isSerialConnected()){
                addErrorLog("Error: Wrong COM port. Try again.\n");
                return;
            }
        //if serial connection is correct  
            for (int i = 0; i < 5; i++) {
                if(cpu.getVersion())
                    break;
            }
//            cpu.getVersion();
            this.addLog("Version: " + cpu.getVersionString() + '\n');  //<<show getVersion data 
            this.data_size = cpu.line_num;  //<<8 or 16
            cpu.max_distance = new int[data_size];
            cpu.min_distance = new int[data_size];
            _draw = new Draw(777, 323, 275, data_size);
            drawPane.getChildren().add(_draw.getCanvas());
            
            if(checkBox.isSelected()){
                if(!cpu.readConfig()){
                    addErrorLog("Config file not found.\nGet data from board.\n");                   
                }else
                    this.cpu.setConfig(cpu.read_min, cpu.read_max, cpu.read_in_sense, cpu.read_out_sense);    //set min max that read from config.json to board
            }
            else{
                
            }   
            for (int i = 0; i < 5; i++) {
                if(cpu.getConfig())
                    break;
            }
            showConfig();
            
            checkBox.setVisible(false);
            
            //set button action
            updateButton.setOnAction(this::handleUpdateButton);
            detectedButton.setOnAction(this::handleDetectedButton);
            setRefDistanceButton.setOnAction(this::handleSetRefDistanceButton);
            
            leddarStatusPane.setVisible(true);

            port.setDisable(true);
            startButton.setDisable(true);
            startButton.setVisible(false);
            portText.setFill(Color.valueOf("#ABB2B9"));
            

            Thread t = new Thread(new Runnable(){
                @Override
                public void run(){
                    int get_system_time = 2000; //every 10 mins
                    int count = 0;
                    while(true){
                        try {
                            if(count >= get_system_time)
                                count = 0;
                            
                            if(count == 0){
                                for (int i = 0; i < 5; i++) {
                                    if(cpu.getSystemStatus())
                                        break;
                                }
 
//                                cpu.getSystemStatus();
                                Thread.sleep(1);
                            }        
                            cpu.getDetection();
                            count++;
                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                            Logger.getLogger(TOFGUIController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            t.start();
            
            //----start update thread----
            AnimationTimer update_thread = new AnimationTimer() 
            {
                @Override
                public void handle(long now) 
                {
                    update_animation();
                }
            };
            update_thread.start();
            
        //--------------------------------        
        }catch(Exception ex){
            ex.printStackTrace();
            addErrorLog("Error | startButton: " + ex.getMessage());
        }   
    }
    
    //----------------------------Update Button----------------------------
    private void handleUpdateButton(ActionEvent event) {
//        addLog("Please Wait. -> ");
        System.out.println("Please Wait.");
        
        int[] min = new int[8];
        int[] max = new int[8];  //<=999
        
        //use min&max from textBox
        String[] mx = new String[8];
        String[] mn = new String[8];
        mx[7] = max1.getText().trim();
        mx[6] = max2.getText().trim();
        mx[5] = max3.getText().trim();
        mx[4] = max4.getText().trim();
        mx[3] = max5.getText().trim();
        mx[2] = max6.getText().trim();
        mx[1] = max7.getText().trim();
        mx[0] = max8.getText().trim();

        mn[7] = min1.getText().trim();
        mn[6] = min2.getText().trim();
        mn[5] = min3.getText().trim();
        mn[4] = min4.getText().trim();
        mn[3] = min5.getText().trim();
        mn[2] = min6.getText().trim();
        mn[1] = min7.getText().trim();
        mn[0] = min8.getText().trim(); 
        int in_sensitivity = 0;
        int out_sensitivity = 0;
        try{
            //in
            if(start_delay.getText().isEmpty())
                in_sensitivity = cpu.getInSense();
            else
                in_sensitivity = (int)(Double.parseDouble(start_delay.getText()) * 1000);
            //out
            if(end_delay.getText().isEmpty())
                in_sensitivity = cpu.getOutSense();
            else
                out_sensitivity = (int)(Double.parseDouble(end_delay.getText()) * 1000);
            System.out.println("In = " + in_sensitivity);
            System.out.println("Out = " + out_sensitivity);
            
            //min max
            for(int i = 0; i < 8;i++){
             
                if(mn[i].isEmpty()){
                    min[i] = cpu.getMin()[i];
                }
                else{
                    min[i] = Integer.parseInt(mn[i]);    
                }

                if(mx[i].isEmpty()){
                    max[i] = cpu.getMax()[i];
                }
                else{
                    max[i] = Integer.parseInt(mx[i]);  
                }
                if(min[i] > max[i]){
                    min[i] = cpu.getMin()[i];
                    max[i] = cpu.getMax()[i];
                }
                
                //if > 1000 
                if(min[i] > 1000 || max[i] > 1000){
                    showConfig();
                    addErrorLog("Please Enter Min/Max only 1 - 1000.\n");
                    return;
                }
            } 
        }catch(NumberFormatException e){
           addErrorLog("Please enter numbers only." + '\n');
           showConfig();
           return;
        }   
        
        if(autoAssignCheckBox.isSelected()){  
            System.out.println("auto_min      auto_max");
            for (int i = 0; i < 8; i++) {
                max[i] = cpu.auto_line_max[i];
                min[i] = cpu.auto_line_min[i];
                System.out.println(cpu.auto_max[i] + "      " + cpu.auto_min[i] );
            }
//            cpu.resetAutoMinMax();
        }
        
        try{
//            --set min max and send data to board--
//            this.cpu.setMinMax(min, max);
//            showMinMax();

//            addErrorLog("Please Wait.\n");
            if(!this.cpu.setConfig(min, max, in_sensitivity, out_sensitivity)){
//                addErrorLog("Updating Fail!!\n");
//                addErrorLog("Please Wait.\n");
                while(!cpu.getConfig()){
                    System.out.print("");
                }
                showConfig();   
                addLog("Updating Success!!\n");
//                return;
            }else{
                cpu.getConfig();
                showConfig(); 
                addLog("Updating Success!!\n");
            }
            //--------------------------------------  
//            showMinMax();
//            while(!cpu.getConfig()){
//                System.out.print("");
//            }
//            showMinMax();
            
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
//    public int[] buffer;
    public int[] max_distance;
    public int[] min_distance;
    public void update_animation() {
        //recieve data 
        int[] buffer;
        int[] data;
        boolean isDetect;
        try{      
            //draw 
            int[] distance = this.cpu.getDistance();
            this._draw.clearCanvas();
            this._draw.resize((int)this.drawPane.getWidth(), (int)this.drawPane.getHeight());
            this._draw.updateMaxMax(this.cpu.getMax());
            this._draw.draw_default(cpu.getLeddarConnect());      //<< draw default line (a green one)
            this._draw.drawMinMaxLine(this.cpu.getMin(), this.cpu.getMax(), cpu.getLineDetected(), cpu.getLeddarConnect());  //<< (blue line)    
            if(showAutoLine.isSelected())
                this._draw.drawAutoLine(this.cpu.auto_line_min, this.cpu.auto_line_max);
            
            if(cpu.getLeddarConnect()){
                if(showAutoMinMax.isSelected())
                    this._draw.drawAutoMinMax(this.cpu.auto_min, this.cpu.auto_max);
                this._draw.drawDistancePoint(distance, Color.valueOf("#E74C3C"));    //<<draw red dot        
            }
            //detect status
            if(cpu.isDetected()){ //found
                detect.setText("Detected!");
                detect.setFill(Color.valueOf("#E74C3C"));
            }else{
                detect.setText("No Object Found");
                detect.setFill(Color.valueOf("#5D6D7E"));
            }    
            
            //leddar status
            if(cpu.getLeddarConnect()){
                leddarStatus.setText("Connect");
                leddarStatus.setFill(Color.valueOf("#5EF887"));
            }else{
                leddarStatus.setText("Disconnect");
                leddarStatus.setFill(Color.valueOf("#E74C3C")); 
            }
            this.watchdogText.setText(cpu.getWatchDog() + "");
            
            long uptime = cpu.getUpTime();
            int hours = (int)(uptime/3600);
            int mins = (int)((uptime % 3600)/60);
            int sec = (int)(uptime % 60);
            String uptime_text = "" + hours + " : " + mins + " : " + sec + " s";
            this.uptimeText.setText(uptime_text);
//            this.uptimeText.setText(String.valueOf(cpu.getUpTime()));
            
            //update distance on monitor
            t1.setText(Integer.toString(distance[7]));
            t2.setText(Integer.toString(distance[6]));
            t3.setText(Integer.toString(distance[5]));
            t4.setText(Integer.toString(distance[4]));
            t5.setText(Integer.toString(distance[3]));
            t6.setText(Integer.toString(distance[2]));
            t7.setText(Integer.toString(distance[1]));
            t8.setText(Integer.toString(distance[0]));
        }
        catch(NullPointerException e){  
            e.printStackTrace();
//            System.out.println("Error | TOFGUIController | update(): " +  e.getMessage());  
            System.out.println(this.cpu.getError());
            System.out.println(this._draw.getError());
        }
    }
    
    public void showConfig(){  //ver2
        int[] min = this.cpu.getMin();
        int[] max = this.cpu.getMax();
        try{
                min1.setText("" + min[7]);
                min2.setText("" + min[6]);
                min3.setText("" + min[5]);
                min4.setText("" + min[4]);
                min5.setText("" + min[3]);
                min6.setText("" + min[2]);
                min7.setText("" + min[1]);
                min8.setText("" + min[0]);

                max1.setText("" + max[7]);
                max2.setText("" + max[6]);
                max3.setText("" + max[5]);
                max4.setText("" + max[4]);
                max5.setText("" + max[3]);
                max6.setText("" + max[2]);
                max7.setText("" + max[1]);
                max8.setText("" + max[0]);
                
                double in_s = (double)cpu.getInSense()/(double)1000;
                double out_s = (double)cpu.getOutSense()/(double)1000;
                DecimalFormat df = new DecimalFormat("#.#");      
                start_delay.setText(Double.valueOf(df.format(in_s)) + "");
                end_delay.setText(Double.valueOf(df.format(out_s)) + "");
//                start_delay.setText(cpu.getInSense() + "");
//                end_delay.setText(cpu.getOutSense() + "");
        }catch(NullPointerException ex){
            System.out.println("Error | TOFGUIController | showMinMax(): " +  ex.getMessage());
        }
    }    
    
    
}

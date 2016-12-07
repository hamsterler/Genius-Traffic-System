package tofgui_v2;

import java.io.UnsupportedEncodingException;

public class CPU {
    private Serial _serial1; //for Sensor
    private WebCam webcam;
//    private Serial2 _serial2; //for camera
//    private Serial _serial3;
    private String _error = "";
    private int _min[] = new int[16];
    private int _max[] = new int[16];
    private int _lane[][];
    private int _distance[] = new int[16];
    private int _interval;
    private boolean _detected[];
    private boolean _lane_detected[];
    
    public CPU(String port1, String port2){
        //---webcam---
        
        //------------
        _serial1 = new Serial(port1);
        _serial1.start();
        this._interval = 500;
        this.clearDetected();
        this.clearLaneDetected();               
        
    }
    
    public boolean clearDetected(){
        for(int i = 0; i < this._detected.length; i++)
            this._detected[i] = false;
        return true;
    }
    public boolean clearLaneDetected(){
        for(int i = 0; i < this._lane_detected.length; i++)
            this._lane_detected[i] = false;
        return true;
    }
    
    //done (not sure)
    public boolean setMin(int[] min){
        try{
            if(min.length == this._min.length)
                this._min = min;
            else{
                this._error = "Error - setMin(): min.length != 16";
                return false;
            }
        }catch(Exception ex){
            this._error = "Error - setMin(): " + ex.getMessage();
            return false;
        }
        return true;
    }
    
    //done (not sure)
    public boolean setMax(int[] max){
        try{
            if(max.length == this._max.length)
                this._max = max;
            else{
                this._error = "Error - setMin(): min.length != 16";
                return false;
            }
        }catch(Exception ex){
            this._error = "Error - setMin(): " + ex.getMessage();
            return false;
        }
        return true;
    }
 
    //done (not sure)
    public boolean writeDistance(int[] distance){
        try{
            this._distance = distance;
            for(int i = 0; i < this._distance.length; i++){
                if(this._distance[i] >= this._min[i] && this._distance[i] <= this._max[i])
                    this._detected[i] = true;
                else
                    this._detected[i] = false;
            }
            
            //-------------check for lane detection-------------
            this.clearLaneDetected();
            for(int i = 0; i < this._lane.length; i++){
                for(int j = 0; j < this._lane[i].length; j++){
                    int line = this._lane[i][j];
                    if(this._detected[line])
                        this._lane_detected[i] = true;
                }
            }
            //--------------------------------------------------
            
        }catch(Exception ex){
            this._error = "Error - writeDistance(): " + ex.getMessage(); 
            return false;
        }
        return true;
    }   
    
    public void readConfig(){
        alisa.json.Parser parser = new alisa.json.Parser();
        alisa.json.Object root = parser.load("config.json");
    }
    
    //done (not sure)
    public boolean loadConfig()
    {
        try{
            alisa.json.Parser parser = new alisa.json.Parser();
            alisa.json.Object root = parser.load("config.json");
            if (root == null) { 
                this._error = "Error on reading config.json file."; 
                return false; 
            }
            //get min
            alisa.json.Data mins = root.findData("min");
            int min_length = mins.getArray().countObjects();
            this._min = new int[min_length];
            for (int i = 0; i < this._min.length ; i++){
                alisa.json.Object obj = mins.getArray().getObject(i);
                this._min[i] = obj.getData(0).getInteger();
            }

            //get max
            alisa.json.Data maxs = root.findData("max");
            int max_length = maxs.getArray().countObjects();
            this._max = new int[min_length];
            for (int i = 0; i < this._max.length ; i++){
                alisa.json.Object obj = maxs.getArray().getObject(i);
                this._min[i] = obj.getData(0).getInteger();
            }

            //get lane
            alisa.json.Data lanes = root.findData("lane");
            int lane_length = lanes.getArray().countObjects();
            this._lane = new int[lane_length][];
            for (int i = 0; i < this._lane.length ; i++){
                alisa.json.Object obj = lanes.getArray().getObject(i);
                int lane_line_length = obj.countData();
                this._lane[i] = new int[lane_line_length];
                for(int j = 0; j < lane_line_length; j++){
                    this._lane[i][j] = obj.getData(j).getInteger();
                }
            }
        }catch(Exception ex){
            this._error = "Error - readConfig(): " + ex.getMessage();
            return false;
        }
        
        return true;
    }
    public void writeConfig(){
    
    }
}

 
package tofgui_v2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.concurrent.Task;
import tofgui_v2.Draw.*;
import tofgui_v2.Model.*;

import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class CPU {
    //------------Model------------
    private int[] _distance;
    private Lines _lines;
    private Lane[] _lane;
    private ArrayList<GroupDetect> _group_detect;
    public int[] min = new int[16];
    public int[] max = new int[16];
    private int _car_num = 0;
    private int _total_car = 0;
    private int _row = 0;
    private String _error;
    private String _status = "Running";
    private int _interval = 100;
    //-----------------------------
    
    //------------Draw------------
    private int[][] _detectedLog = new int[100][16];
    private DrawLine _draw;
    private DrawDetectedGraph _drawDetect;
    private DrawSquare _draw_square;
    //----------------------------
    

    private Serial _serial;
    private FXMLDocumentController _controller;
    
    private boolean _pause = false;
    
    public boolean pauseStatus(){ return this._pause;}
    public void pause(){ 
        this._pause = true;
        this._status = "Pause";
    }
    public void unpause(){ 
        this._pause = false;
        this._status = "Running";
    }
    
    public CPU(FXMLDocumentController controller){
        this._controller = controller;
        _group_detect = new ArrayList<GroupDetect>();
        for(int i = 0; i < this._detectedLog.length; i++){
            for(int j = 0; j < this._detectedLog[i].length; j++){
                this._detectedLog[i][j] = 0;
            }
        }
        this._lines = new Lines();
        
        //---------------------DrawLine Class---------------------
        _draw = new DrawLine(controller.canvasLine, 200, 16);
        _drawDetect = new DrawDetectedGraph(controller.canvasDetect, 16);
        _draw_square = new DrawSquare(controller.canvasSquare, 16);
        
        this._serial = new Serial("COM8");  

        this.run(); 
    }
    
    public void run(){
        //-----------------Serial Read Thread-----------------
//        this._status = "Running";
//        if(!_serial.connect())
//            this._status = "Serail Connection Fail";
        _serial.connect();
        Thread serialThread = new Thread(new Runnable(){
            public void run(){
                while(true){
                    _status = _serial.getError();
                    _serial.run();
                }
            }
        });
        serialThread.start();
        //----------------------------------------------------
      
        //----Read Config----
//        readInputConfig();
//        readOutputConfig();
        readConfig();   
        //---------------Create Thread for calculating a distance value--------------
        Thread computeThread = new Thread(new Runnable(){
            public void run(){
                while(true){
                    _distance = _serial.getDistance();
                    startCompute();
                }
            }
        });
        computeThread.start();
        //----------------------------------------------------------------------------
        
        //-----------Create drawCanvas Thrad (Animation Timer)-------------
        AnimationTimer drawCanvasThread = new AnimationTimer() 
        {
            @Override
            public void handle(long now2){  
                try{
                    showDistance(_lines);
                    //draw square line and lane
                    _draw_square.clearCanvas();
                    _draw_square.drawLine(_lines); 
                    _draw_square.drawLane(_lines, _lane);
                    
                    //draw detected graph
                    _drawDetect.draw(_detectedLog, _lines.line);

                    //----draw line----
                    _draw.clearCanvas();
                    _draw.drawDefaultLine(_lines.line);      //<< draw default line (a green one)
                    _draw.drawMinMaxLine(_lines.line, Color.valueOf("#3498DB"), 4);  //<< (blue line)
                    _draw.drawDistancePoint(_lines.line, Color.valueOf("#E74C3C"));    //<<draw red dot
                    
                    //update text
                    for (int i = 0; i < _lane.length; i++) {
                        _controller.lane[i].setText(_lane[i].getCarcount() + "");
                        _controller.total.setText(_total_car + "");
                    }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        drawCanvasThread.start();
        //------------------------------------------------------------------
       
    }
    //-------------------------------Compute-------------------------------
    public AutoLaneGenerate gd = new AutoLaneGenerate();
    private void startCompute(){
        if(this._status == "")
            this._status = "Running";
        this._controller.status.setText(this._status);
        try{
            //if pause >> return 
            if(this._pause)
                return;
            
            for(int i = 0; i < 16; i++){
                this._lines.line[i].distance = this._distance[i]; 
                
            //-------------find max_distance in each line---------------
                if(this._row == 0)
                    this._lines.line[i].max_distance = this._lines.line[i].distance;
                else{
                    if(this._lines.line[i].max_distance < this._lines.line[i].distance)
                        this._lines.line[i].max_distance = this._lines.line[i].distance;
                }
            //----------------------------------------------------------
                
            //------------------check for detection--------------------
                //Case1: if first detected (change from 0 -> 1)
                if(this._lines.line[i].detect() && !this._lines.line[i].detected){
                    this._lines.line[i].detected = true;
                    this._lines.line[i].firstDetectRow = this._row;
                    this._lines.line[i].lastDetectRow = -1;
                    boolean count = true;

                    for(int j = 0; j < this._group_detect.size(); j++){
                        if(this._group_detect.get(j).status == 1){  //if this _car_found does not have end row yet
                            if(this._row - this._group_detect.get(j).first_row <= 3 && this._group_detect.get(j).first_row != -1){
                                count = false;
                                //add line in this group
                                if(!this._group_detect.get(j).addLine(i))
                                    count = true;
                            }
                        }
                    }
                    // if this line is not a member of any car_found then create a new one
                    if(count){
                        this._group_detect.add(new GroupDetect(this._row));
                        this._group_detect.get(this._group_detect.size() - 1).addLine(i);
                    }    

                                     
                //Case2: if this line change from detected to not detected (1 -> 0)
                }else if(!this._lines.line[i].detect() && this._lines.line[i].detected){
                    this._lines.line[i].detected = false;
                    this._lines.line[i].lastDetectRow = this._row;
                    for(int j = 0; j < this._group_detect.size(); j++ ){

                        if(this._group_detect.get(j).status == 1){

//                            if(this._lines.line[i].firstDetectRow - this._group_detect.get(j).first_row <= 3){
                            if(this._group_detect.get(j).isMember(i)){  // <<< if this line[i] is a member of this group_detect
                                if(this._group_detect.get(j).end_row == -1)
                                    this._group_detect.get(j).end_row = this._lines.line[i].lastDetectRow;
                                this._group_detect.get(j).line_num_check++;

                                if(this._group_detect.get(j).get_line_num()== this._group_detect.get(j).line_num_check)
                                    this._group_detect.get(j).status = 0;  //count process
                            }
                        }
                    }
                }
            //-----------------------------------------------------------
            }  
            addDetectLog();

            //------------------------car count process------------------------
//            for (GroupDetect this_group : this._group_detect ) {
//                
//                //in case that this car_found line member does not already end at all(change from detect to non detect every line member) but the last  
//                if(this_group.status == 1 && this_group.end_row != -1){
//                    if(this._row - this_group.end_row > 3) {
//                        this_group.status = 0;
//                    }
//                }
//                if(this_group.status == 0){
//                                            
////                      |..II..II....|    <-- in this case some line are still not have thier end_row yet      
////                      |..II..II....|        so, I have to create new group_detect for the line that already have an end_row 
////                      |IIIIIIIIIIII|            and delete that line from the old group
////                      |IIIIIIIIIIII| 
////                      |............|
//                    if(this_group.line_num_check != this_group.get_line_num()){   
//                        this_group.sortLine();
//                        Integer[] line = this_group.getLine();
////                        int pre = 0;
//                        int start = this_group.getLine()[0];
//
//                        for(int j = 0; j < line.length; j++){
//                            if((j < line.length - 1 && this._lines.line[line[j + 1]].lastDetectRow == -1 && this._lines.line[line[j]].lastDetectRow != -1) 
//                                || (this._lines.line[line[j]].lastDetectRow != -1 && j == line.length - 1) 
//                                || (j < line.length - 1 && line[j + 1] - line[j] > 1)){
//                                this._group_detect.add(new GroupDetect(this_group.first_row, this_group.end_row, 0));
//                                for(int k = start; k <= j;k++){
//                                    this._group_detect.get(this._group_detect.size() - 1).addLine(line[k]);
//                                    this._group_detect.get(this._group_detect.size() - 1).line_num_check++;
//                                }
//                                for(int k = start; k <= j;k++){
//                                    this_group.removeLine(line[k]);
//                                    this_group.line_num_check--;
//                                }
//                                start = j + 1;
//                            }else if(j < line.length - 1 && this._lines.line[line[j + 1]].lastDetectRow != -1 && this._lines.line[line[j]].lastDetectRow == -1){
//                                start = j + 1;
//                            }
//                        }
//                        
//                            
//                        this_group.line_num_check = 0;
//                        this_group.end_row = -1;
//                        this_group.status = 1;
//    //                    System.out.println("length = " + this._group_detect.get(i).getLine().length);
//    //                    System.out.println("line_num_check = " + this._group_detect.get(i).line_num_check + "\nline_num = " + this._group_detect.get(i).line_num);
//
//                    }
//                    //in case  -> ........
//                    //            .||.|.|.
//                    //            .||||||.
//                    //            .||||||.
//                    else{
//                        // do nothing
//                    }
//                }
//                if(this_group.get_line_num() == 0)
//                    this._group_detect.remove(this_group);
//                
//            }
            //------------------------car count process------------------------
            for(int i = 0; i < this._group_detect.size(); i++ ){
                //in case that this car_found line member does not already end at all(change from detect to non detect every line member) but the last  
                if(this._group_detect.get(i).status == 1 && this._group_detect.get(i).end_row != -1){
                    if(this._row - this._group_detect.get(i).end_row > 3) {
                        this._group_detect.get(i).status = 0;
                    }
                }

                if(this._group_detect.get(i).status == 0){             
//                  |..II..II....|    <-- in this case some line are still not have thier end_row yet      
//                  |..II..II....|        so, I have to create new group_detect for the line that already have an end_row 
//                  |IIIIIIIIIIII|        and delete that line from the old group
//                  |IIIIIIIIIIII| 
//                  |............|
                    if(this._group_detect.get(i).line_num_check != this._group_detect.get(i).get_line_num()){   
                        this._group_detect.get(i).sortLine();
                        Integer[] line = this._group_detect.get(i).getLine();
//                        int pre = 0;
                        int start = this._group_detect.get(i).getLine()[0];

                        for(int j = 0; j < line.length; j++){
                            if((j < line.length - 1 && this._lines.line[line[j + 1]].lastDetectRow == -1 && this._lines.line[line[j]].lastDetectRow != -1) 
                                || (this._lines.line[line[j]].lastDetectRow != -1 && j == line.length - 1) 
                                || (j < line.length - 1 && line[j + 1] - line[j] > 1)){
                                this._group_detect.add(new GroupDetect(this._group_detect.get(i).first_row, this._group_detect.get(i).end_row, 0));
                                for(int k = start; k <= j;k++){
                                    this._group_detect.get(this._group_detect.size() - 1).addLine(line[k]);
                                    this._group_detect.get(this._group_detect.size() - 1).line_num_check++;
                                }
                                for(int k = start; k <= j;k++){
                                    this._group_detect.get(i).removeLine(line[k]);
                                    this._group_detect.get(i).line_num_check--;
                                }
                                start = j + 1;
                            }else if(j < line.length - 1 && this._lines.line[line[j + 1]].lastDetectRow != -1 && this._lines.line[line[j]].lastDetectRow == -1){
                                start = j + 1;
                            }
                        }
                         
                        this._group_detect.get(i).line_num_check = 0;
                        this._group_detect.get(i).end_row = -1;
                        this._group_detect.get(i).status = 1;

                    }
//                  |............|    <-- in case(all lines already end)
//                  |..I.I.III...|         
//                  |..IIIIIII...|        
//                  |..IIIIIII...| 
//                  |............|
                    else{
                        // do nothing
                    }
                }
                if(this._group_detect.get(i).get_line_num() == 0){
                    this._group_detect.remove(i);
                    i--;
                }
            }
            for(int i = 0; i < this._group_detect.size(); i++ ){
                if(this._group_detect.get(i).status == 0){
//                    gd.autoGenLane2(this._group_detect.get(i));
//                    gd.collectLaneExample(this._group_detect.get(i).get_most_left_line(), this._group_detect.get(i).get_most_right_line());
                    this._car_num += this._group_detect.get(i).carSeperate(this._lines, this._lane);
                    
                   
                    this._group_detect.remove(i);
                }
            }

            //----------------------------Auto Gen-----------------------------
//            System.out.println("----Auto Generate Lane----");
//            List<int[]> lane = gd.getLane();
//            for (int i = 0; i < lane.size(); i++) {
//                System.out.println("Lane" + i + ": Left = " + lane.get(i)[0] + "    Right = " + lane.get(i)[1] + "    Count = " + lane.get(i)[2]); 
//            }
            
            //-----------------------------------------------------------------
            
            //---print---
//            System.out.println("GroupDetect Num = " + this._group_detect.size());   
//            System.out.println("Car count = " + this._car_num);
            this._total_car = 0;
            for(int i = 0; i < this._lane.length; i++){
//                System.out.print("Lane" + i + ": car count = " + this._lane[i].getCarcount() + "     ");
                this._total_car += this._lane[i].getCarcount();
            }
//            System.out.println();


            //write excel file
    //        this.writeXLSFile(this._distance, this._row);

            this._row++;
                       
            Thread.sleep(this._interval);
            
        }catch(Exception ex){
            ex.printStackTrace();
            return;
        }
        //------------------------------- 
    }
    //---------------------------------------------------------------------
    
    //------------------------Auto Create Lanes------------------------
    private void autoCreateLane(){
        ArrayList<Lane> lane = new ArrayList<Lane>();
        
        for(int i = 0; i < this._group_detect.size(); i++){
            if(this._group_detect.get(i).status == 0){
                for (int j = 0; j < this._group_detect.get(i).getLine().length; j++) {
                    
                }
            }
        }
    }
    
    //-----------------------------------------------------------------
    
    public boolean coutReset(){
        this._car_num = 0;
        for(int i = 0; i < this._lane.length; i++){
           this._lane[i].resetCarcount();
        }
        return true;
    }
    
    public boolean resetLinesMaxDistance(){
        this._lines.resetMaxDistance();
        return true;
    }
    
    public boolean showDistance(Lines lines){
        try{
            String text = "Line:\t\t";
            for(int i = 0; i < lines.length(); i++){
                text += "  " + lines.line[i].getId() + "\t";
            }
            
            text += "\nMax:\t\t";
            for(int i = 0; i < lines.line.length; i++){
                if(lines.line[i].getMax() / 100 == 0){
                    text += " ";
                    if(lines.line[i].getMax() / 10 == 0){
                        text += " ";
                    }
                }
                text += lines.line[i].getMax() + "\t";
            }
            
            text += "\nMin:\t\t";
            for(int i = 0; i < lines.line.length; i++){
                if(lines.line[i].getMin() / 100 == 0){
                    text += " ";
                    if(lines.line[i].getMin() / 10 == 0){
                        text += " ";
                    }
                }
                text += lines.line[i].getMin() + "\t";
            }
            
            text += "\nDistance:\t";
            for(int i = 0; i < lines.line.length; i++){
                if(lines.line[i].distance / 100 == 0){
                    text += " ";
                    if(lines.line[i].distance / 10 == 0){
                        text += " ";
                    }
                }
                text += lines.line[i].distance + "\t";
            }
            
            this._controller.distanceTextArea.setFont(new Font(13));
            this._controller.distanceTextArea.setText(text);
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    public boolean addDetectLog(){
        try{
            for(int i = 0; i < this._detectedLog.length - 1; i++)
                for(int j = 0; j < this._detectedLog[i].length; j++)
                    this._detectedLog[this._detectedLog.length - (i + 1)][j] = this._detectedLog[this._detectedLog.length - (i + 2)][j];
                
            for(int i = 0; i < this._lines.length(); i++)
                if(this._lines.line[i].detected)
                    this._detectedLog[0][i] = 1;
                else
                    this._detectedLog[0][i] = 0;           
        }catch(Exception ex){
//            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    //-------------------------------Read Config-------------------------------
    public boolean readConfig(){
        readInputConfig();
        readOutputConfig();
        return true;
    }
    
    //-------------------------------Read Config-------------------------------
    public boolean readInputConfig(){
        try{
            alisa.json.Parser parser = new alisa.json.Parser();
            alisa.json.Object root = parser.load("config.json");
            if (root == null) { 
                this._status = "Error on reading input_config.json"; 
                return false; 
            }
            //lines
            alisa.json.Data lines = root.findData("input");
            if (lines == null || !lines.isArray()){
                this._status = "Wrong input_config.json."; 
                return false;
            }
            int length = lines.getArray().countObjects();
//            System.out.println("length = " + length);
            for (int i = 0; i < length; i++){
                alisa.json.Object obj = lines.getArray().getObject(i);
                String id = getStringJson(obj, "id");
                int min = getIntegerJson(obj, "min");
                int max = getIntegerJson(obj, "max");
                this._lines.line[i] = new Line(id.charAt(0));
                this._lines.line[i].setMinMax(min, max);
//                this._lines.line[this._lines.findById(id)].setMinMax(min, max);
//                alisa.json.Object obj = lines.getArray().getObject(i);
//                String id = getStringJson(obj, "id");
//                int min = getIntegerJson(obj, "min");
//                int max = getIntegerJson(obj, "max");
//                this._lines.line[i] = new Line(id.charAt(0));
//                this._lines.line[i].setMinMax(min, max);
                System.out.println("line " + i + ":     id = " + this._lines.line[i].getId() + "     min = " + this._lines.line[i].getMin() + "     max = " + this._lines.line[i].getMax());
            }           
        }
        catch (Exception ex) {
            this._status = "Read input_config.json Fail!!";
            ex.printStackTrace();
            return false;
        }
//        this._status = "Running";
        return true;
    }
    
    public boolean readOutputConfig(){
        try{
            alisa.json.Parser parser = new alisa.json.Parser();
            alisa.json.Object root = parser.load("config.json");
            if (root == null) { 
                this._status = "Error on reading output_config.json"; 
                return false; 
            }
            //lines
            alisa.json.Data lines = root.findData("output");
            if (lines == null || !lines.isArray()){
                this._status = "Wrong output_config.json"; 
                return false;
            }
            int length = lines.getArray().countObjects();
            this._lane = new Lane[length];
            
            for (int i = 0; i < length ; i++){
                alisa.json.Object obj = lines.getArray().getObject(i);
                int id = getIntegerJson(obj, "id");
                this._lane[i] = new Lane(id);
                String input = getStringJson(obj, "input"); 
                char[] in = new char[input.length()];
                input.getChars(0, input.length(), in, 0);         
                int[] in2 = new int[in.length];
                for(int j = 0; j < in.length; j++){
                    this._lines.line[this._lines.findById("" + in[j])].setLane(this._lane[i].getId());
                    in2[j] = this._lines.findById("" + in[j]);
                }
                this._lane[i].setLine(in2);
                System.out.println("lane " + i + ":     id = " + id + "     input = " + input );
            }    
            
            for(int i = 0; i < 6; i++){
                if(i < this._lane.length){
                    this._controller.text[i].setVisible(true);
                    this._controller.text[i].setText(this._lane[i].getId() + "");
                    this._controller.lane[i].setVisible(true);
                }else{
                    this._controller.text[i].setVisible(false);
                    this._controller.lane[i].setVisible(false);
                }
            }
            for(int i = 0; i < this._lines.length(); i++){
//                System.out.println("line" + i + ": id = " + this._lines.line[i].getId() + "  min = " + this._lines.line[i].getMin() + "  max = " + this._lines.line[i].getMax() + "     lane = " + this._lines.line[i].getLaneId() );
                this._controller.max[i].setText(this._lines.line[i].getMax() + "");
                this._controller.min[i].setText(this._lines.line[i].getMin() + "");
            }
        }
        catch (Exception ex) {
            this._status = "Read output_config.json Fail!!";
            ex.printStackTrace();
            return false;
        }
//        this._status = "Running";
        return true;
    }
    //------------------------------------------------------------------------
    
    //-----------------function get value from object---------------------
    public String getStringJson(alisa.json.Object obj, String name){
        alisa.json.Data text = obj.findData(name);
        if (text == null || !text.isString()){
            this._error = "Error on " + name; 
            return null;
        }
        return text.getString();
    }

    public int getIntegerJson(alisa.json.Object obj, String name){
        alisa.json.Data text = obj.findData(name);
        if (text == null || !text.isInteger()){
            this._error = "Error on" + name; 
            return -1;
        }
        return text.getInteger();
    }    
    //--------------------------------------------------------------------
}

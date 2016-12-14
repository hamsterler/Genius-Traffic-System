package tofgui_v2;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


public class Serial extends Thread
{ 
    private String _error = "";
    public boolean exit = false;
    
    private InputStream _in = null;
    private OutputStream _out = null;
            
    private CommPort _comm_port = null;
    
    private String _port_name = "";
    
    private int[] _distance = new int[16];
    
    private static String _excelFileName = "";
    private static HSSFSheet _sheet;
    private static HSSFWorkbook _wb;
    
//    private int[] _max_distance = new int[16];
//    private int[] _detected = new int[16]; 
//    private int[] _pre_detected = new int[16];
//    private int[][] _car_found = new int[10][7];
    private int _car_num = 0;
    private int _index = 0;
    private boolean _is_distance_in_use = false;
   

    private int[][] _detectedLog = new int[100][16];
    private Draw _draw;
    private Draw2 _drawDetect;
    private DrawSquare _draw_square;
    
    //******
    private Lane[] _lane;
//    private Line[] _line = new Line[16];
    private Lines _lines = new Lines(16);
    private int _group_detect_num = 100;
    private GroupDetect[] _group_detect = new GroupDetect[_group_detect_num];
            
    public int[] min = new int[16];
    public int[] max = new int[16];
    
    private boolean _pause = false;
    
    public boolean pauseStatus(){ return this._pause;}
    public void pause(){ this._pause = true;}
    public void unpause(){ this._pause = false;}
    
    public Serial(String port_name, Canvas canvasLine, Canvas canvasDetect, Canvas canvasSquare)
    {
        this._port_name = port_name;
        
        for(int i = 0; i < this._detectedLog.length; i++){
            for(int j = 0; j < this._detectedLog[i].length; j++){
                this._detectedLog[i][j] = 0;
            }
        }
        for(int i = 0; i < this._lines.length(); i++){
            this._lines.line[i] = new Line("A",1,0,100);
        }
        for(int i = 0; i < this._group_detect.length; i++)
            this._group_detect[i] = new GroupDetect();

        
        this._row = 0;
        _excelFileName = "D:/Test8.xls";//name of excel file
	String sheetName = "Sheet1";//name of sheet
	_wb = new HSSFWorkbook();
	_sheet = _wb.createSheet(sheetName) ;
        
        _draw = new Draw(canvasLine, 200, 16);
        _drawDetect = new Draw2(canvasDetect, 16);
        _draw_square = new DrawSquare(canvasSquare, 16);
    }
    
    public boolean connect()
    {
        try
        {
            System.out.println("Open port " + this._port_name + "...");           
            
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this._port_name);
            if (portIdentifier.isCurrentlyOwned()) 
            {
                System.out.println("Error: Port is currently in use"); 
                return false;
            }
            else 
            {
                this._comm_port = portIdentifier.open(this.getClass().getName(), 6000);

                if (this._comm_port instanceof SerialPort) 
                {
                    int timeout = 5000;
                    int baudrate = 115200;
                    
                    SerialPort serial_port = (SerialPort)this._comm_port;
                    serial_port.enableReceiveTimeout(timeout); // timeout
                    serial_port.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    serial_port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    System.out.print("BaudRate: " + serial_port.getBaudRate() + ", ");
                    System.out.print("DataBIts: " + serial_port.getDataBits() + ", ");
                    System.out.print("StopBits: " + serial_port.getStopBits() + ", ");
                    System.out.print("Parity: " + serial_port.getParity() + ", ");
                    System.out.println("FlowControl: " + serial_port.getFlowControlMode());
                    this._in = serial_port.getInputStream();
                    this._out = serial_port.getOutputStream();
                    
                    System.out.println();
                }
                else 
                {
                    System.out.println("Error: Only serial ports are handled by this example.");
                    return false;
                }
            }
        }
        catch (Exception ex) 
        {
            ex.printStackTrace();
            return false; 
        }
        
        return true;
    }
    
    public void reconnect()
    {
        if (this._comm_port != null) this._comm_port.close();
        this.connect();
    }
    
    public void disconnect()
    {
        this.exit = true;
        this.interrupt();
        
        if (this._comm_port != null) this._comm_port.close();
        this._comm_port = null;
    }
    
    @Override
    public void run() 
    {
        this.connect();
        readInputConfig();   
        readOutputConfig();
        while (true)
        {
            //if pause
            while(this._pause){}
            
            if (exit)
            {
                return;
            }
            
            String s = "";
            try 
            {
                // write
                {
                    byte []b = new byte[8];
                    
                    b[0] = 1;       // slave id
                    
                    b[1] = 0x04;    // function code
                    
                    b[2] = 0;       // data start [HI]
                    b[3] = 16;      // data start [LO]
                    
                    b[4] = 0;       // data length [HI]
                    b[5] = 16;       // data length [LO]
                    
                    int crc16 = alisa.CRC.crc16(b, 0, 6);
                    b[6] = (byte)(crc16 >> 8); // high
                    b[7] = (byte)(crc16 & 0xFF); // low

                    this._out.write(b);
                }                
                
                // wait for response
                Thread.sleep(200);
                
                // read                
                if (this._in.available() > 0)
                {
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    while (this._in.available() > 0)
                    {
                        int l = this._in.read(buffer, length, buffer.length - length);
                        length += l;
                        Thread.sleep(1);
                    }
                    
                    //int length = this._in.read(buffer);
                    if (length >= 37)
                    {
                        if (buffer[0] == 1 && buffer[1] == 0x04) // slave_id & function code
                        {                            
                            if (alisa.CRC.crc16(buffer, 0, 37) == 0) // check CRC16
                            {
                                s += "distance = ";
                                
                                _is_distance_in_use = true;
                                _draw_square.clearCanvas();
                                for (int i = 0; i < 16; i++)
                                {
                                    int d = ((buffer[3+(i*2)] & 0xFF) << 8) + (buffer[4+(i*2)] & 0xFF);
                                    this._lines.line[i].distance = d;
                                    this._distance[i] = d;
                                    s += d + ",";  
                                    
                                    //assign max_distance
                                    if(this._row == 0)
                                        this._lines.line[i].max_distance = this._lines.line[i].distance;
                                    else{
                                        if(this._lines.line[i].max_distance < this._lines.line[i].distance)
                                            this._lines.line[i].max_distance = this._lines.line[i].distance;
                                    }
                                //------------------check for detection--------------------
                                    
                                    //Case1: if first detected (change from 0 -> 1)
                                    if(this._lines.line[i].detect() && !this._lines.line[i].detected){
                                        this._lines.line[i].detected = true;
                                        this._lines.line[i].firstDetectRow = this._row;
                                        boolean count = true;
                                        
                                        for(int j = 0; j < this._group_detect.length; j++){
                                            if(this._group_detect[j].status == 1){  //if this _car_found does not have end row yet
                                                if(this._row - this._group_detect[j].first_row <= 5 && this._group_detect[j].first_row != -1){
                                                    count = false;
                                                    //add line in this group
                                                    this._group_detect[j].addLine(i);
                                                }
                                            }
                                        }
                                        // if this line is not a member of any car_found then create a new one
                                        if(count){
//                                            while(true){
//                                                if(this._group_detect[this._index % _group_detect_num].status == -1)
//                                                    break;
//                                                else
//                                                    this._index++;
//                                            }
                                            this._index++;
                                            this._group_detect[(this._index) % _group_detect_num].first_row = this._row;
                                            this._group_detect[this._index % _group_detect_num].line_num = 0;
                                            this._group_detect[this._index % _group_detect_num].line_num_check = 0;
                                            this._group_detect[this._index % _group_detect_num].status = 1;
                                            this._group_detect[this._index % _group_detect_num].addLine(i);
                                            this._index++;
                                        }    
                                        
//                                    
                                    //Case2: if this line change from detected to not detected (1 -> 0)
                                    }else if(!this._lines.line[i].detect() && this._lines.line[i].detected){
                                        this._lines.line[i].detected = false;
                                        this._lines.line[i].lastDetectRow = this._row;
                                        for(int j = 0; j < this._group_detect.length; j++ ){
                                            if(this._group_detect[j].status == 1 && this._lines.line[i].firstDetectRow - this._group_detect[j].first_row <= 3){

                                                if(this._lines.line[i].lastDetectRow > this._group_detect[j].end_row){
                                                    this._group_detect[j].end_row = this._lines.line[i].lastDetectRow;
                                                }
                                                //--new--
//                                                this._group_detect[j].removeLine(i);
//                                                if(this._group_detect[j].line_num == 0)
//                                                    this._group_detect[j].status = 0;  //count process
                                                //-------
                                                this._group_detect[j].line_num_check++;
                                                if(this._group_detect[j].line_num == this._group_detect[j].line_num_check)
                                                    this._group_detect[j].status = 0;  //count process
                                            }
                                        }
                                    } 
                                    _draw_square.draw(i, this._lines.line[i].detected);
                                //-----------------------------------------------------------
                                }  
                                System.out.println("ok1");
                                addDetectLog();
                                _is_distance_in_use = false;
                                
                                System.out.println(s);
                                
                                
                                //------------------------car count process------------------------
                                
                                for(int i = 0; i < this._group_detect.length; i++ ){
//                                    System.out.println("ok2");
                                    //in case that this car_found line member does not already end at all(change from detect to non detect every line member) but the last  
                                    if(this._group_detect[i].status == 1 && this._group_detect[i].end_row != -1){
                                        if(this._row - this._group_detect[i].end_row > 3) {
                                            this._group_detect[i].status = 0;
                                        }
                                    }
                                    
                                    if(this._group_detect[i].status == 0){
                                        //in case like his -> ...|...
                                        //                    ...|...
                                        //                    |||||||
                                        if(this._group_detect[i].line_num_check != this._group_detect[i].line_num){
//                                            this._car_num += 1;  //count 1 car
                                            this._group_detect[this._index % _group_detect_num].first_row = this._group_detect[i].first_row;
                                            this._group_detect[this._index % _group_detect_num].end_row = -1;
                                            this._group_detect[this._index % _group_detect_num].line_num = this._group_detect[i].line_num - this._group_detect[i].line_num_check;
                                            this._group_detect[this._index % _group_detect_num].line_num_check = 0;
                                            this._group_detect[this._index % _group_detect_num].status = 1;
                                            int[] line = this._group_detect[i].getLine();
                                            for(int j = 0; j < line.length; j++){
                                                if(this._lines.line[line[j]].lastDetectRow == -1){
                                                    this._group_detect[i].removeLine(line[j]);
                                                    this._group_detect[i].line_num_check--;
                                                    this._group_detect[this._index % _group_detect_num].addLine(line[j]);
                                                }
                                            }
                                            this._car_num += this._group_detect[i].carSeperate();
                                            this._group_detect[i].status = -1;
                                        }
                                        //in case  -> ........
                                        //            .||.|.|.
                                        //            .||||||.
                                        //            .||||||.
                                        else{
                                            this._car_num += this._group_detect[i].carSeperate();
                                            this._group_detect[i].status = -1;
                                        }
                                    }
                                }
                                //-----------------------------------------------------------------
                               
                                
                                System.out.println("Car count = " + this._car_num);
                                
                                //write excel file
                                this.writeXLSFile(this._distance, this._row);
                                
                                this._row++;
                                
                                //draw detected graph
                                this._drawDetect.clearCanvas();
                                this._drawDetect.draw(this._detectedLog);
                                 
                                //----draw line----
                                this._draw.clearCanvas();
                                this._draw.draw();      //<< draw default line (a green one)
                                
                                this._draw.drawMinMaxLine(this._lines.line, Color.valueOf("#3498DB"), 4);  //<< (blue line)
                                this._draw.drawDistancePoint(this._lines.line, Color.valueOf("#E74C3C"));    //<<draw red dot
                                //-------------------------------
                                
                            }
                            else System.out.println("Error-3");
                        }
                        else System.out.println("Error-2");
                    }
                    else System.out.println("Error-1 (" + length + ")");
                }                
            } 
            catch (Exception ex) 
            {                
                // reconnect
                try 
                {
                    System.out.println("Error: " + ex.getMessage());
                    ex.printStackTrace();
                    this.reconnect();
                    Thread.sleep(1000);
                    _is_distance_in_use = false;
                } 
                catch (Exception ex2) { }
            }
        }
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
    
    public synchronized int[] getDistance(){
//        while(_is_distance_in_use){}
        return this._distance;
    }
    
    public boolean readInputConfig(){
        try{
            alisa.json.Parser parser = new alisa.json.Parser();
            alisa.json.Object root = parser.load("input_config.json");
            if (root == null) { 
                this._error = "Error on reading input_config.json file."; 
                return false; 
            }
            //lines
            alisa.json.Data lines = root.findData("input");
            if (lines == null || !lines.isArray()){
                this._error = "Error on file."; 
                return false;
            }
            int length = lines.getArray().countObjects();
            System.out.println("length = " + length);
            for (int i = 0; i < this._lines.length(); i++){
                alisa.json.Object obj = lines.getArray().getObject(i);
                String id = getStringJson(obj, "id");
                int min = getIntegerJson(obj, "min");
                int max = getIntegerJson(obj, "max");
                this._lines.line[i] = new Line(id, -1, min, max);
                System.out.println("line " + i + ":     id = " + this._lines.line[i].getId() + "     min = " + this._lines.line[i].getMin() + "     max = " + this._lines.line[i].getMax());
            }           
        }
        catch (Exception ex) {
            System.out.println("Error");
            return false;
        }
        return true;
    }
    
    public boolean readOutputConfig(){
        try{
            alisa.json.Parser parser = new alisa.json.Parser();
            alisa.json.Object root = parser.load("Output_config.json");
            if (root == null) { 
                this._error = "Error on reading input_config.json file."; 
                return false; 
            }
            //lines
            alisa.json.Data lines = root.findData("output");
            if (lines == null || !lines.isArray()){
                this._error = "Error on file."; 
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
                this._lane[i].setLine(in);
                for(int j = 0; j < in.length; j++){
                    this._lines.line[this._lines.findById("" + in[j])].setLane(this._lane[i].getId());
                }
//                System.out.println("lane " + i + ":     id = " + id + "     input = " + input + "input[0] = " + in[in.length-1] );
            }     
            //--------show lane--------
            for(int i = 0; i < this._lane.length; i++){
                System.out.print("lane" + i + ": id = " + this._lane[i].getId());
                for(int j = 0; j < this._lane[i].getLine().length; j++)
                    System.out.print("  line" + j + " = " + this._lane[i].getLine()[j]);
                System.out.println("");
            }
            //--------show lines-------
            for(int i = 0; i < this._lines.length(); i++)
                System.out.println("line" + i + ": id = " + this._lines.line[i].getId() + "  min = " + this._lines.line[i].getMin() + "  max = " + this._lines.line[i].getMax() + "     lane = " + this._lines.line[i].getLane() );
            
        }
        catch (Exception ex) {
            System.out.println("Error");
            return false;
        }
        return true;
    }
    
    public int _row;
    public void writeXLSFile(int[] distance, int r) throws IOException {
		
	//iterating r number of rows
	HSSFRow row = _sheet.createRow(r);
        if(r==0){
            for (int c=0;c < 16; c++ ){
		HSSFCell cell = row.createCell(c);
                cell.setCellValue(c);
            }
        }else{
            //iterating c number of columns
            for (int c = 0;c < distance.length; c++ )
            {
                HSSFCell cell = row.createCell(c);

                cell.setCellValue(distance[c] * (-1) + this._lines.line[c].max_distance);
            }
        }
		
	FileOutputStream fileOut = new FileOutputStream(_excelFileName);
		
	//write this workbook to an Outputstream.
	_wb.write(fileOut);
	fileOut.flush();
	fileOut.close();
    }
    
   
    
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

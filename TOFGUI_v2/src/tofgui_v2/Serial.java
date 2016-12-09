package tofgui_v2;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


public class Serial extends Thread
{ 
    
    public boolean exit = false;
    
    private InputStream _in = null;
    private OutputStream _out = null;
            
    private CommPort _comm_port = null;
    
    private String _port_name = "";
    
    private int[] _distance = new int[16];
    
    private static String _excelFileName = "";
    private static HSSFSheet _sheet;
    private static HSSFWorkbook _wb;
    
    private int[] _max_distance = new int[16];
    private int[][] _detected = new int[16][3]; //0=not found  1=detected and already in use  2=detected
    private int[] _pre_detected = new int[16];
    private int[][] _car_found = new int[10][7];
    private int _car_num = 0;
    private int _index = 0;
    private boolean _is_distance_in_use = false;
    
    private Canvas _canvas;
    private Draw _draw;
    public Serial(String port_name, Canvas canvas)
    {
        this._port_name = port_name;
        this._canvas = canvas;
        for(int i = 0; i < this._car_found.length; i++){
            this._car_found[i][0] = 0;  //first row
             this._car_found[i][1] = -1; //end row
            this._car_found[i][2] = 0;  //most left line
            this._car_found[i][3] = 0;  //most right found
            this._car_found[i][4] = 0;  //line num
            this._car_found[i][5] = 0;  //line num check
            this._car_found[i][6] = -1;  //isAvailable 0 = no, 1 = yes
        }
        for(int i = 0; i < this._detected.length; i++){
            this._detected[i][0] = 0;
            this._detected[i][1] = 0;
            this._detected[i][2] = -1;
            this._pre_detected[i] = 0;
        }
        
        this._row = 0;
        _excelFileName = "D:/Test8.xls";//name of excel file
	String sheetName = "Sheet1";//name of sheet
	_wb = new HSSFWorkbook();
	_sheet = _wb.createSheet(sheetName) ;
        
        _draw = new Draw(canvas, 200, 16);
        
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
                        
        while (true)
        {
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
                                for (int i=0; i<16; i++)
                                {
                                    this._pre_detected[i] = this._detected[i][0];
                                    int d = ((buffer[3+(i*2)] & 0xFF) << 8) + (buffer[4+(i*2)] & 0xFF);
                                    this._distance[i] = d;
                                    s += d + ",";  
                                    //-------update-------
                                    if(this._row == 0)
                                        this._max_distance[i] = this._distance[i];
                                    else{
                                        if(this._max_distance[i] < this._distance[i])
                                            this._max_distance[i] = this._distance[i];
                                    }
                                    if(this._distance[i] < this._max_distance[i] - 50 && this._detected[i][0] == 0)
                                        this._detected[i][0] = 2;
                                    else if(this._distance[i] < this._max_distance[i] - 50 && this._detected[i][0] != 0){
                                    }else
                                        this._detected[i][0] = 0;     
                                    //--------------------
                                }    
                                _is_distance_in_use = false;
                                
                                System.out.println(s);
                                
                                //-----------update--------------
                                for(int k = 0; k < this._detected.length; k++){
                                    //if this line detected
                                    if(this._detected[k][0] == 2 && this._pre_detected[k] == 0){
                                        boolean count = true;
                                        for(int l = 0; l < this._car_found.length; l++){
                                            if(this._car_found[l][5] == -1){  //if this _car_found does not end
                                                if(this._row - this._car_found[l][0] <= 3 && this._car_found[l][0] != 0){
                                                    count = false;
                                                    this._car_found[l][4]++;
                                                    
                                                    //find most left & right 
                                                    if(k < this._car_found[l][2])
                                                        this._car_found[l][2] = k;
                                                    else if(k < this._car_found[l][3])
                                                        this._car_found[l][3] = k;
                                                }
                                            }
                                        }
                                        if(count){
                                            this._car_found[(this._index) % 10][0] = this._row;
                                            this._car_found[(this._index) % 10][2] = k;
                                            this._car_found[(this._index) % 10][3] = k;
                                            this._car_found[_index % 10][4] = 1;
                                            this._car_found[_index % 10][5] = 0;
                                            this._car_found[_index % 10][6] = 1;
                                            this._index++;
                                        }    
                                        this._detected[k][0] = 1; 
                                        this._detected[k][1] = this._row;
                                    
                                    //if this line change from detected to not detected
                                    }else if(this._detected[k][0] == 0 && this._pre_detected[k] != 0){
                                        this._detected[k][2] = this._row;
                                        for(int i = 0; i < this._car_found.length; i++ ){
                                            if(this._car_found[i][6] != -1 && this._detected[k][1] - this._car_found[i][0] <= 3){
                                                //find most left & right (again)
                                                if(k < this._car_found[i][2])
                                                    this._car_found[i][2] = k;
                                                else if(k < this._car_found[i][3])
                                                    this._car_found[i][3] = k;
                                                
                                                if(this._detected[k][2] > this._car_found[i][1]){
                                                    this._car_found[i][1] = this._detected[k][2];
                                                    this._car_found[i][5]++;
                                                }
                                                this._car_found[i][6] = 0;
                                            }
                                        }
                                        
                                    }
                                }
                                
                                for(int i = 0; i < this._car_found.length; i++ ){
                                    if(this._car_found[i][6] == 1 && this._car_found[i][1] != -1){
                                        if(this._row - this._car_found[i][1] > 3) {
                                            this._car_found[i][6] = 0;
                                        }
                                    }
                                    if(this._car_found[i][6] == 0){
                                        if(this._car_found[i][5] != this._car_found[i][4]){
                                            this._car_num += 1;
                                            this._car_found[i][6] = -1;
                                            while(true){
                                                if(this._car_found[_index % 10][6] == -1){
                                                    this._car_found[_index % 10][0] = this._car_found[i][0];
                                                    this._car_found[_index % 10][1] = 0;
                                                    this._car_found[_index % 10][2] = 16;
                                                    this._car_found[_index % 10][3] = 0;
                                                    this._car_found[_index % 10][4] = this._car_found[i][4] - this._car_found[i][5];
                                                    this._car_found[_index % 10][5] = 0;
                                                    this._car_found[_index % 10][6] = 1;
                                                    break;
                                                }else{
                                                    _index++;
                                                }
                                            }
                                        }
                                        else{
                                            if(this._car_found[i][3] - this._car_found[i][2] + 1 == this._car_found[i][4]){
                                                this._car_num += 1;
                                                this._car_found[i][6] = -1;
                                            }else{
                                                if(this._car_found[i][4] - (this._car_found[i][3] - this._car_found[i][2] + 1) == 1 ){
                                                    this._car_num += 2;
                                                    this._car_found[i][6] = -1;
                                                }else { //กรณี เส้นเว้นตรงกลางมากกว่า 1 เส้น = อาจมีรถมากกว่า 2คัน
                                                    // to be continue
                                                    this._car_num += 2;
                                                    this._car_found[i][6] = -1;
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                
                                System.out.println("Car count = " + this._car_num);
                                writeXLSFile(this._distance, this._row);
                                this._row++;
                                
                                 //----draw line----
                                _draw.clearCanvas();
                                _draw.draw();      //<< draw default line (a green one)
                                int[] min= new int[16];
                                for(int i =0 ;i<16;i++)
                                    min[i] = _max_distance[i] - 100;
                                _draw.drawMinMaxLine(min, _max_distance, Color.valueOf("#3498DB"), 4);  //<< (blue line)
                                _draw.drawDistancePoint(_distance, _max_distance, Color.valueOf("#E74C3C"));    //<<draw red dot
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
                    this.reconnect();
                    Thread.sleep(1000);
                    _is_distance_in_use = false;
                } 
                catch (Exception ex2) { }
            }
        }
    } 
    
    public synchronized int[] getDistance(){
//        while(_is_distance_in_use){}
        return this._distance;
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

                cell.setCellValue(distance[c] * (-1) + this._max_distance[c]);
            }
        }
		
	FileOutputStream fileOut = new FileOutputStream(_excelFileName);
		
	//write this workbook to an Outputstream.
	_wb.write(fileOut);
	fileOut.flush();
	fileOut.close();
    }
}

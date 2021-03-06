package tof_test;


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
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

//import org.apache.poi.hssf.usermodel.HSSFCell;
//import org.apache.poi.hssf.usermodel.HSSFRow;
//import org.apache.poi.hssf.usermodel.HSSFSheet;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class Serial2 /*extends Thread*/
{
    private String _error = "";
    public boolean exit = false;
    
    private InputStream _in = null;
    private OutputStream _out = null;
            
    private CommPort _comm_port = null;
    
    private String _port_name = "";
    
    private int[] _distance = new int[16];

    public Serial2(String port_name)
    {  
        this._port_name = port_name;
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
                this._error = "Serial Connection Fail.";
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
                    this._error = "";
                }
                else 
                {
                    System.out.println("Error: Only serial ports are handled by this example.");
                    this._error = "Serial Connection Fail.";
                    return false;
                }
            }
        }
        catch (Exception ex) 
        {
            ex.printStackTrace();
            this._error = "Serial Connection Fail.";
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
//        this.interrupt();
        
        if (this._comm_port != null) this._comm_port.close();
        this._comm_port = null;
    }

//    @Override
    public void run() 
    {
            
            if (exit)
            {
                return ;
            }
            
            String s = "";
            try 
            {
            //------------------------------1-----------------------------------    
//                // write
//                {
//                    byte []b = new byte[8];
//                    
//                    b[0] = 1;       // slave id
//                    
//                    b[1] = 0x04;    // function code
//                    
//                    b[2] = 0;       // data start [HI]
//                    b[3] = 16;      // data start [LO]
//                    
//                    b[4] = 0;       // data length [HI]
//                    b[5] = 16;       // data length [LO]
//                    
//                    int crc16 = alisa.CRC.crc16(b, 0, 6);
//                    b[6] = (byte)(crc16 >> 8); // high
//                    b[7] = (byte)(crc16 & 0xFF); // low
//
//                    this._out.write(b);
//                }                
//                
//                // wait for response
//                Thread.sleep(200);
//                
//                // read                
//                if (this._in.available() > 0)
//                {
//                    byte[] buffer = new byte[1024];
//                    int length = 0;
//                    while (this._in.available() > 0)
//                    {
//                        int l = this._in.read(buffer, length, buffer.length - length);
//                        length += l;
//                        Thread.sleep(1);
//                    }
//                    
//                    //int length = this._in.read(buffer);
//                    if (length >= 37)
//                    {
//                        if (buffer[0] == 1 && buffer[1] == 0x04) // slave_id & function code
//                        {                            
//                            if (alisa.CRC.crc16(buffer, 0, 37) == 0) // check CRC16
//                            {
////                                s += "distance = ";
//                                
//                                for (int i = 0; i < 16; i++)
//                                {
//                                    int d = ((buffer[3+(i*2)] & 0xFF) << 8) + (buffer[4+(i*2)] & 0xFF);
//                                    this._distance[i] = d;
////                                    s += d + ",";  
//                                    
//                                }
//                                this._error = "";
//                            }
//                            else System.out.println("Error-3");
//                        }
//                        else System.out.println("Error-2");
//                    }
//                    else System.out.println("Error-1 (" + length + ")");
//                }    
            //------------------------------------------------------------------
                
            //------------------------------2-----------------------------------
//                 write
                {
                    byte []b = new byte[4];
                    b[0] = 1;       // slave id
                    b[1] = 0x41;    // function code
                    
                    int crc16 = alisa.CRC.crc16(b, 0, 2);
                    b[2] = (byte)(crc16 >> 8); // high
                    b[3] = (byte)(crc16 & 0xFF); // low

                    this._out.write(b);
                }
                
                // wait for response
                Thread.sleep(100);
                System.out.println("...");
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
                    System.out.println("Length = " + length);
                    
//                    System.out.print("Message:    ");
//                    for (int i = 0; i < length; i++) {
//                        System.out.print((buffer[i] & 0xff) + "    ");
//                    }
                    System.out.println("");
                    if (length == 60)
                    {
                        // Leddar VU
                        int index = 0;
//                        while(index < length){ 
//                            if(buffer[index] == 1)
//                                break;
//                            index++; 
//                        }
                        if (buffer[index] == 1 && buffer[index + 1] == 0x41 && buffer[index + 2] == 8) // slave_id & function code & detectors
                        {                            
                            if (alisa.CRC.crc16(buffer, index, 60) == 0) // check CRC16
                            {
                                s += "VU : distance = ";
                                int offset = index + 3;
                                for (int i=0; i<8; i++)
                                {
                                    int d = (buffer[offset] & 0xFF) + ((buffer[offset+1] & 0xFF) << 8); // [LO][HI]
                                    this._distance[i] = d;
                                    s += d + ",";
                                    offset += 6;
                                }                                
                                System.out.println("length: " + length + "  Start:" + index + "  " + s);
                            }                            
                        }
                        // Leddar VU
//                        if (buffer[0] == 1 && buffer[1] == 0x41 && buffer[2] == 8) // slave_id & function code & detectors
//                        {                            
//                            if (alisa.CRC.crc16(buffer, 0, 60) == 0) // check CRC16
//                            {
//                                s += "VU : distance = ";
//                                int offset = 3;
//                                for (int i=0; i<8; i++)
//                                {
//                                    int d = (buffer[offset] & 0xFF) + ((buffer[offset+1] & 0xFF) << 8); // [LO][HI]
//                                    this._distance[i] = d;
//                                    s += d + ",";
//                                    offset += 6;
//                                }                                
//                                System.out.println(s);
//                            }                            
//                        }
                    }
                    else if(length >= 91)
                    {
//                        System.out.println("91");
                        int index = 0;
                        for(int i = 0; i < length;i++){
                            if(buffer[i] == 1 && buffer[i + 1] == 0x41 && buffer[i + 2] == 16){
                                index = i;
                                break;
                            }
                        }
                        
                        if (buffer[index] == 1 && buffer[index + 1] == 0x41 && buffer[index + 2] == 16) // slave_id & function code & detectors
                        {
                            if (alisa.CRC.crc16(buffer, index + 0, index + 91) == 0) // check CRC16
                            {
                                System.out.println("length = " + length);
                                s += "M16 : distance = ";
                                int offset = 3;
                                for (int i=0; i<16; i++)
                                {
                                    int d = (buffer[index + offset] & 0xFF) + ((buffer[index + offset+1] & 0xFF) << 8); // [LO][HI]
                                    this._distance[i] = d;
                                    s += d + ",";
                                    offset += 5;
                                }                                
                                System.out.println(s);
                            } else{
                                System.out.println("Wrong CRC");
                            }    
                        }     
                        else{
                            System.out.println("Fail Length = " + length);
                        }                        
                    }
                    else{
                        System.out.println("No message");
                    }
                    Thread.sleep(400);
                }                
            //------------------------------------------------------------------    
            } 
            catch (Exception ex) 
            {                
                // reconnect
                try 
                {
                    System.out.println("Error: " + ex.getMessage());
                    this._error = "Serial Connection Fail";
                    ex.printStackTrace();
                    this.reconnect();
                    Thread.sleep(1000);
                } 
                catch (Exception ex2) { }
                
            }
    } 
    public synchronized boolean getDetection(){              
        //--------------Send--------------
        try{
            
            byte[] data = new byte[3];
            data[0] = (byte)0xff;
            data[1] = (byte)3;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 

            this._out.write(data);
            System.out.println("Sent GetDetection");       
        } 
        catch (Exception ex){
            this._error = "getDistance | Write | Exception Error | " + ex.getMessage();
            return false;
        }
        
        //---------------Sleep----------------
        try{  Thread.sleep(300);  }
        catch (InterruptedException  ex ){  return false; }
        //------------------------------------
        
        //--------------Receive---------------    
        int[] result = new int[8 + 1];
        try{
            byte[] buffer = new byte[1024];
            
            int length = this._in.read(buffer);
                     
            System.out.println(length);
           
            if (length >= 37 && buffer[0] == (byte)0xff  && buffer[1] == 3 ){ // slave_id & function code  
                if (alisa.CRC.crc8(buffer, 0, 37) == 0){ // check CRC8
                    
                    System.out.println("Received!!!!");
                    try{
                        //connected
                        int connected = (int)(buffer[34] & 0xff);
//                        if(connected == 1)
//                            this._leddar_connected = true;
//                        else 
//                            this._leddar_connected = false;
                        
                        //detected
                        int detected = (int)(buffer[35] & 0xff);
//                        if(detected == 1)
//                            this._detected = true;
//                        else
//                            this._detected = false;
                        
                        //distance 
                        System.out.print("Distance: ");
                        for (int i = 0; i < 8; i++){
                            int a = 0;
                            try{
                                a = ((int)(buffer[2*i + 2] & 0xff) + (int)((buffer[2*i + 3] & 0xff) << 8));
                                this._distance[i] = a;
                                //find new max_distance
//                                if(this._distance[i] > this.max_distance[i])
//                                    this.max_distance[i] = this._distance[i];
                                
                                //check for each line detection
//                                if(this._distance[i] >= this._min[i] && this._distance[i] <= this._max[i] && this._detected)
//                                    this._line_detected[i] = true;
//                                else if(!this._detected)
//                                    this._line_detected[i] = false;
//                                else
//                                    this._line_detected[i] = false;
                                  
                            }catch(Exception e){
//                                e.printStackTrace();
                                this._distance[i] = 0;
                            }
                            System.out.print( a + " " );
                        }    
                        
                        System.out.println();
                    }catch(Exception e){
                        System.out.println("Error: " + e.getMessage());
//                        _serial_status = 0;
                   }
                } 
                else{
                    this._error = "getDistance | Read | CRC Error";
//                    this._connected = false;
//                    _serial_status = 0;
                    return false;
                }
//                this._connected = true;
            } 
            else{
                this._error = "getDistance | Read | Data Error";
//                this._connected = false;
//                _serial_status = 0;
                return false;
            }   
        }
        catch (Exception ex){
            this._error = "getDistance | Read | Exception Error | " + ex.getMessage();
//            this.distance = -3; 
//            this._connected = false;
//            this._reconnect();
//            _serial_status = 0;
            return false;
        }
//        _serial_status = 0;
        return true;
    }
    public String getError(){
        return this._error;
    }
    
    public int[] getDistance(){
//        while(_is_distance_in_use){}
        return this._distance;
    }
    


}

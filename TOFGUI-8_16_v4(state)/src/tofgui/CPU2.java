package tofgui;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
public class CPU2 
{
    public int present_command = 0;
    public static final int Idle = -1;
    public static final int GetVersion = 0;
    public static final int SetConfig = 1;
    public static final int GetConfig = 2;
    public static final int GetDetection = 3;
    public static final int GetSystemStatus = 100;
    public int distance = 0;
    private int board_type = 3;
    public int line_num = 8; 
    public int interval = 300;
    private boolean _serial_connect;
    private boolean _serial_idle = true;
    public int[] buffer;
    int[] min_distance;
    int[] max_distance;
    
    private int[] _min = new int[8];
    private int[] _max = new int[8];
    private int[] _distance = new int[8];
    private boolean _leddar_connected = false;
    private boolean _detected = false;
    private String _error = "";
    private InputStream _in = null;
    private OutputStream _out = null;
    private String _port = "";
    private CommPort _comm_port = null;
    private boolean _connected = false;
    private String _version = "";
    private int _in_sensitivity = 0;
    private int _out_sensitivity = 0;
    private int _serial_status = 0; //0 = idle, 1 = sending Data , 2 =getting Data, 3 = geting Distance
    
    public CPU2(){
        this.line_num = 8;
    }
    
    public CPU2(String port)
    {
        this.line_num = 8;
        this._port = port;
        _serial_connect = this._reconnect();
    }
    
    public void reconnect(){
        this._serial_connect = _reconnect();
    }
    
    public boolean setPort(String port){
        this._port = port;
        return true;
    }
    
    public void dispose() 
    {
        // disconnect
        if (this._comm_port != null) 
        {
            this._comm_port.close();
        }
    }
    
    
    //-----------------------------Getter-----------------------------
    public boolean getLeddarConnect(){ return this._leddar_connected; }
    public int getInSense(){ return this._in_sensitivity; }
    public int getOutSense(){ return this._out_sensitivity; }
    public int getStatus(){ return _serial_status; }
    public boolean isSerialConnected(){ return this._serial_connect; }
    public boolean isDetected(){ return _detected; }
    public String getVersionString(){ return this._version; }
    public String getError() {  return this._error;  }
    public String getPort(){ return this._port; }
    public boolean isConnected() { return this._connected; }
    //----------------------------------------------------------------
    
    //reconnect
    private boolean _reconnect(){
        try{
            // try to disconnect
            if (this._comm_port != null){
                this._comm_port.close();
            }

            // connect com port
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this._port);
            if (portIdentifier.isCurrentlyOwned()){
                return false;
            }
            else{
                this._comm_port = portIdentifier.open(this.getClass().getName(), 6000);

                if (this._comm_port instanceof SerialPort){
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
                else{
                    return false;
                }
            }
        }catch (Exception ex){/*ex.printStackTrace();*/  
            this._error = "CPU | _reconnect(): " + ex.getMessage();
            return false;
        }
        return true;
    }
    
    public boolean setIdle(){
        this._serial_status = Idle;
        return true;
    }
    public boolean setStatus(int status){
        try{
            while(this._serial_status != Idle){
                //wait
            }
            this._serial_status = status;
        }catch(Exception ex){
            return false;
        }
        return true;
    }
  
    // ---------------- 0. GetVersion ------------------
    public int getMajorVersion() {  return this._major_cpu_version;  }
    private int _major_cpu_version = -1;
    
    public int getMinorVersion(){ return this._minor_cpu_version; }
    private int _minor_cpu_version = -1;
    
    public int getDevice(){ return this._device;}
    private int _device = 4;    //<<<< if _device = 3 >> 16 line, 4 >> 8line
            
    public int [] read_min = new int[8];
    public int [] read_max = new int[8];
    public int read_in_sense = 0;
    public int read_out_sense = 0;
    
    public boolean readConfig(){
        try{
            String path = "config.txt";
            List<String> lines = Files.readAllLines(Paths.get(path),
                    Charset.defaultCharset());
            
            String[] value_min = lines.get(0).split(",");
            String[] value_max = lines.get(1).split(",");
            String in_sense = lines.get(2);
            String out_sense = lines.get(3);
            read_in_sense = Integer.parseInt(in_sense);
            read_out_sense = Integer.parseInt(out_sense);
            
            for(int i=0; i < read_min.length; i++){
                read_min[i] = Integer.parseInt(value_min[i]);
                read_max[i] = Integer.parseInt(value_max[i]);
            }
            
            System.out.print("\nMin = ");
            for(int i=0; i < 8; i++)
                System.out.print(read_min[i] + "  ");
            System.out.print("\nMax = ");
            for(int i=0; i < 8; i++)
                System.out.print(read_max[i] + "  ");          
            System.out.println("In Sensitivity = " + read_in_sense);
            System.out.println("Out Sensitivity = " + read_out_sense);
        }
        catch (Exception ex) {
            return false;
        }
        
        return true;
    }
    
     //0.getVersion
    public boolean getVersion(){
        //--------------Send--------------
        try{
            byte[] data = new byte[4];
            data[0] = (byte)0xff;
            data[1] = (byte)this.GetVersion;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8);
            this._out.write(data);
            System.out.println("Sent");
        } 
        catch (Exception ex){
            ex.printStackTrace();
            this._error = "getVersion | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            return false;
        }

        //---------------Sleep----------------
        try{  Thread.sleep(this.interval);  } 
        catch (InterruptedException ex){ return false;  }
        //------------------------------------
        
        //--------------Receive--------------- 
        try{
            byte[] buffer = new byte[1024];
            int length = this._in.read(buffer);          
            System.out.println(length);
            if (length >= 5 && buffer[0] == (byte)0xff  && buffer[1] == this.GetVersion){ // slave_id & function code                            
                if (alisa.CRC.crc8(buffer, 0, 5) == 0){ // check CRC8
                    this._version = (int)(buffer[2] & 0xff) +  "." + (int)(buffer[3] & 0xff);
 
                    System.out.println("Received!!!!");
                    
                    System.out.println("Version: " + this._version);
                    
                } 
                else{
                    this._error = "getVersion | Read | CRC Error";
                    this._connected = false;
                    return false;
                }
                this._connected = true;
            } 
            else{
                this._error = "getVersion | Read | Data Error";
                this._connected = false;
                return false;
            }   
        }
        catch (Exception ex){
            ex.printStackTrace();
            this._error = "getVersion | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            return false;
        }
        return true;
    }

    
    //1.setConfig
    public boolean setConfig(int[] min, int[] max, int in_sense, int out_sense){
        //--------------Send--------------
        try{
            byte []data = new byte[39];
            
            data[0] = (byte)0xff;     
            data[1] = (byte)this.SetConfig; //command id 
            
            //min max
            for(int i = 0; i < line_num; i++){
                data[2*i + 2] = (byte)(min[i] & 0xff);                          //[LO]
                data[2*i + 3] = (byte)((min[i] >> 8) & 0xff);                   //[HI]
                data[2*i + 18] = (byte)(max[i] & 0xff);                         //[LO]
                data[2*i + 19] = (byte)((max[i] >> 8) & 0xff);                  //[HI]
            }
            
            //sensitive
            data[34] = (byte)(in_sense & 0xff);         //[LO]
            data[35] = (byte)((in_sense >> 8) & 0xff);  //[HI]
            
            data[36] = (byte)(out_sense & 0xff);        //[LO]
            data[37] = (byte)((out_sense >> 8) & 0xff); //[HI]
            
            //crc8
            int crc8 = alisa.CRC.crc8(data, 0, (line_num * 4) + 6);
            data[38] = (byte)(crc8); 
            this._out.write(data);
            System.out.println("Sent SetConfig Command");
        } 
        catch (Exception ex){           
            this._error = "sendData | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            return false;
        }

        //---------------Sleep----------------
        try{  Thread.sleep(this.interval );  } 
        catch (InterruptedException ex){ return false;  }
        //------------------------------------
        
        //--------------Receive--------------- 
        try{
            byte[] buffer = new byte[1024];
            int length = this._in.read(buffer);
            System.out.println(length);
            
            while(length <= 2){
                Thread.sleep(200);
                length = this._in.read(buffer);
            }
            System.out.println("Length = " + length);
            
            if (length >= 4 && buffer[0] == (byte)0xff  && buffer[1] == this.SetConfig){ // slave_id & function code                            
                if (alisa.CRC.crc8(buffer, 0, 4) == 0){ // check CRC8
                   System.out.println("Received!!!!");
//                   for (int i=0; i<length; i++){
//                       System.out.print((buffer[i] & 0xFF) + " ");
//                   }
//                   System.out.println();
                    if(buffer[2] != 0){
                        this._error = "sendData | Read | Something Wrong at MCU side";
                        this._connected = false;
                        return false;
                    }
                } 
                else{
                    this._error = "sendData | Read | CRC Error";
                    this._connected = false;
                    return false;
                }
            } 
            else{
                this._error = "sendData | Read | Data Error";
                this._connected = false;
                return false;
            }
            
            this._connected = true;
        }
        catch (Exception ex){
            this._error = "sendData | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            return false;
        }
        return true;
    }

    
    //2.getData
    public boolean getConfig(){
        //--------------Send--------------
        try{
            byte []data = new byte[3]; 
            data[0] = (byte)0xff;     
            data[1] = (byte)this.GetConfig; //command id 
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 
            this._out.write(data);
            System.out.println("Sent");
        } 
        catch (Exception ex){
            this._error = "getData | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            
            return false;
        }
        
        //---------------Sleep----------------
        try{  Thread.sleep(this.interval);  } 
        catch (InterruptedException ex){ return false;  }
        //------------------------------------
        
        //--------------Receive--------------- 
        try{
            byte[] buffer = new byte[1024];
            int length = this._in.read(buffer);
                     
            System.out.println(length);
           
            if (length >= 39 && buffer[0] == (byte)0xff  && buffer[1] == this.GetConfig){ // slave_id & function code                         
                if (alisa.CRC.crc8(buffer, 0, 39) == 0){ // check CRC8
                   System.out.println("Received!!!!");
                   for (int i=0; i<line_num; i++){
                       _min[i] = (int)(buffer[2*i + 2] & 0xff) + (int)((buffer[2*i + 3] & 0xff) << 8);
                       _max[i] = (int)(buffer[2*i + 18] & 0xff) + (int)((buffer[2*i + 19] & 0xff) << 8);
                   }
                   this._in_sensitivity = (int)(buffer[34] & 0xff) + (int)((buffer[35] & 0xff) << 8);
                   this._out_sensitivity = (int)(buffer[36] & 0xff) + (int)((buffer[37] & 0xff) << 8);
                   
                   System.out.println();
                } 
                else{
                    this._error = "GetConfig | Read | CRC Error";
                    this._connected = false;
                    return false;
                }
                this._connected = true;
            } 
            else{
                this._error = "GetConfig | Read | Data Error";
                this._connected = false;
                return false;
            }   
        }
        catch (Exception ex){
            this._error = "GetConfig | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            return false;
        }
        return true;
    }
    
    public int getValid(){return this._valid;}
    private int _valid = -1;
    
//    public int getDetected(){return this._detected;}
//    private int _detected = -1;
       
//    public synchronized boolean setMinMax(byte[] min, byte[] max){
//        try{
//            if(min == null)         
//                System.arraycopy(max, 0, this.Max, 0, max.length);
//            else if(max == null)
//                System.arraycopy(min, 0, this.Min, 0, min.length);
//            else{
//                System.arraycopy(max, 0, this.Max, 0, max.length);
//                System.arraycopy(min, 0, this.Min, 0, min.length);
//            }
//        }catch(Exception ex){
//            this._error = "getDistance | Read | Exception Error | " + ex.getMessage();
//            return false;
//        }
//        return true;
//    }    
    public int[] getMin(){
        return this._min;
    }    
    public int[] getMax(){
        return this._max;
    }  
    public int[] getDistance(){
        return this._distance;
    } 
    
    //3.getDistance (new)
    public synchronized boolean getDetection(){              
        //--------------Send--------------
        try{
            byte[] data = new byte[3];
            data[0] = (byte)0xff;
            data[1] = (byte) this.GetDetection;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 

            this._out.write(data);
            System.out.println("Sent");       
        } 
        catch (Exception ex){
            this._error = "getDistance | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            return false;
        }
        
        //---------------Sleep----------------
        try{  Thread.sleep(this.interval);  }
        catch (InterruptedException  ex ){ return false; }
        //------------------------------------
        
        //--------------Receive---------------    
        int[] result = new int[line_num + 1];
        try{
            byte[] buffer = new byte[1024];
            
            int length = this._in.read(buffer);
                     
            System.out.println(length);
           
            if (length >= 37 && buffer[0] == (byte)0xff  && buffer[1] == this.GetDetection ){ // slave_id & function code  
                if (alisa.CRC.crc8(buffer, 0, 37) == 0){ // check CRC8
                    
                    System.out.println("Received!!!!");
                    try{
                        //distance 
                        for (int i = 0; i < 8; i++){
                            int a = 0;
                            try{
                                a = ((int)(buffer[2*i + 2] & 0xff) + (int)((buffer[2*i + 3] & 0xff) << 8));
                                this._distance[i] = a;
                                
                                //find new max_distance
                                if(this._distance[i] > this.max_distance[i])
                                    this.max_distance[i] = this._distance[i];
                                //---------------------    
                            }catch(Exception e){
//                                e.printStackTrace();
                                this._distance[i] = 0;
                            }
                            System.out.print( a + " " );
                        }    
                        
                        //connected
                        int connected = (int)(buffer[34] & 0xff);
                        if(connected == 1)
                            this._leddar_connected = true;
                        else 
                            this._leddar_connected = false;
                        
                        //detected
                        int detected = (int)(buffer[35] & 0xff);
                        if(detected == 1)
                            this._detected = true;
                        else
                            this._detected = false;
                        
                        System.out.println();
                    }catch(Exception e){
                        System.out.println("Error: " + e.getMessage());
                   }
                } 
                else{
                    this._error = "getDistance | Read | CRC Error";
                    this._connected = false;
                    return false;
                }
                this._connected = true;
            } 
            else{
                this._error = "getDistance | Read | Data Error";
                this._connected = false;
                return false;
            }   
        }
        catch (Exception ex){
            this._error = "getDistance | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            return false;
        }
        return true;
    } 
    public boolean getSystemStatus(){
        //--------------Send--------------
        try{
            byte[] data = new byte[3];
            data[0] = (byte)0xff;
            data[1] = (byte) this.GetSystemStatus;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 

            this._out.write(data);
            System.out.println("Sent");       
        } 
        catch (Exception ex){
            this._error = "getDistance | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            return false;
        }
        
        //---------------Sleep----------------
        try{  Thread.sleep(this.interval);  }
        catch (InterruptedException  ex ){ return false; }
        //------------------------------------
        
        //--------------Receive---------------    
        
        try{
            byte[] buffer = new byte[1024];
            
            int length = this._in.read(buffer);
                     
            System.out.println(length);
           
            if (length >= 9 && buffer[0] == (byte)0xff  && buffer[1] == this.GetSystemStatus ){ // slave_id & function code  
                if (alisa.CRC.crc8(buffer, 0, 9) == 0){ // check CRC8
                    
                    System.out.println("Received!!!!");
                    try{
                        int time = 0;
                        int watchdog = (int)(buffer[6] & 0xff) + (int)((buffer[7] & 0xff) << 8);
                        
                        
                    }catch(Exception e){
                        System.out.println("Error: " + e.getMessage());
                   }
                } 
                else{
                    this._error = "GetSystemStatus | Read | CRC Error";
                    this._connected = false;
                    return false;
                }
                this._connected = true;
            } 
            else{
                this._error = "GetSystemStatus | Read | Data Error";
                this._connected = false;
                return false;
            }   
        }
        catch (Exception ex){
            this._error = "GetSystemStatus | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            return false;
        }
        return true;
    }
    
    //----------------------------------------------------------
    //new
    public int[] auto_min = new int[8];
    public int[] auto_max = new int[8];
    int safe_zone_length = 30; // (cm.)
    public boolean auto_assign_minmax(int[] distance){
        for (int i = 0; i < max_distance.length; i++) {
//            int min = (int)((Min[2*i] << 8) & 0xff) + (int)(Min[2*i + 1] & 0xff);  
//            int max = (int)((Max[2*i] << 8) & 0xff) + (int)(Max[2*i + 1] & 0xff);  
            if(distance[i] > auto_max[i] && distance[i] < this.max_distance[i] - this.safe_zone_length){
                auto_max[i] = distance[i];
            }
            if(auto_min[i] == 0 && distance[i] < max_distance[i] - 20){
                if(distance[i] <= auto_max[i])
                    auto_min[i] = distance[i];
            }else if(distance[i] < auto_min[i] && distance[i] > 0 + 10){
//                if(distance[i] <= max[i])
                auto_min[i] = distance[i];
            }
        }
//        setMinMax(min, max);
//        boolean send = this.sendData();
////        while(!send){
//        if(!send){
//            System.out.println("Set Min Max Fail!");
////            this.sendData();
//        }
//        System.out.println("Set Min Max Success");
        return true;
    }
    
    public void resetMaxDistance(){
        for (int i = 0; i < this.max_distance.length; i++) {
            this.max_distance[i] = 0;
//            this.min_distance[i] = 0;
        }
    }
    
}

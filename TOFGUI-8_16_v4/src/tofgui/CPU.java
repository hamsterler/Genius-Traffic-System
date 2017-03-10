package tofgui;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
public class CPU 
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
    public int interval = 500;
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
    private int _watchdog = 0;
    private long _uptime = 0;
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
    public int[] getMin(){ return this._min; }    
    public int[] getMax(){ return this._max; }  
    public int[] getDistance(){ return this._distance; } 
    public int getWatchDog(){ return this._watchdog; }
    public long getUpTime(){ return this._uptime; }
    public boolean[] getLineDetected(){ return this._line_detected; }
    //----------------------------------------------------------------
    
    //-------------Constructor--------------
    public CPU(){
        this.line_num = 8;
    }
    //--------------------------------------
    
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

    //------------------------------------------reconnect------------------------------------------
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

  
    // -------------------------- Read Config -----------------------------         
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
    
    // ---------------------------- 0.GetVersion ---------------------------- 
    public boolean getVersion(){
        //--------------Send--------------
        try{
            //wait if serial is busy
            while(_serial_status != 0){
                System.out.print("");
            }
            //set serial status    
            _serial_status = this.GetVersion;
            
            byte[] data = new byte[4];
            data[0] = (byte)0xff;
            data[1] = (byte)this.GetVersion;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8);
            this._out.write(data);
            System.out.println("Sent GetVersion");
        } 
        catch (Exception ex){
            ex.printStackTrace();
            this._error = "getVersion | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return false;
        }

        //---------------Sleep----------------
        try{  Thread.sleep(this.interval);  } 
        catch (InterruptedException ex){ _serial_status = 0; return false;  }
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
                    _serial_status = 0;
                    return false;
                }
                this._connected = true;
            } 
            else{
                this._error = "getVersion | Read | Data Error";
                this._connected = false;
                _serial_status = 0;
                return false;
            }   
        }
        catch (Exception ex){
            ex.printStackTrace();
            this._error = "getVersion | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return false;
        }
        _serial_status = 0;
        return true;
    }

    
    // ---------------------------- 1.SetConfig -------------------------------  
    public boolean setConfig(int[] min, int[] max, int in_sense, int out_sense){
        //--------------Send--------------
        try{
            //wait if serial is busy
            while(_serial_status != 0){
                System.out.print("");
            }
            //set serial status    
            _serial_status = this.SetConfig;
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
//            in_sense = in_sense * 1000;
            data[34] = (byte)(in_sense & 0xff);         //[LO]
            data[35] = (byte)((in_sense >> 8) & 0xff);  //[HI]
//            out_sense = out_sense * 1000;
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
            _serial_status = 0;
            return false;
        }

        //---------------Sleep----------------
        try{  Thread.sleep(this.interval );  } 
        catch (InterruptedException ex){ _serial_status = 0; return false;  }
        //------------------------------------
        
        //--------------Receive--------------- 
        try{
            byte[] buffer = new byte[1024];
            int length = 0;
           
            for (int i = 0; i < 5; i++) {
                length = this._in.read(buffer);
                if(length >= 2)
                    break;
                Thread.sleep(100);
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
                        _serial_status = 0;
                        return false;
                    }
                } 
                else{
                    this._error = "sendData | Read | CRC Error";
                    this._connected = false;
                    _serial_status = 0;
                    return false;
                }
            } 
            else{
                this._error = "sendData | Read | Data Error";
                this._connected = false;
                _serial_status = 0;
                return false;
            }
            
            this._connected = true;
        }
        catch (Exception ex){
            this._error = "sendData | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return false;
        }
        _serial_status = 0;
        return true;
    }

    
    // ---------------------------- 2.GetConfig -------------------------------
    public boolean getConfig(){
        //--------------Send--------------
        try{
            //wait if serial is busy
            while(_serial_status != 0){
                System.out.print("");
            }
            _serial_status = this.GetConfig;
            
            byte []data = new byte[3]; 
            data[0] = (byte)0xff;     
            data[1] = (byte)this.GetConfig; //command id 
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 
            this._out.write(data);
            System.out.println("Sent GetConfig");
        } 
        catch (Exception ex){
            this._error = "getData | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            
            _serial_status = 0;
            return false;
        }
        
        //---------------Sleep----------------
        try{  Thread.sleep(this.interval);  } 
        catch (InterruptedException ex){ _serial_status = 0; return false;  }
        //------------------------------------
        
        //--------------Receive--------------- 
        try{
            byte[] buffer = new byte[1024];
            int length = 0;
                    
            for (int i = 0; i < 5; i++) {
                length = this._in.read(buffer);
                if(length >= 2)
                    break;
                Thread.sleep(100);
            }
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
                    _serial_status = 0;
                    return false;
                }
                this._connected = true;
            } 
            else{
                this._error = "GetConfig | Read | Data Error";
                this._connected = false;
                _serial_status = 0;
                return false;
            }   
        }
        catch (Exception ex){
            this._error = "GetConfig | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return false;
        }
        _serial_status = 0;
        return true;
    }

    // ---------------------------- 3.GetDetection -------------------------------
    private boolean[] _line_detected = new boolean[8];
    public synchronized boolean getDetection(){              
        //--------------Send--------------
        try{
            //wait if serial is busy
            while(_serial_status != 0){
               System.out.print("");
            }       
            //set status
            _serial_status = this.GetDetection;
        
            byte[] data = new byte[3];
            data[0] = (byte)0xff;
            data[1] = (byte) this.GetDetection;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 

            this._out.write(data);
            System.out.println("Sent GetDetection");       
        } 
        catch (Exception ex){
            this._error = "getDistance | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return false;
        }
        
        //---------------Sleep----------------
        try{  Thread.sleep(this.interval);  }
        catch (InterruptedException  ex ){ _serial_status = 0; return false; }
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
                        
                        //distance 
                        for (int i = 0; i < 8; i++){
                            int a = 0;
                            try{
                                a = ((int)(buffer[2*i + 2] & 0xff) + (int)((buffer[2*i + 3] & 0xff) << 8));
                                this._distance[i] = a;
                                
                                //find new max_distance
                                if(this._distance[i] > this.max_distance[i])
                                    this.max_distance[i] = this._distance[i];
                                
                                //check for each line detection
                                if(this._distance[i] >= this._min[i] && this._distance[i] <= this._max[i] && this._detected)
                                    this._line_detected[i] = true;
                                else if(!this._detected)
                                    this._line_detected[i] = false;
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
                        _serial_status = 0;
                   }
                } 
                else{
                    this._error = "getDistance | Read | CRC Error";
                    this._connected = false;
                    _serial_status = 0;
                    return false;
                }
                this._connected = true;
            } 
            else{
                this._error = "getDistance | Read | Data Error";
                this._connected = false;
                _serial_status = 0;
                return false;
            }   
        }
        catch (Exception ex){
            this._error = "getDistance | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return false;
        }
        _serial_status = 0;
        return true;
    } 
    
    // ---------------------------- 4.GetSystemStatus -------------------------------
    public boolean getSystemStatus(){
        //--------------Send--------------
        try{
            //wait if serial is busy
            while(_serial_status != 0){
               System.out.print("");
            }       
            //set status
            _serial_status = this.GetSystemStatus;
            
            byte[] data = new byte[3];
            data[0] = (byte)0xff;
            data[1] = (byte) this.GetSystemStatus;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 

            this._out.write(data);
            System.out.println("Sent GetSystem Status");       
        } 
        catch (Exception ex){
            this._error = "getDistance | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return false;
        }
        
        //---------------Sleep----------------
        try{  Thread.sleep(this.interval);  }
        catch (InterruptedException  ex ){ _serial_status = 0; return false; }
        //------------------------------------
        
        //--------------Receive---------------    
        
        try{
            byte[] buffer = new byte[1024];
            
            int length = 0;
            for (int i = 0; i < 5; i++) {
                length = this._in.read(buffer);
                if(length >= 2)
                    break;
                Thread.sleep(100);
            }         
            System.out.println(length);
           
            if (length >= 9 && buffer[0] == (byte)0xff  && buffer[1] == this.GetSystemStatus ){ // slave_id & function code  
                if (alisa.CRC.crc8(buffer, 0, 9) == 0){ // check CRC8
                    
                    System.out.println("Received!!!!");
                    try{
                        this._uptime = (long)((int)(buffer[2] & 0xff) + (int)((buffer[3] & 0xff) << 8)
                                 + (int)((buffer[4] & 0xff) << 16) + (int)((buffer[5] & 0xff) << 24));
                        this._watchdog = (int)(buffer[6] & 0xff) + (int)((buffer[7] & 0xff) << 8);
                        
                        
                    }catch(Exception e){
                        System.out.println("Error: " + e.getMessage());
                   }
                } 
                else{
                    this._error = "GetSystemStatus | Read | CRC Error";
                    this._connected = false;
                    _serial_status = 0;
                    return false;
                }
                this._connected = true;
            } 
            else{
                this._error = "GetSystemStatus | Read | Data Error";
                this._connected = false;
                _serial_status = 0;
                return false;
            }   
        }
        catch (Exception ex){
            this._error = "GetSystemStatus | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return false;
        }
        _serial_status = 0;
        return true;
    }
    
    // ---------------------------- auto assign minmax -------------------------------
    public int[] auto_min = new int[8];
    public int[] auto_max = new int[8];
    int safe_zone_length = 30; // (cm.)
    public int[] reference_distance = new int[8]; 
    public boolean auto_assign_minmax(int[] distance){
//        auto_min[0] = 100;
//        auto_max[0] = 400;
//        auto_min[3] = 100;
//        auto_max[3] = 300;
//        auto_min[5] = 200;
//        auto_max[5] = 400;
//        auto_min[6] = 150;
//        auto_max[6] = 400;
//        auto_min[7] = 300;
//        auto_max[7] = 300;
        //check if all ref distance == 0
        boolean all_zero = true;
        for (int i = 0; i < 8; i++) {
            if(reference_distance[i] > 0)
                all_zero = false;
        }
        if(all_zero)
            this.reference_distance = this.max_distance;
        
        
        System.out.print("Safe Zone:  ");
        for (int i = 0; i < reference_distance.length; i++) {
            //safe_zone = 10% of Ref. distance
            safe_zone_length = (int)((double)this.reference_distance[i] /(double)10);
            System.out.print(safe_zone_length + "     ");
            if(distance[i] > auto_max[i] && distance[i] < this.reference_distance[i] - this.safe_zone_length){
                auto_max[i] = distance[i];
            }
            if(auto_min[i] == 0 && distance[i] < reference_distance[i] - 20){
                if(distance[i] <= auto_max[i])
                    auto_min[i] = distance[i];
            }else if(distance[i] < auto_min[i] && distance[i] > 0 + 10){
//                if(distance[i] <= max[i])
                auto_min[i] = distance[i];
            }
        }
        System.out.println("");
        return true;
    }
    
    public int[] auto_line_min = new int[8];
    public int[] auto_line_max = new int[8];
    public void drawAutoLine2(){
//        auto_line_min[0] = 100;
//        auto_line_max[0] = 300;
//        auto_line_min[1] = 100;
//        auto_line_max[1] = 300;
//        auto_line_min[2] = 100;
//        auto_line_max[2] = 300;
//        auto_line_min[3] = 100;
//        auto_line_max[3] = 300;
//        auto_line_min[4] = 100;
//        auto_line_max[4] = 300;
//        auto_line_min[5] = 100;
//        auto_line_max[5] = 300;
        auto_line_min = new int[8];
        auto_line_max = new int[8];
        int pre = -1;
        int status = 0;
        for (int i = 0; i < 8; i++) {
            if(auto_min[i] == 0 && auto_max[i] == 0){
                
                
            }else {
                auto_line_min[i] = auto_min[i];
                auto_line_max[i] = auto_max[i];
                if(status == 0){
                    pre = i;
                    status = 1;
                }else if(status == 1){
                    if(i - pre > 1){
                        int bas_min = 0;
                        bas_min = auto_line_min[pre];
                        
                        int bas_max = 0;
                        bas_max = auto_line_max[pre];
                        
                        int num = i - pre;
                        int gab_min = (int)((double)(auto_line_min[i] - auto_line_min[pre]) / (double)num);
                        int gab_max = (int)((double)(auto_line_max[i] - auto_line_max[pre]) / (double)num);
                        
                        for (int j = 1; j < num; j++) {
                            auto_line_min[pre + j] = bas_min + gab_min * j;
                            auto_line_max[pre + j] = bas_max + gab_max * j;
                        }
                    }
                    pre = i;
                }
            }
            
        }
        System.out.print("AutoDraw Min: " );
        for (int i = 0; i < 8; i++) {
            System.out.print( auto_line_min[i] + "    ");
        }
        System.out.print("\nAutoDraw Max: " );
        for (int i = 0; i < 8; i++) {
            System.out.print( auto_line_max[i] + "    ");
        }
        System.out.println("");
    }
    
    public void drawDetectedArea(){
        auto_line_min = new int[8];
        auto_line_max = new int[8];
        String pre_type = "";
        int pre = -1;
        int status = 0;
        int left_edge = -1;
        int right_edge = -1;
        for (int i = 0; i < 8; i++) {
            if(auto_min[i] == 0 && auto_max[i] == 0){
                //do nothing
            }else{
                auto_line_min[i] = auto_min[i];
                auto_line_max[i] = auto_max[i];
                if(pre == -1){
                    left_edge = i;
                    right_edge = i;
                }else if(auto_min[i] != auto_max[i]){
                    int num_min = i - left_edge;
                    int num_max = i - right_edge;
                    int gab_min = (int)((double)(auto_min[i] - auto_min[left_edge]) / (double)num_min);
                    int gab_max = (int)((double)(auto_max[i] - auto_max[right_edge]) / (double)num_max);

                    for (int j = 1; j < num_min; j++) {
                        auto_line_min[left_edge + j] = auto_min[left_edge] + gab_min * j;
                    }
                    for (int j = 1; j < num_max; j++) {
                        auto_line_max[right_edge + j] = auto_max[right_edge] + gab_max * j;
                    }
                    left_edge = i;
                    right_edge = i;
                }else{ //min == max
                    int num_min = i - left_edge;
                    int num_max = i - right_edge;
                    int gab_min = (int)((double)(auto_min[i] - auto_min[left_edge]) / (double)num_min);
                    int gab_max = (int)((double)(auto_max[i] - auto_max[right_edge]) / (double)num_max);

                    for (int j = 1; j < num_min; j++) {
                        auto_line_min[left_edge + j] = auto_min[left_edge] + gab_min * j;
                    }
                    for (int j = 1; j < num_max; j++) {
                        auto_line_max[right_edge + j] = auto_max[right_edge] + gab_max * j;
                    }
                   
                    if(auto_min[left_edge] == auto_max[right_edge]){
                        if(auto_max[i] < auto_min[left_edge])
                            left_edge = i;
                        else
                            right_edge = i;
                    }else{
                        if(Math.abs(auto_max[i] - auto_min[left_edge]) <  Math.abs(auto_max[i] - auto_min[right_edge])){
                            left_edge = i;
                        }else{
                            right_edge = i;
                        }
                    }
                }
                pre = i;
            }
            if(left_edge >= 0)
                System.out.println("Left Edge" + i + " = " + auto_min[left_edge]);
            if(right_edge >= 0)
                System.out.println("Right Edge" + i + " = " + auto_max[right_edge]);
        }
        
        for (int i = 0; i < 8; i++) {
            if(auto_line_min[i] !=  0)
                auto_line_min[i] = auto_line_min[i] - 10;
            
            if(auto_line_max[i] !=  0)
                auto_line_max[i] = auto_line_max[i] + 10;        
        }
        
        System.out.print("AutoDraw Min: " );
        for (int i = 0; i < 8; i++) {
            System.out.print( auto_line_min[7 - i] + "    ");
        }
        System.out.print("\nAutoDraw Max: " );
        for (int i = 0; i < 8; i++) {
            System.out.print( auto_line_max[7 - i] + "    ");
        }
        System.out.println("");
    }
    
    public void resetAutoMinMax(){
        for (int i = 0; i < 8; i++) {
            this.auto_max[i] = 0;
            this.auto_min[i] = 0;
            this.auto_line_min[i] = 0;
            this.auto_line_max[i] = 0;
        }
    }
    
    public void setRefDistance(){
        for (int i = 0; i < reference_distance.length; i++) {
            this.reference_distance[i] = this._distance[i];           
        }
    }
    
    public void resetMaxDistance(){
        for (int i = 0; i < this.max_distance.length; i++) {
            this.max_distance[i] = 0;
        }
    }
    
}

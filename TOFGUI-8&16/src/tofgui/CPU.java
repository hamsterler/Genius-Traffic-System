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
public class CPU 
{
    public static final int GetVersion = 0;
    public static final int SendData = 1;
    public static final int GetData = 2;
    public static final int GetDistance = 3;
    public int distance = 0;
    private int board_type = 3;
    public int line_num;  //<< only 8 or 16
    private String version = "";
    public int interval = 300;
    private boolean _serial_connect;
    private boolean _serial_idle = true;
    public boolean isSerialConnected(){
        return this._serial_connect;
    }

    private int _serial_status = 0; //0 = idle, 1 = sending Data , 2 =getting Data, 3 = geting Distance
    public int getStatus(){
        return _serial_status;
    }
    public boolean setPort(String port){
        this._port = port;
        return true;
    }
    
    public CPU(){
        this.line_num = 16;
    }
    
    public void reconnect(){
        this._serial_connect = _reconnect();
    }
    
    
    public CPU(String port)
    {
        this.line_num = 16;
        this._port = port;
        _serial_connect = this._reconnect();
    }

    public void dispose() 
    {
        // disconnect
        if (this._comm_port != null) 
        {
            this._comm_port.close();
        }
    }
    public String getVersionString(){ return this.version; }
    public String getError() {  return this._error;  }
    private String _error = "";

    private InputStream _in = null;
    private OutputStream _out = null;

    public String getPort(){ return this._port; }
    private String _port = "";

    private CommPort _comm_port = null;

    public boolean isConnected() { return this._connected; }
    private boolean _connected = false;

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

  
    // ---------------- 0. GetVersion ------------------
    public int getMajorVersion() {  return this._major_cpu_version;  }
    private int _major_cpu_version = -1;
    
    public int getMinorVersion(){ return this._minor_cpu_version; }
    private int _minor_cpu_version = -1;
    
    public int getDevice(){ return this._device;}
    private int _device = -1;    //<<<< if _device = 3 >> 16 line, 4 >> 8line
            
    public byte [] Min = new byte[32];
    public byte [] Max = new byte[32];
    
    
    public boolean readConfig(){
        try{
            String path = "config.txt";
            List<String> lines = Files.readAllLines(Paths.get(path),
                    Charset.defaultCharset());
            
            String[] value_min = lines.get(0).split(",");
            String[] value_max = lines.get(1).split(",");
            
            for(int i=0; i < Min.length/2; i++){
                Min[2*i] = (byte)(Integer.parseInt(value_min[i]) >> 8);
                Min[2*i + 1] = (byte)(Integer.parseInt(value_min[i]));
                
                Max[2*i] = (byte)(Integer.parseInt(value_max[i]) >> 8);
                Max[2*i + 1] = (byte)(Integer.parseInt(value_max[i]));
            }
            System.out.print("\nMin = ");
            for(int i=0; i < 16; i++)
                System.out.print((int)Min[2*i] + " " + (int)Min[2*i + 1] + "  ");
            System.out.print("\nMax = ");
            for(int i=0; i < 16; i++)
                System.out.print((int)(Max[2*i]&0xff) + " " + (int)(Max[2*i + 1]&0xff) + "  ");           
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
            //wait if serial is busy
            while(_serial_status != 0){
                System.out.print("serial busy");
            }
            //set serial status    
            _serial_status = 1;
            
            byte[] data = new byte[4];
            data[0] = (byte)'{';
            data[1] = (byte)CPU.GetVersion;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 
            data[3] = (byte)'}';
            this._out.write(data);
            System.out.println("Sent");
        } 
        catch (Exception ex){
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
            if (length >= 7 && buffer[0] == (byte)'{'  && buffer[1] == CPU.GetVersion && buffer[6] == (byte)'}'){ // slave_id & function code                            
                if (alisa.CRC.crc8(buffer, 0, 6) == 0){ // check CRC8
                    this._device = buffer[2];
                    
                    //check for sensor type(8 or 16 line)
                    if(this._device == 3)
                        this.line_num = 16;
                    else
                        this.line_num = 8;
                    
                    this._major_cpu_version = buffer[3];
                    this._minor_cpu_version = buffer[4];
                    
                    System.out.println("Received!!!!");
                    for (int i=0; i<length; i++){
                        System.out.print((buffer[i] & 0xFF) + " ");
                    }
                    System.out.println();
                    this.version = "Device Number: " + buffer[2] + "\nMajor Version: " + (buffer[3] & 0xff) + ".0   Minor Version: " + buffer[4] + ".0\n";
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

    
    //1.sendData
    public boolean sendData(){
        //--------------Send--------------
        try{
            //wait if serial is busy
            while(_serial_status != 0){
                System.out.print("serial busy");
            }
            //set serial status    
            _serial_status = 1;
            byte []data = new byte[(line_num * 4) +4];
            
            data[0] = (byte)'{';     
            data[1] = CPU.SendData; //command id 
            for(int i = 0; i < line_num; i++){
                data[2*i + 2] = Min[2*i];
                data[2*i + 3] = Min[2*i + 1];
                data[2*i + (line_num*2 + 2)] = Max[2*i];
                data[2*i + (line_num*2 + 3)] = Max[2*i + 1];
            }

            int crc8 = alisa.CRC.crc8(data, 0, (line_num * 4) + 2);
            data[(line_num * 4) + 2] = (byte)(crc8); 
            data[(line_num * 4) + 3] = (byte)'}';
            this._out.write(data);
            System.out.println("Sent");
        } 
        catch (Exception ex){           
            this._error = "sendData | Write | Exception Error | " + ex.getMessage();
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
            
            if (length >= 5 && buffer[0] == (byte)'{'  && buffer[1] == CPU.SendData && buffer[4] == (byte)'}'){ // slave_id & function code                            
                if (alisa.CRC.crc8(buffer, 0, 4) == 0){ // check CRC8
                   System.out.println("Received!!!!");
                   for (int i=0; i<length; i++){
                       System.out.print((buffer[i] & 0xFF) + " ");
                   }
                   System.out.println();
                } 
                else{
                    this._error = "sendData | Read | CRC Error";
                    this._connected = false;
                    _serial_status = 0;
                    return false;
                }
                this._connected = true;
            } 
            else{
                this._error = "sendData | Read | Data Error";
                this._connected = false;
                _serial_status = 0;
                return false;
            }               
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

    
    //2.getData
    public boolean getData(){
        //--------------Send--------------
        try{
            //wait if serial is busy
            while(_serial_status != 0){
                System.out.print("serial busy");
            }
            
            byte []data = new byte[4]; 
            data[0] = (byte)'{';     
            data[1] = (byte)CPU.GetData; //command id 
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 
            data[3] = (byte)'}';
            this._out.write(data);
            System.out.println("Sent");
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
            int length = this._in.read(buffer);
                     
            System.out.println(length);
           
            if (length >= line_num * 4 + 4 && buffer[0] == (byte)'{'  && buffer[1] == CPU.GetData && buffer[line_num * 4 + 3] == (byte)'}'){ // slave_id & function code                         
                if (alisa.CRC.crc8(buffer, 0, line_num * 4 + 3) == 0){ // check CRC8
                   System.out.println("Received!!!!");
                   for (int i=0; i<line_num; i++){
                       Min[2*i] = buffer[2*i + 2];
                       Min[2*i + 1] = buffer[2*i + 3];
                       Max[2*i] = buffer[2*i + (line_num*2 + 2)];
                       Max[2*i + 1] = buffer[2*i + (line_num*2 + 3)]; 
                   }
                   System.out.println();
                } 
                else{
                    this._error = "getData | Read | CRC Error";
                    this._connected = false;
                    _serial_status = 0;
                    return false;
                }
                this._connected = true;
            } 
            else{
                this._error = "getData | Read | Data Error";
                this._connected = false;
                _serial_status = 0;
                return false;
            }   
        }
        catch (Exception ex){
            this._error = "getData | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return false;
        }
        _serial_status = 0;
        return true;
    }
    
    public int getValid(){return this._valid;}
    private int _valid = -1;
    
    public int getDetected(){return this._detected;}
    private int _detected = -1;
       
    public synchronized boolean setMinMax(byte[] min, byte[] max){
        try{
            if(min == null)         
                System.arraycopy(max, 0, this.Max, 0, max.length);
            else if(max == null)
                System.arraycopy(min, 0, this.Min, 0, min.length);
            else{
                System.arraycopy(max, 0, this.Max, 0, max.length);
                System.arraycopy(min, 0, this.Min, 0, min.length);
            }
        }catch(Exception ex){
            this._error = "getDistance | Read | Exception Error | " + ex.getMessage();
            return false;
        }
        return true;
    }    
    public int[] getMin(){
        int[] min = new int[this.line_num];
        for(int i = 0;i < min.length ; i++)
            min[i] = (int)((Min[2*i] & 0xff) << 8) + (int)(Min[2*i + 1] & 0xff);
        return min;
    }    
    public int[] getMax(){
        int[] max = new int[this.line_num];
        for(int i = 0;i < max.length; i++)
            max[i] = (int)((Max[2*i] & 0xff) << 8) + (int)(Max[2*i + 1] & 0xff);
        return max;
    }  
    
    //3.getDistance (new)
    public synchronized int[] getDistanceInt(){              
        //--------------Send--------------
        try{
            //wait if serial is busy
            while(_serial_status != 0){
               System.out.print("serial busy");
            }       
            //set status
            _serial_status = 3;
        
            byte[] data = new byte[4];
            data[0] = (byte) '{';
            data[1] = (byte) CPU.GetDistance;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 
            data[3] = (byte)'}';

            this._out.write(data);
            System.out.println("Sent");       
        } 
        catch (Exception ex){
            this._error = "getDistance | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return null;
        }
        
        //---------------Sleep----------------
        try{  Thread.sleep(this.interval);  }
        catch (InterruptedException  ex ){ _serial_status = 0; return null; }
        //------------------------------------
        
        //--------------Receive---------------    
        int[] result = new int[line_num + 1];
        try{
            byte[] buffer = new byte[1024];
            
            int length = this._in.read(buffer);
                     
            System.out.println(length);
           
            if (length >= line_num * 2 + 6 && buffer[0] == (byte)'{'  && buffer[1] == CPU.GetDistance && buffer[line_num * 2 + 5] == (byte)'}'){ // slave_id & function code                            
                if (alisa.CRC.crc8(buffer, 0, line_num * 2 + 5) == 0){ // check CRC8
                    this._valid = buffer[line_num * 2 + 2];
                    this._detected = buffer[line_num * 2 + 3];
                    System.out.println("Received!!!!");
                    try{
                        for (int i = 0; i < this.line_num; i++){
                            int a = 0;
                            try{
                                a = ((int)((buffer[2*i + 2] & 0xff) << 8) + (int)(buffer[2*i + 3] & 0xff));
                                result[i] = a;
                            }catch(Exception e){
                                result[i] = 0;
                            }
                            System.out.print( a + " " );
                        }    
                        result[line_num] = (int)(buffer[line_num * 2 + 3] & 0xff);
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
                    return null;
                }
                this._connected = true;
            } 
            else{
                this._error = "getDistance | Read | Data Error";
                this._connected = false;
                _serial_status = 0;
                return null;
            }   
        }
        catch (Exception ex){
            this._error = "getDistance | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            _serial_status = 0;
            return null;
        }
        _serial_status = 0;
        return result;
    } 
    //----------------------------------------------------------
}

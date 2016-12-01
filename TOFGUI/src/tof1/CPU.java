package tof1;

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
    
    //update
    private int _serial_status = 0; //0 = idle, 1 = sending Data , 2 =getting Data, 3 = geting Distance
    public int getStatus(){
        return _serial_status;
    }
    
    //
    public CPU(String port) 
    {
        this._port = port;
        this._reconnect();
    }

    public void dispose() 
    {
        // disconnect
        if (this._comm_port != null) 
        {
            this._comm_port.close();
        }
    }
    
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
    private boolean _reconnect() 
    {
        try 
        {
            // try to disconnect
            if (this._comm_port != null) 
            {
                this._comm_port.close();
            }

            // connect com port
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this._port);
            if (portIdentifier.isCurrentlyOwned()) 
            {
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
                    return false;
                }
            }
        }catch (Exception ex) {ex.printStackTrace(); return false;}

        return true;
    }

  
    // ---------------- 0. GetVersion ------------------
    public int getMajorVersion() {  return this._major_cpu_version;  }
    private int _major_cpu_version = -1;
    
    public int getMinorVersion(){ return this._minor_cpu_version; }
    private int _minor_cpu_version = -1;
    
    public int getDevice(){ return this._device;}
    private int _device = -1;
            
    public byte [] Min = new byte[16];
    public byte [] Max = new byte[16];
    
    
    public boolean readConfig()
    {
        try
        {
            String path = "config.txt";
            List<String> lines = Files.readAllLines(Paths.get(path),
                    Charset.defaultCharset());
            
            String[] value_min = lines.get(0).split(",");
            String[] value_max = lines.get(1).split(",");
//            System.out.print("Max: ");
            for(int i=0; i<Min.length; i++)
            {
//                System.out.print(Integer.parseInt(value_max[i]) + " ");
                Min[i] = (byte)Integer.parseInt(value_min[i]);
                Max[i] = (byte)Integer.parseInt(value_max[i]);
//                Min[i] = Byte.parseByte(value_min[i]);
//                Max[i] = Byte.parseByte(value_max[i]);
            }
           // for(int i=0; i<Min.length; i++){System.out.println(Min[i]+","+ Max[i]);}  
        }
        catch (Exception ex) {}
        
        return true;
    }
    

     //0.getVersion
    public boolean getVersion()
    {
         //send
        try 
        {
            byte[] data = new byte[4];
            data[0] = (byte)'{';
            data[1] = (byte)CPU.GetVersion;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 
            data[3] = (byte)'}';

            this._out.write(data);
            System.out.println("Sent");
        } 
        catch (Exception ex) 
        {
            this._error = "getVersion | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            return false;
        }

        try{  Thread.sleep(1000);  } 
        catch (InterruptedException ex){  return false;  }
        
        //receive
        try 
        {
            byte[] buffer = new byte[1024];
            int length = this._in.read(buffer);
                     
            System.out.println(length);
           
           if (length >= 7 && buffer[0] == (byte)'{'  && buffer[1] == CPU.GetVersion && buffer[6] == (byte)'}') // slave_id & function code
            {                            
                if (alisa.CRC.crc8(buffer, 0, 6) == 0) // check CRC8
                {
                    this._device = buffer[2];
                    this._major_cpu_version = buffer[3];
                    this._minor_cpu_version = buffer[4];
                    
                    System.out.println("Received!!!!");
                    for (int i=0; i<length; i++)
                    {
                        System.out.print((buffer[i] & 0xFF) + " ");
                    }
                    System.out.println();
                } 
                else 
                {
                    this._error = "getVersion | Read | CRC Error";
                    this._connected = false;
                    return false;
                }
                this._connected = true;
            } 
            else 
            {
                this._error = "getVersion | Read | Data Error";
                this._connected = false;
                return false;
            }   
        }
        catch (Exception ex) 
        {
            this._error = "getVersion | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            return false;
        }
        
        return true;
    }

    
    //1.sendData
    public boolean sendData()
    {
    //wait
        while(_serial_status != 0){
            System.out.print("");
        }
         //send
        try 
        {
        //set serial status    
            _serial_status = 1;
            byte []data = new byte[36];
            
            data[0] = (byte)'{';     
            data[1] = CPU.SendData; //command id 
            for(int i=2; i<18; i++)
            {
                data[i] = Min[i-2];
            }

            for(int i=18; i<34; i++)
            {
                data[i] = Max[i-18];
            }

            int crc8 = alisa.CRC.crc8(data, 0, 34);
            data[34] = (byte)(crc8); 

            data[35] = (byte)'}';
            this._out.write(data);
            System.out.println("Sent");

        } 
        catch (Exception ex) 
        {           
            this._error = "sendData | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            
            _serial_status = 0;
            return false;
        }

        try{  Thread.sleep(1000);  } 
        catch (InterruptedException ex){  _serial_status = 0; return false;  }
    //wait
//        while(_serial_status != 1){}
        
        //receive
        try 
        {
            byte[] buffer = new byte[1024];
            int length = this._in.read(buffer);
                     
            System.out.println(length);
            
            if (length >= 5 && buffer[0] == (byte)'{'  && buffer[1] == CPU.SendData && buffer[4] == (byte)'}') // slave_id & function code
            {                            
                if (alisa.CRC.crc8(buffer, 0, 4) == 0) // check CRC8
                {
                   System.out.println("Received!!!!");
                   for (int i=0; i<length; i++)
                   {
                       System.out.print((buffer[i] & 0xFF) + " ");
                   }
                   System.out.println();
                } 
                else 
                {
                    this._error = "sendData | Read | CRC Error";
                    this._connected = false;
                //set    
                    _serial_status = 0;
                    return false;
                }
                this._connected = true;
            } 
            else 
            {
                this._error = "sendData | Read | Data Error";
                this._connected = false;
            //set
                _serial_status = 0;
                return false;
            }               
        }
        catch (Exception ex) 
        {
            this._error = "sendData | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
        //set
            _serial_status = 0;
            return false;
        }
    //set
        _serial_status = 0;
        return true;
    }
    
    
    //2.getData
    public boolean getData()
    {
        while(_serial_status != 0){}
         //send
        try 
        {
            byte []data = new byte[4];
                    
            data[0] = (byte)'{';     
            data[1] = (byte)CPU.GetData; //command id 
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 
            data[3] = (byte)'}';
            this._out.write(data);
            System.out.println("Sent");
        } 
        catch (Exception ex) 
        {
            this._error = "getData | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            
            _serial_status = 0;
            return false;
        }

        try{  Thread.sleep(1000);  } 
        catch (InterruptedException ex){ _serial_status = 0; return false;  }
        
        //receive
        try 
        {
            byte[] buffer = new byte[1024];
            int length = this._in.read(buffer);
                     
            System.out.println(length);
           
           if (length >= 36 && buffer[0] == (byte)'{'  && buffer[1] == CPU.GetData && buffer[35] == (byte)'}') // slave_id & function code
            {                            
                if (alisa.CRC.crc8(buffer, 0, 35) == 0) // check CRC8
                {
                   System.out.println("Received!!!!");
                   for (int i=0; i<length; i++)
                   {
                       System.out.print((buffer[i] & 0xFF) + " ");
                   }
                   System.out.println();
                } 
                else 
                {
                    this._error = "getData | Read | CRC Error";
                    this._connected = false;
                    _serial_status = 0;
                    return false;
                }
                this._connected = true;
            } 
            else 
            {
                this._error = "getData | Read | Data Error";
                this._connected = false;
                _serial_status = 0;
                return false;
            }   
        }
        catch (Exception ex) 
        {
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
    
     //3.getDistance
    public boolean getDistance()
    {
         //send
        try 
        {
            byte[] data = new byte[4];
            data[0] = (byte) '{';
            data[1] = (byte) CPU.GetDistance;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 
            data[3] = (byte)'}';

            this._out.write(data);
            System.out.println("Sent");
        } 
        catch (Exception ex) 
        {
            this._error = "getDistance | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
            return false;
        }
    
        try{  Thread.sleep(1000);  } 
        catch (InterruptedException ex){  return false;  }
        //receive
        try 
        {
            byte[] buffer = new byte[1024];
            int length = this._in.read(buffer);
                     
            System.out.println(length);
           
           if (length >= 38 && buffer[0] == (byte)'{'  && buffer[1] == CPU.GetDistance && buffer[37] == (byte)'}') // slave_id & function code
            {                            
                if (alisa.CRC.crc8(buffer, 0, 37) == 0) // check CRC8
                {
                   this._valid = buffer[34];
                   this._detected = buffer[35];
                   
                   System.out.println("Received!!!!");
                   for (int i=0; i<length; i++)
                   {
                       System.out.print((buffer[i] & 0xFF) + " ");
                   }
                   System.out.println();
                } 
                else 
                {
                    this._error = "getDistance | Read | CRC Error";
                    this._connected = false;
                    return false;
                }
                this._connected = true;
            } 
            else 
            {
                this._error = "getDistance | Read | Data Error";
                this._connected = false;
                return false;
            }   
        }
        catch (Exception ex) 
        {
            this._error = "getDistance | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
            return false;
        }
        
        return true;
    }
    //-------------------update----------------------
    public synchronized boolean setMinMax(byte[] min, byte[] max){
        if(min == null)         
            this.Max = max;
        else if(max == null)
            this.Min = min;
        else{
            this.Min = min;
            this.Max = max;
        }
        return true;
    }    
    public int[] getMin(){
        int[] min = new int[Min.length];
        for(int i = 0;i < min.length; i++)
            min[i] = (int)(Min[i]&0xff);
        return min;
    }    
    public int[] getMax(){
        int[] max = new int[Max.length];
        for(int i = 0;i < max.length; i++)
            max[i] = (int)(Max[i]&0xff);
        return max;
    }  
    public synchronized int[] getDistanceInt(int interval)
    {   
        //---wait until serial is idle---
        while(_serial_status != 0){
            System.out.print("");
        }       
        //set status
            _serial_status = 3;
        //-------------------------------
        
        int[] result = new int[17];
         //send
        try 
        {
            byte[] data = new byte[4];
            data[0] = (byte) '{';
            data[1] = (byte) CPU.GetDistance;
            int crc8 = alisa.CRC.crc8(data, 0, 2);
            data[2] = (byte)(crc8); 
            data[3] = (byte)'}';

            this._out.write(data);
            System.out.println("Sent");
            
            
        } 
        catch (Exception ex) 
        {
            this._error = "getDistance | Write | Exception Error | " + ex.getMessage();
            this._connected = false;
            this._reconnect();
        //set
            _serial_status = 0;
            return null;
        }
        try{  Thread.sleep(interval);  } 
        catch (InterruptedException  ex ){  
            _serial_status = 0;
            return null;  
        }
        //receive
        
        //wait until serial_status = 3
//        while(_serial_status != 3){}
        
        try 
        {
            byte[] buffer = new byte[1024];
            
            int length = this._in.read(buffer);
                     
            System.out.println(length);
           
           if (length >= 38 && buffer[0] == (byte)'{'  && buffer[1] == CPU.GetDistance && buffer[37] == (byte)'}') // slave_id & function code
            {                            
                if (alisa.CRC.crc8(buffer, 0, 37) == 0) // check CRC8
                {
                    this._valid = buffer[34];
                    this._detected = buffer[35];
                   
                    System.out.println("Received!!!!");
                    try{
//                        for (int i=0; i<16; i++)
//                        {
//                            int a = 0;
//                            try{
//                                a = ((int)((buffer[2*i+2] & 0xff) << 8) + (int)(buffer[2*i+3] & 0xff));
//                                result[i] = a;
//                            }catch(Exception e){
//                                result[i] = 0;
//                            }
//                            System.out.print( a + " " );
//                        }   
                        for (int i=2; i<34; i+=2)
                        {
                            int a = 0;
                            try{
                                a = ((int)((buffer[i] & 0xff) << 8) + (int)(buffer[i+1] & 0xff));
                                result[i/2-1] = a;
                            }catch(Exception e){
                                result[i/2-1] = 0;
                            }
                            System.out.print( a + " " );
                        }   
                        result[16] = (int)buffer[35] & 0xff;
                        System.out.println();
                    }catch(Exception e){
                        System.out.println("Error: " + e.getMessage());
                    //set
                        _serial_status = 0;
                   }
                } 
                else 
                {
                    this._error = "getDistance | Read | CRC Error";
                    this._connected = false;
                //set
                    _serial_status = 0;
                    return null;
                }
                this._connected = true;
            } 
            else 
            {
                this._error = "getDistance | Read | Data Error";
                this._connected = false;
            //set
                _serial_status = 0;
                return null;
            }   
        }
        catch (Exception ex) 
        {
            this._error = "getDistance | Read | Exception Error | " + ex.getMessage();
            this.distance = -3; 
            this._connected = false;
            this._reconnect();
        //set
            _serial_status = 0;
            return null;
        }
    //serial idle
        _serial_status = 0;
        return result;
    } 
    //----------------------------------------------------------
}

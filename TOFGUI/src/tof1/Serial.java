package tof1;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Serial extends Thread
{ 
    public boolean exit = false;
    
    private InputStream _in = null;
    private OutputStream _out = null;
            
    private CommPort _comm_port = null;
    
    private String _port_name = "";
    
    public int distance = 0;
     
    public Serial(String port_name)
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
        
        System.out.println("ddd");
        
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
    
    byte [] Min = new byte[16];
    byte [] Max = new byte[16];
    
    public boolean readConfig()
    {
        try
        {
            String path = "config.txt";
            List<String> lines = Files.readAllLines(Paths.get(path),
                    Charset.defaultCharset());
            
            String[] value_min = lines.get(0).split(",");
            String[] value_max = lines.get(1).split(",");
          
            for(int i=0; i<Min.length; i++)
            {
                Min[i] = Byte.parseByte(value_min[i]);
                Max[i] = Byte.parseByte(value_max[i]);
            }
            //for(int i=0; i<Min.length; i++){System.out.println(Min[i]+","+ Max[i]);}  
        }
        catch (Exception ex) {}
        
        return true;
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
                //write command id = 0
                {
                    byte[] data = new byte[4];
                    data[0] = (byte) '{';
                    data[1] = 0;
                    int crc8 = alisa.CRC.crc8(data, 0, 2);
                    data[2] = (byte)(crc8); 
                    data[3] = (byte)'}';

                    this._out.write(data);
                    System.out.println("Sent");
                }
                Thread.sleep(100);
                
                {
                    byte[] buffer = new byte[1024];
                    int length = this._in.read(buffer);
                     
                    System.out.println(length);
                    if (length >= 7)
                    {
                        if (buffer[0] == (byte)'{'  && buffer[1] == 0 && buffer[6] == (byte)'}') // slave_id & function code
                        {                            
                            if (alisa.CRC.crc8(buffer, 0, 6) == 0) // check CRC8
                            {
                               System.out.println("Received!!!!");
                               for (int i=0; i<length; i++)
                               {
                                   System.out.print((buffer[i] & 0xFF) + " ");
                               }
                               System.out.println();
                            }
                        }
                    }                    
                }
            
        /*      
                //command id =1 -->write to Arduino
                // write
                {
                    byte []b = new byte[36];
                    
                    b[0] = (byte)'{';     
                    b[1] = 1; //command id 
                    for(int i=2; i<18; i++)
                    {
                        b[i] = Min[i-2];
                    }
                   
                    for(int i=18; i<34; i++)
                    {
                        b[i] = Max[i-18];
                    }
                    
                    int crc8 = alisa.CRC.crc8(b, 0, 34);
                    b[34] = (byte)(crc8); 
                    
                    b[35] = (byte)'}';

                    this._out.write(b);
                    System.out.println("Sent");
                }
                
                Thread.sleep(100);
                
                // read
                {
                    byte[] buffer = new byte[1024];
                    int length = this._in.read(buffer);
                     
                    System.out.println(length);
                    if (length >= 5)
                    {
                        if (buffer[0] == (byte)'{'  && buffer[1] == 1 && buffer[4] == (byte)'}') // slave_id & function code
                        {                            
                            if (alisa.CRC.crc8(buffer, 0, 4) == 0) // check CRC8
                            {
                               System.out.println("Received!!!!");
                               for (int i=0; i<length; i++)
                               {
                                   System.out.print(Integer.toHexString(buffer[i]) + " ");
                               }
                               System.out.println();
                               
                               // close program
                               System.exit(0);
                               return;
//                                for (int i=0; i<16; i++)
//                                {
//                                    int distance = ((buffer[3+(i*2)] & 0xFF) << 8) + (buffer[4+(i*2)] & 0xFF);
//                                    s += distance + ",";
//                                }                                
//                                System.out.println(s);
                            }
                        }
                    }                    
                }
            */   
              
                /*
                //command id =2 -->getData from Arduino
                // write
                {
                    byte []b = new byte[4];
                    
                    b[0] = (byte)'{';     
                    b[1] = 2; //command id 
                    int crc8 = alisa.CRC.crc8(b, 0, 2);
                    b[2] = (byte)(crc8); 
                    b[3] = (byte)'}';
                    this._out.write(b);
                    System.out.println("Sent");
                }
                
                Thread.sleep(100);
                
                // read
                {
                    byte[] buffer = new byte[1024];
                    int length = this._in.read(buffer);
                     
                    System.out.println(length);
                    if (length >= 36)
                    {
                        if (buffer[0] == (byte)'{'  && buffer[1] == 2 && buffer[35] == (byte)'}') // slave_id & function code
                        {                            
                            if (alisa.CRC.crc8(buffer, 0, 35) == 0) // check CRC8
                            {
                               System.out.println("Received!!!!");
                               for (int i=0; i<length; i++)
                               {
                                   System.out.print((buffer[i] & 0xFF) + " ");
                               }
                               System.out.println();
                               
                               // close program
                               System.exit(0);
                               return;
                            }
                        }
                    }                    
                }
                */
                
                /* 
                //command id =3 -->get distance from Arduino
                // write
                {
                    byte []b = new byte[4];
                    
                    b[0] = (byte)'{';     
                    b[1] = 3; //command id 
                    int crc8 = alisa.CRC.crc8(b, 0, 2);
                    b[2] = (byte)(crc8); 
                    b[3] = (byte)'}';
                    this._out.write(b);
                    System.out.println("Sent");
                }
                
                Thread.sleep(100);
                
                // read
                {
                    byte[] buffer = new byte[1024];
                    int length = this._in.read(buffer);
                     
                    System.out.println(length);
                    if (length >= 36)
                    {
                        if (buffer[0] == (byte)'{'  && buffer[1] == 3 && buffer[35] == (byte)'}') // slave_id & function code
                        {                            
                            if (alisa.CRC.crc8(buffer, 0, 35) == 0) // check CRC8
                            {
                               System.out.println("Received!!!!");
                               for (int i=0; i<length; i++)
                               {
                                   System.out.print((buffer[i] & 0xFF) + " ");
                               }
                               System.out.println();
                               
                               // close program
//                               System.exit(0);
//                               return;
                            }
                        }
                    }                    
                }
*/
                
                Thread.sleep(80);
            } 
            catch (Exception ex) 
            {
                ex.printStackTrace();
                
                this.distance = -3;               
                
                // reconnect
                try 
                {
                    this.reconnect();
                    Thread.sleep(1000);
                } 
                catch (Exception ex2) { }
            }
            
            try { Thread.sleep(1000); } catch (Exception ex2) { }
        }
    }    
}

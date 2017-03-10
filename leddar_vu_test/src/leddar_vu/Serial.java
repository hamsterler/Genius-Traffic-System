package leddar_vu;

// Serial Communication is using RXTX 2.2 
// Download from http://rxtx.qbang.org/wiki/index.php/Download

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class Serial extends Thread
{ 
    public boolean exit = false;    
    
    private InputStream _in = null;
    private OutputStream _out = null;
            
    private CommPort _comm_port = null;
    
    private String _port_name = "";
    
    private int[] _distance = new int[16];

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
            return false; 
        }
        
        return true;
    }
    
    public void reconnect()
    {
        if (this._comm_port != null) try { this._comm_port.close(); } catch (Exception ex) { }
        this.connect();
    }
    
    public void disconnect()
    {
        this.exit = true;
        this.interrupt();
        
        if (this._comm_port != null) try { this._comm_port.close(); } catch (Exception ex) { }
        this._comm_port = null;
    }
    
    private void _reportServerId(int delay)
    {
        // 0x11 report server id 
        try 
        {
            System.out.print("report server id...");
            
            // write
            {
                byte []b = new byte[4];

                b[0] = 1;       // slave id

                b[1] = 0x11;    // function code

                int crc16 = crc16(b, 0, 2);
                b[2] = (byte)(crc16 >> 8); // high
                b[3] = (byte)(crc16 & 0xFF); // low

                this._out.write(b);
            }

            // wait for response
            Thread.sleep(delay);

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

                System.out.println(length + " bytes received");
                if (length >= 158)
                {
                    if (buffer[0] == 1 && buffer[1] == 0x11) // slave_id & function code & detectors
                    {                            
                        if (crc16(buffer, 0, 158) == 0) // check CRC16
                        {
                            String s = "device name = ";
                            for (int i=0; i<32; i++)
                            {
                                char c = (char)(buffer[36+i] & 0xFF);
                                s += c;
                            }
                            System.out.println(s);

                            s = "software version = ";
                            for (int i=0; i<32; i++)
                            {
                                char c = (char)(buffer[100+i] & 0xFF);
                                s += c;
                            }
                            System.out.println(s);
                        }
                    }
                }
            }
            else System.out.println("no response");
        }
        catch (Exception ex) 
        {                
            System.out.println("error: " + ex.getMessage());
            // reconnect
            try 
            {
                this.reconnect();
                Thread.sleep(1000);
            } 
            catch (Exception ex2) { }
        }            
    }
    
    private void _getDetection(int delay)
    {
        // 0x41 get detection 
        try 
        {
            System.out.print("get detection...");
            
            // write
            {
                byte []b = new byte[4];

                b[0] = 1;       // slave id

                b[1] = 0x41;    // function code

                int crc16 = crc16(b, 0, 2);
                b[2] = (byte)(crc16 >> 8); // high
                b[3] = (byte)(crc16 & 0xFF); // low

                this._out.write(b);
            }

            // wait for response
            Thread.sleep(delay);

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
                System.out.println(length + " bytes received");

                if (length == 60)
                {
                    // Leddar VU
                    if (buffer[0] == 1 && buffer[1] == 0x41 && buffer[2] == 8) // slave_id & function code & detectors
                    {                            
                        if (crc16(buffer, 0, 60) == 0) // check CRC16
                        {
                            String s = "";
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH) + 1; // Jan = 0, dec = 11
                            int date = calendar.get(Calendar.DAY_OF_MONTH); 
                            int hour = calendar.get(Calendar.HOUR_OF_DAY); // 24 hour clock
                            int minute = calendar.get(Calendar.MINUTE);
                            int second = calendar.get(Calendar.SECOND);

                            if (year < 10) s += "0" + year; s += year;  s += "-";
                            if (month < 10) s += "0"; s += month;       s += "-";
                            if (date < 10) s += "0"; s += date;         s += " ";

                            if (hour < 10) s += "0"; s += hour;         s += ":";
                            if (minute < 10) s += "0"; s += minute;     s += ":";
                            if (second < 10) s += "0"; s += second;

                            s += " | VU : distance = ";
                            int offset = 3;
                            for (int i=0; i<8; i++)
                            {
                                int d = (buffer[offset] & 0xFF) + ((buffer[offset+1] & 0xFF) << 8); // [LO][HI]
                                this._distance[i] = d;
                                s += d + ",";
                                offset += 6;
                            }
                            System.out.println(s);
                        }
                    }
                }
                else if (length == 91)
                {
                    // Leddar M16
                    if (buffer[0] == 1 && buffer[1] == 0x41 && buffer[2] == 16) // slave_id & function code & detectors
                    {
                        if (crc16(buffer, 0, 91) == 0) // check CRC16
                        {
                            String s = "";
                            s += "M16 : distance = ";
                            int offset = 3;
                            for (int i=0; i<16; i++)
                            {
                                int d = (buffer[offset] & 0xFF) + ((buffer[offset+1] & 0xFF) << 8); // [LO][HI]
                                this._distance[i] = d;
                                s += d + ",";
                                offset += 5;
                            }                                
                            System.out.println(s);
                        }     
                    }                        
                }
            }
            else System.out.println("no response");
        } 
        catch (Exception ex) 
        {                
            System.out.println("error: " + ex.getMessage());
            // reconnect
            try 
            {
                this.reconnect();
                Thread.sleep(1000);
            } 
            catch (Exception ex2) { }
        }
    }
    
    private void _floodGarbage()
    {
        // write garbage for testing
        try 
        {
            System.out.println("garbage");
            byte []b = new byte[100];

            Random r = new Random();
            for (int i=0; i<b.length; i++)
            {
                b[i] = (byte)r.nextInt(255);
            }                   

            this._out.write(b);

        }
        catch (Exception ex) 
        {                
            System.out.println("Error: " + ex.getMessage());
            // reconnect
            try 
            {
                this.reconnect();
                Thread.sleep(1000);
            } 
            catch (Exception ex2) { }
        }
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
            
            //this._reportServerId(200);            
            //try { Thread.sleep(10); } catch (InterruptedException ex) { }

            this._getDetection(100);
            try { Thread.sleep(100); } catch (InterruptedException ex) { }
         
            //this._floodGarbage();            
            //try { Thread.sleep(10); } catch (InterruptedException ex) { }
        }
    }    
    
    private final int[/*512*/] _crc16_table = 
    {
        0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64,
        1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65,
        1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65,
        0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64,
        1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65,
        0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64,
        0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64,
        1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65,
        1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65,
        0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64,
        0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64,
        1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65,
        0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64,
        1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65,
        1, 192, 128, 65, 0, 193, 129, 64, 0, 193, 129, 64, 1, 192, 128, 65,
        0, 193, 129, 64, 1, 192, 128, 65, 1, 192, 128, 65, 0, 193, 129, 64,
        0, 192, 193, 1, 195, 3, 2, 194, 198, 6, 7, 199, 5, 197, 196, 4,
        204, 12, 13, 205, 15, 207, 206, 14, 10, 202, 203, 11, 201, 9, 8, 200,
        216, 24, 25, 217, 27, 219, 218, 26, 30, 222, 223, 31, 221, 29, 28, 220,
        20, 212, 213, 21, 215, 23, 22, 214, 210, 18, 19, 211, 17, 209, 208, 16,
        240, 48, 49, 241, 51, 243, 242, 50, 54, 246, 247, 55, 245, 53, 52, 244,
        60, 252, 253, 61, 255, 63, 62, 254, 250, 58, 59, 251, 57, 249, 248, 56,
        40, 232, 233, 41, 235, 43, 42, 234, 238, 46, 47, 239, 45, 237, 236, 44,
        228, 36, 37, 229, 39, 231, 230, 38, 34, 226, 227, 35, 225, 33, 32, 224,
        160, 96, 97, 161, 99, 163, 162, 98, 102, 166, 167, 103, 165, 101, 100, 164,
        108, 172, 173, 109, 175, 111, 110, 174, 170, 106, 107, 171, 105, 169, 168, 104,
        120, 184, 185, 121, 187, 123, 122, 186, 190, 126, 127, 191, 125, 189, 188, 124,
        180, 116, 117, 181, 119, 183, 182, 118, 114, 178, 179, 115, 177, 113, 112, 176,
        80, 144, 145, 81, 147, 83, 82, 146, 150, 86, 87, 151, 85, 149, 148, 84,
        156, 92, 93, 157, 95, 159, 158, 94, 90, 154, 155, 91, 153, 89, 88, 152,
        136, 72, 73, 137, 75, 139, 138, 74, 78, 142, 143, 79, 141, 77, 76, 140,
        68, 132, 133, 69, 135, 71, 70, 134, 130, 66, 67, 131, 65, 129, 128, 64
    };

    private int crc16(byte[] data) 
    {
        int index;
        int crc_Low = 255;
        int crc_High = 255;

        for (int i = 0; i < data.length; i++) 
        {
            index = crc_High ^ (data[i] & 0xFF);
            crc_High = (crc_Low ^ _crc16_table[index]);
            crc_Low = _crc16_table[index + 256];
        }

        return ((crc_High & 0xFF) << 8) + (crc_Low & 0xFF);
    }
    
    private int crc16(byte[] data, int start, int length) 
    {
        int index;
        int crc_Low = 255;
        int crc_High = 255;

        for (int i = 0; i < length; i++) 
        {
            index = crc_High ^ (data[start + i] & 0xFF);
            crc_High = (crc_Low ^ _crc16_table[index]);
            crc_Low = _crc16_table[index + 256];
        }

        return ((crc_High & 0xFF) << 8) + (crc_Low & 0xFF);
    }
}

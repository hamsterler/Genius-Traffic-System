
package file_sync_client_2.pkg0;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientFileThread extends java.lang.Thread
{       
    public ClientFileThread(Client client){
        this.client = client;
        this._timeout = this.client.getTimeout();
    }


//----------Attribute-----------	
    private int _id = -1;
	private String _path = "";
	private String _key = "";
    private String _error = "";
    private Client client = null;
    private int _interval = 1000;
    private int _timeout = -1;

    private boolean isMessageCorrect = false;
    public Encode encode = new Encode();  
    private Socket _socket = null;
    public boolean isFinish = false;
    OutputStream _out = null;
    InputStream _in = null;
//------------------------------	


//----------Get Method----------
    public int getID(){
            return this._id;
    }
    public String getPath(){
            return this._path;
    }
    public String getKey(){
            return this._key;
    }
    public String getError(){
        return this._error;
    }
//------------------------------


//----------------Load Function------------------
    public boolean load(alisa.json.Object obj){ 
        this._id = getIntegerJson(obj, "id");
        this._path = getStringJson(obj, "path");
        this._key = getStringJson(obj, "key");         
        
        int timeout = getIntegerJson(obj, "timeout");
        if(timeout > 0) 
            this._timeout = timeout;
        int interval = getIntegerJson(obj, "interval");
        if(interval > 0 )
            this._interval = interval; 

        if(this._id == -1 || this._path == null || this._key == null){
            this._error = "Null value Attribute in ClientFileThread.load()";
            return false;        
        }
        return true;    
    }
//-----------------------------------------------

        
//------------------------------------------run---------------------------------------------
    @Override
    public void run(){
        //Connect
        loopConnect();
        while(true){
            try{
                //get file
                File file = new File(this._path);
                while(!file.exists()){
                    System.out.println("File-" + this._id + ":File Not Found.");
                    try { Thread.sleep(this._interval); } catch (InterruptedException ex) { }
                    file = new File(this._path);
                }
                
                // read file 
                byte[] data = Files.readAllBytes(Paths.get(this._path));

                //CRC data >> encrypt (data+crc) >> pack with id and start&end byte = [255][HI_ID][LOW_ID][..Encrypted Data&crc][254]
                byte[] message = packAllData1(data); 
                
                //Connect
                // loopConnect();

                //sendmessage
                try{
                    // int missing_message_count = 0;
                    boolean isSend = send(message,this);
                    System.out.println("File-" + this._id + " Sending Message... ");
                    while(!isSend){
                        // missing_message_count++;
                        System.out.println("File-" + this._id + " Resending Message... ");
                        try { Thread.sleep(this._interval); } catch (InterruptedException ex) { }
                        isSend = send(message,this);
                    }    
                    System.out.println("File-" + this._id + " Sending Success!!");
                
                } catch (IOException ex){ 
                    System.out.println("File-" + this._id + ": Server was closed.");
                    this.isFinish = true;                    
                    return;
                } 

                // _disconnect();
                
            } catch (IOException e){
                System.out.println("File-" + this._id + " | " + e.getMessage());
            }
            
//            if(this._socket.isClosed())
//                return;
            //sleep 
            try { Thread.sleep(this._interval); } catch (InterruptedException ex) { }
        }      
    }
//------------------------------------------------------------------------------------------
        

//------------------------------------pack Data before Send---------------------------------------        
        public byte[] packAllData1(byte[] data) throws UnsupportedEncodingException{
            //CRC
            int crc16  = alisa.CRC.crc16(data, 0, data.length);
            byte hi = (byte)((crc16 >> 8) & 0xff);
            byte lo = (byte)(crc16 & 0xff);
            
            //crcData <- data + [hi][lo]
            byte[] crcData = new byte[data.length + 2];
            System.arraycopy(data, 0, crcData, 0, data.length);
            crcData[crcData.length-2] = hi;
            crcData[crcData.length-1] = lo;

            byte[] secret = encode.encrypt(crcData,this._key);

            byte[] message = new byte[secret.length + 4];
            message[0] = (byte)255;
            message[1] = (byte)((this._id >> 8) & 0xff);
            message[2] = (byte)(this._id & 0xff);
            System.arraycopy(secret, 0, message, 3, secret.length);
            message[message.length-1] = (byte)254;
            System.out.println("secret: " + secret.toString());
            return message;
        }
        
        //decrypt just data not include crc value
        public byte[] packAllData2(byte[] data) throws UnsupportedEncodingException{
                    
            //encrypt
            byte[] secret = encode.encrypt(data,this._key);
        
            //CRC
            int crc16  = alisa.CRC.crc16(secret, 0, secret.length);
            byte hi = (byte)((crc16 >> 8) & 0xff);
            byte lo = (byte)(crc16 & 0xff);
            
            //data + [hi][lo]
            byte[] crcData = new byte[secret.length + 2];
            System.arraycopy(secret, 0, crcData, 0, secret.length);
            crcData[crcData.length-2] = hi;
            crcData[crcData.length-1] = lo;
            
            //prepare message before send (this one work leaw na ja mai tong kae leaw ei ei)
            byte[] message = new byte[crcData.length + 4];
            message[0] = (byte)255;
            message[1] = (byte)((this._id >> 8) & 0xff);
            message[2] = (byte)(this._id & 0xff);
            System.arraycopy(crcData, 0, message, 3, crcData.length);
            message[message.length-1] = (byte)254;
            return message;
        }
//--------------------------------------------------------------------------------------


//------------------------------------Send-----------------------------------------
        public boolean send(byte[] message, ClientFileThread thread) throws IOException{
            //send
            // try{
                this._out.write(message);
            // }
            // catch(IOException ex){
            //     System.out.println("File-" + this._id + ": Server was closed.");
            //     return false;
            // }

            //receive
            // try{
                byte[] data = new byte[1024];
                int length = this._in.read(data);
                              
                //if message that server receive is true
                if (length >= 5 && 
                   (data[0] & 0xFF) == 255 && (data[4] & 0xFF) == 254 &&
                   (data[3] & 0xFF) == 0 &&
                    data[1] == (byte)((this._id >> 8) & 0xFF) && data[2] == (byte)((this._id & 0xFF))){                                      
                }
                else{
                    System.out.println("File-" + this._id + ": Wrong  Message Sending");
                    return false;
                }                        
            // } 
            // catch (IOException ex){ 
            //     System.out.println("File-" + this._id + ": Server was closed.");
            //     return false;
            // } 
        return true;
    }
//---------------------------------------------------------------------------------        


//---------------------------------------Connecttion----------------------------------------
    private boolean _connect(){
        try{
            this._socket = new Socket(this.client.getAddress(), this.client.getPort());
            this._socket.setSoTimeout(this._timeout);

            this._in = this._socket.getInputStream();
            this._out = this._socket.getOutputStream();
        } 
        catch (IOException ex){
            return false;
        }
        System.out.println("File-" + this._id + ": Connected to The Server");
        return true;
    }
    
    private void _reconnect(){
        this._disconnect();
        try { Thread.sleep(1000); } catch (InterruptedException ex) { }            
        this.loopConnect();
    }
        
    public void loopConnect(){
        while(!_connect()){
            System.out.println("File-" + this._id + ": Wait For Connection.");
        }
    }
//--------------------------------------------------------------------------------------
    
//-------------Disconnect-------------
    private void _disconnect(){
        try{ 
            if (this._in != null){ 
                this._in.close(); 
                this._in = null; 
            }
        } 
        catch (IOException ex) { }
        
        try{
            if (this._out != null){ 
                this._out.close(); 
                this._out = null; 
            }
        } 
        catch (IOException ex) { }            
        
        try{
            this._socket.close(); 
            this._socket = null;
        } 
        catch (IOException ex) { }  
        System.out.println("File-" + this._id + ": Disconnected");
    }  
//------------------------------------
    

//----------------------get value for object--------------------------
    public String getStringJson(alisa.json.Object obj, String name){
        try{
            alisa.json.Data text = obj.findData(name);
            if (text == null || !text.isString()){
                this._error = "Error on <ClientFileThread> " + name; 
                return null;
            }
            return text.getString();
        } catch(NullPointerException e){
            return null;
        }
    }

    public int getIntegerJson(alisa.json.Object obj, String name){
        try{
            alisa.json.Data text = obj.findData(name);
            if (text == null || !text.isInteger()){
                this._error = "Error on <ClientFileThread> " + name; 
                return -1;
            }
            return text.getInteger();
        } catch(NullPointerException e){
            return -1;
        }
    }   
//--------------------------------------------------------------------

}
    
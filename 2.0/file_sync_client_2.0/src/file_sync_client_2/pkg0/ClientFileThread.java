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
	private String _password = "";
    private byte[] _key = null;
    private String _error = "";
    private Client client = null;
    private int _interval = 1000;
    private int _timeout = -1;

    private boolean isMessageCorrect = false;
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
    public String getPassword(){
            return this._password;
    }
    public String getError(){
        return this._error;
    }
//------------------------------


//----------------Load Function------------------
    public boolean load(alisa.json.Object obj) throws UnsupportedEncodingException{ 
        this._id = getIntegerJson(obj, "id");
        this._path = getStringJson(obj, "path");
        this._password = getStringJson(obj, "password");         
        
        int timeout = getIntegerJson(obj, "timeout");
        if(timeout > 0) 
            this._timeout = timeout;
        int interval = getIntegerJson(obj, "interval");
        if(interval > 0 )
            this._interval = interval; 

        if(this._id == -1 || this._path == null || this._password == null){
            this._error = "Null value Attribute in ClientFileThread.load()";
            return false;        
        }
        
        this._key= new Hash().hash(this._password.getBytes("ISO-8859-1"));

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
               
                //sendmessage
                try{             
                    //  Send  
                    boolean isSend = send(message,this);
                    System.out.println("File-" + this._id + " Sending Message... ");
                    if(!isSend)
                        continue;     
                    System.out.println("File-" + this._id + " Sending Success!!");
                } catch (IOException ex){  
                    System.out.println("File-" + this._id + ": Server was closed.");
                    this.isFinish = true;                    
                    return;
                } 
                
            } catch (IOException e){
                System.out.println("File-" + this._id + " | " + e.getMessage());
            }

            try { Thread.sleep(this._interval); } catch (InterruptedException ex) { }
        }      
    }
//------------------------------------------------------------------------------------------
        

//------------------------------------pack Data before Send---------------------------------------        
        public byte[] packAllData1(byte[] data) {
            byte[] message = null;
            try{
                //CRC
                int crc16  = alisa.CRC.crc16(data, 0, data.length);
                byte hi = (byte)((crc16 >> 8) & 0xff);
                byte lo = (byte)(crc16 & 0xff);

                //crcData <- data + [hi][lo]
                byte[] crcData = new byte[data.length + 2];
                System.arraycopy(data, 0, crcData, 0, data.length);
                crcData[crcData.length-2] = hi;
                crcData[crcData.length-1] = lo;
                System.out.println("File-" + this._id + ": key length = " +this._key.length);
                byte[] secret = new Encode().encrypt(crcData, this._key);

                //check, can the encoded data be decode and crc?. before send it to server
                byte[] decode = new Encode().decrypt(secret, this._key);
                if(alisa.CRC.crc16(decode, 0, decode.length) != 0){
                    System.out.println("File-" + this._id + ": Wrong Encoding data!!");
                    return packAllData1(data);
                }

                System.out.println("File-" + this._id + ": Password = " +this._password + "\n" + "File-" + this._id + ": Hash = " + new String(this._key, "ISO-8859-1"));
            // //---------show crc data------------------------
            //     String show2 = "";
            //     for(int i =0; i < crcData.length; i++){
            //        show2 += (int)crcData[i] + " ";
            //     }
            //     System.out.println("File-" + this._id + " crcData: " + show2);
            // //------------------------------------------
            // //---------show encnrypted data-----------------
            //     String show3 = "";
            //     for(int i =0; i < secret.length; i++){
            //        show3 += (int)secret[i] + " ";
            //     }
            //    System.out.println("File-" + this._id + " secret(no header): " + show3);
            // //------------------------------------------
            // //---------show decnrypted data-----------------
            //     String show4 = "";
            //     for(int i =0; i < decode.length; i++){
            //         show4 += (int)decode[i] + " ";
            //     }
            //     System.out.println("File-" + this._id + " decodeed secret: " + show4);
            // //------------------------------------------

                message = new byte[secret.length + 4];
                System.arraycopy(secret, 0, message, 3, secret.length);
                message[0] = (byte)255;
                message[1] = (byte)((this._id >> 8) & 0xff);
                message[2] = (byte)(this._id & 0xff);
                message[message.length-1] = (byte)254;
            }catch(Exception e){
                System.out.println("File-" + this._id + ": packAllData Error");
                try { Thread.sleep(this._interval); } catch (InterruptedException ex) { }
                return packAllData1(data);
            }
            return message;
        }      
//--------------------------------------------------------------------------------------


//------------------------------------Send-----------------------------------------
        public boolean send(byte[] message, ClientFileThread thread) throws IOException{
            // send
            this._out.write(message);
//            System.out.println("File-" + this._id + ": message length = " + message.length);
            
            // Receive
            byte[] data = new byte[1024];
            int length = this._in.read(data);
                          
            // if message that server receive is true
            if (length >= 5 && 
               (data[0] & 0xFF) == 255 && (data[4] & 0xFF) == 254 &&
               (data[3] & 0xFF) == 0 &&
                data[1] == (byte)((this._id >> 8) & 0xFF) && data[2] == (byte)((this._id & 0xFF))){                                      
            }
            else{
                System.out.println("File-" + this._id + ": Wrong  Message Sending");
                return false;
            }                        
            
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
    
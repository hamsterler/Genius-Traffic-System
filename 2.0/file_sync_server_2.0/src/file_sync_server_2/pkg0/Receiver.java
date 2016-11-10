package file_sync_server_2.pkg0;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Receiver extends java.lang.Thread
{       
    public Receiver(Socket socket, Server server){
        this._server = server;
        if (this._server.connection_count >= 9999) 
            this._server.connection_count = 0;
        else this._server.connection_count++;

        this._socket = socket;
        try { 
            this._socket.setSoTimeout(this._server.getTimeout()); 
        } catch (Exception ex) {}       
    }


//--------------Attribute---------------
    private Socket _socket = null;
    private Server _server = null;
    InputStream _in = null;
    OutputStream _out = null;
    private int _file_id = -1;
    
    private int _interval = 1000;
    private boolean _resend = false;
//--------------------------------------
    

//--------------------------------------------------------Run--------------------------------------------------------
    public void run(){
        this.loopConnect();         
        System.out.println("File-"+ this._file_id + ": Connected");
        
        while(true)
        {   
            if(this._socket.isClosed())
                return;
            _resend = false;
            try{ 
                byte [] buffer = new byte[_server.getMaxFileSize()];              
                
                //read
                int buffer_length = this._in.read(buffer, 0, buffer.length); //this function can definitely tell the size of the file that was received from inpustream
                if (buffer_length >= 4 && (buffer[0] & 0xFF) == 255 && (buffer[buffer_length-1] & 0xff) == 254){             
                    this._file_id = ((buffer[1] & 0xFF) << 8) + (buffer[2] & 0xFF);
//                    System.out.println("ID = " + this._file_id);
                    ServerFile file = findServer(this._file_id);
                    if(file != null){
                        this._interval = file.getInterval();
                        // System.out.println("File-" + this._file_id + ": message length = " + buffer_length);
//                        System.out.println("File-" + this._file_id + ": Timeout = " +file.getTimeout());
                        //---------show data------------------------
//                        System.out.print("File-" + file.getID() + " secret : ");
//                        String show = "";
//                        for(int i =0; i < buffer_length; i++){
//                            show += (int)buffer[i] + " ";
//                        }
//                        System.out.println(show);
                        //------------------------------------------

                        //---------------------type 1-----------------------
                        byte[] encryptedData = new byte[buffer_length-4];
                        System.arraycopy(buffer, 3, encryptedData, 0, buffer_length-4);

                        //---------show encnrypted data-----------------
//                        String show3 = "";
//                        for(int i =0; i < encryptedData.length; i++){
//                            show3 += (int)encryptedData[i] + " ";
//                        }
//                        System.out.println("File-" + this._file_id + " secret(no header): " + show3);
                        //----------------------------------------------
                        
//                        System.out.println("File-" + this._file_id + ": secret = " + new String(encryptedData));
                        System.out.println("File-" + this._file_id + ": Key = " + file.getKey());
                        byte[] data = new Encode().decrypt(encryptedData, file.getKey());
                            
//                        System.out.println("File-" + this._file_id + ": data = " + new String(data));
                        // System.out.println("File-" + this._file_id + "data + crc length = " + data.length );
                        
                        //---------show crc data------------------------
                        
//                        String show2 = "";
//                        for(int i =0; i < data.length; i++){
//                            show2 += (int)data[i] + " ";
//                        }
//                        System.out.println("File-" + this._file_id + " crcData: " + show2);
                        //------------------------------------------
                        
                        if(alisa.CRC.crc16(data, 0, data.length)== 0){
                            System.out.println("File-" + this._file_id + ": crc correct");
                            this._reply(this._file_id, 0);
                            
                            byte[] dataWrite = new byte[data.length-2];
                            System.arraycopy(data, 0, dataWrite, 0, data.length-2);
                            writeBin(dataWrite, dataWrite.length, file.getPath());
//                            System.out.println("File-" + this._file_id + ": Create file >> " + file.getPath());
                            
                        }
                        else{
                            _resend = true;
                            System.out.println("File-" + this._file_id + ": crc wrong");
                        }
                        //--------------------------------------------------

                    }   
                    else{                            
//                        System.out.println("File-" + this._file_id + ": file = null");
                    }
                }
                else{
                    _resend = true;
//                    System.out.println("File-" + this._file_id + ": Wrong Type Of Message.");
                }         
            }
            catch (Exception ex){  
                System.out.println("File-" + this._file_id + ": Close Thread.");         
                return;
            }
            
            
            if(_resend == true){   
                this._reply(this._file_id, 1);
            }

            try { Thread.sleep(this._interval); } catch (InterruptedException ex) { }
        }                    
    }
//-------------------------------------------------------------------------------------------------------------------


//------------------------------Write Bin------------------------------
    public boolean writeBin(byte[] data, int size, String path) 
    { 
        try 
        {             
            FileOutputStream f = new FileOutputStream(path);
            f.write(data, 0, size);

            f.close();
        } 
        catch (IOException e) 
        {
            System.out.println("Writing file error : " + e.getMessage());
        }     
        return true;
    }
//---------------------------------------------------------------------
    

//-------------------------------------Reply-------------------------------------
    private void _reply(int id, int message){
        byte[] data = new byte[5];
        data[0] = (byte)255;
        data[1] = (byte)((id >> 8) & 0xff);
        data[2] = (byte)(id & 0xff);
        data[4] = (byte)(254);
        data[3] = 1;
        if(message == 0){
            data[3] = 0;
        }
        try{
            this._out.write(data);
//            System.out.println("File-" + id + ": Send Reply value = " + message);
        } catch(IOException ex){
            System.out.println("Error: Cannot Send reply");
        }
    }
//-------------------------------------------------------------------------------


//---------------------Find Server---------------------
    public ServerFile findServer(int id){
        ServerFile[] files = this._server.getFile();
        for(int i = 0; i < files.length; i++){
            if(files[i].getID() == id)
                return files[i];           
        }
        return null;
    }
//-----------------------------------------------------


//--------------------------------------------Connect--------------------------------------------
    private boolean _connect()
    {
        try 
        { 
//            this._in = new DataInputStream(new BufferedInputStream(this._socket.getInputStream()));
            this._in = this._socket.getInputStream();
            this._out = this._socket.getOutputStream();
            this._socket.setSoTimeout(this._server.getTimeout());
            System.out.println("File-" + this._file_id + ": setSoTimeout = " + this._server.getTimeout());
        }
        catch (Exception e) { return false; }
        return true;
    }
//-----------------------------------------------------------------------------------------------
    

//---------------------------------Loop Connect--------------------------------    
    public void loopConnect(){
        while(!_connect()){
            System.out.println("File-" + this._file_id + ": Wait For Connection. " );
        }
    }
//-----------------------------------------------------------------------------


//---------------------Disonnect------------------------
    private void _disconnect()
    {
        try{ 
            if (this._in != null) this._in.close(); 
        } 
        catch (IOException ex) { }
        
        try{ 
            if (this._out != null) this._out.close(); 
        } 
        catch (IOException ex) { }
        
        try{ 
            this._socket.close();
        } 
        catch (IOException ex) { }    
    }
//-----------------------------------------------------
}
    
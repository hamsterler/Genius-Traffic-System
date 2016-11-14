package file_sync_server_2.pkg0;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServerFile 
{       
    public ServerFile(Server server){
        this.server = server;
        this._timeout = server.getTimeout();
        this._interval = 1000;
    }

//-----------------Attribute------------------	
    private int _id = -1;
	private String _path = "";
	private String _password = "";
    private byte[] _key = null;
    private String _error = "";
    private Server server = null;
    private int _timeout = -1;
    private int _interval = -1;

    private boolean isMessageCorrect = false;
    public Encode encode = new Encode();   
    private Socket _socket = null;
    OutputStream _out = null;
    InputStream _in = null;
//---------------------------------------------
        

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
    public byte[] getKey(){
            return this._key;
    }
    public String getError(){
        return this._error;
    }
    public int getTimeout(){
        return this._timeout;
    }
    public int getInterval(){
        return this._interval;
    }
//------------------------------

        
//--------------------------Load Function-----------------------------
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
            this._error = "Null value Attribute in ServerFileload()";
            return false;           
        }

        this._key = new Hash().hash(this._password.getBytes("ISO-8859-1"));
//        this._hash = this._password;
        return true;    
	}
//--------------------------------------------------------------------


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
    
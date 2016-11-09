package file_sync_server_2.pkg0;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServerFile 
{       
    public ServerFile(Server server){
        this.server = server;
    }

//-----------------Attribute------------------	
    private int _id = -1;
	private String _path = "";
	private String _key = "";
    private String _error = "";
    private Server server = null;
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
    public String getKey(){
            return this._key;
    }
    public String getError(){
        return this._error;
    }
//------------------------------

        
//--------------------------Load Function-----------------------------
	public boolean load(alisa.json.Object obj){ 
        this._id = getIntegerJson(obj, "id");
        this._path = getStringJson(obj, "path");
        this._key = getStringJson(obj, "key");
        if(this._id == -1 || this._path == null || this._key == null){
            this._error = "Null value Attribute in ServerFileload()";
            return false;           
        }
        return true;    
	}
//--------------------------------------------------------------------


//----------------------get value for object--------------------------
    public String getStringJson(alisa.json.Object obj, String name){
        alisa.json.Data text = obj.findData(name);
        if (text == null || !text.isString())
            return null;
        return text.getString();
    }

    public int getIntegerJson(alisa.json.Object obj, String name){
        alisa.json.Data text = obj.findData(name);
        if (text == null || !text.isInteger())
            return -1;
        return text.getInteger();
    }   
//--------------------------------------------------------------------  
}
    
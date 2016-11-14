package file_sync_server_2.pkg0;

import java.io.UnsupportedEncodingException;

public class Server
{        
        
//----------------Attribute----------------
    private String _error = "";
    private String _version = "";
    private int _max_file_size = -1;
    private int _port = -1;
    private int _timeout = -1;
    private ServerFile[] _files = null;
    public int connection_count = 0;
//-----------------------------------------


//--------------Get Method------------------
    public String getError(){
        return this._error;
    } 
    public String getVersion(){
        return this._version;
    } 
    public int getMaxFileSize(){
        return this._max_file_size;
    } 
    public int getPort(){
        return this._port;
    } 
    public int getTimeout(){
        return this._timeout;
    } 
    public ServerFile[] getFile(){
        return this._files;
    }
//------------------------------------------
   

//----------------------------------Load Config---------------------------------
   public boolean loadConfig() throws UnsupportedEncodingException
    {
        alisa.json.Parser parser = new alisa.json.Parser();
        alisa.json.Object root = parser.load("config.json");
        if (root == null) { 
            this._error = "Error on reading config.json"; 
            return false; 
        }

        // version(String)
        this._version = getStringJson(root, "version");
        System.out.println("version = " + this._version);
        
        // port (Integer)
        this._port = getIntegerJson(root, "port");

        // timeout (Integer)
        this._timeout = getIntegerJson(root, "timeout");
    
        // max file size(Integer)        
        this._max_file_size = getIntegerJson(root, "max_file_size");
    
        if(this._version == null || this._max_file_size == -1 || this._port == -1 || this._timeout == -1){
            this._error = "Null value Attribute in Server.loadConfig()";
            return false;
        }
        
        //files
        alisa.json.Data files = root.findData("file");
        if (files == null || !files.isArray()){
            this._error = "Error on file"; 
            return false;
        }
        
        int files_length = files.getArray().countObjects();
        this._files = new ServerFile[files_length];
        
        for (int i = 0; i < this._files.length ; i++){
            alisa.json.Object obj = files.getArray().getObject(i);
            this._files[i] = new ServerFile(this); //don't for get to declare this!!
            if(!this._files[i].load(obj)){
                this._error = this._files[i].getError();
                return false;
            }
        }           
        return true;
    }
//------------------------------------------------------------------------------


//-----------------function get value from object---------------------
    public String getStringJson(alisa.json.Object obj, String name){
        alisa.json.Data text = obj.findData(name);
        if (text == null || !text.isString()){
            this._error = "Error on " + name; 
            return null;
        }
        return text.getString();
    }

    public int getIntegerJson(alisa.json.Object obj, String name){
        alisa.json.Data text = obj.findData(name);
        if (text == null || !text.isInteger()){
            this._error = "Error on" + name; 
            return -1;
        }
        return text.getInteger();
    }    
//--------------------------------------------------------------------
}

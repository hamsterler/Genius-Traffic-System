package file_sync_client_2.pkg0;

import java.io.UnsupportedEncodingException;

public class Client
{        
        
//----------------Attribute----------------
    private String _error = "";
    private String _version = "";
    private String _address = "";
    private int _port = -1;
    private int _timeout = -1;
    private ClientFileThread[] _files = null;
//-----------------------------------------


//--------------Get Method------------------
    public String getError(){
        return this._error;
    } 
    public String getVersion(){
        return this._version;
    } 
    public String getAddress(){
        return this._address;
    } 
    public int getPort(){
        return this._port;
    } 
    public int getTimeout(){
        return this._timeout;
    } 
//------------------------------------------


//---------------------------------------------Load Config.json-----------------------------------------
    public boolean loadConfig() throws UnsupportedEncodingException
    {
        alisa.json.Parser parser = new alisa.json.Parser();
        alisa.json.Object root = parser.load("config.json");
        if (root == null) { 
            this._error = "Error on reading config.json file."; 
            return false; 
        }

        // version(String)
        this._version = getStringJson(root, "version");
        
        // address (String)        
        this._address = getStringJson(root, "address");
        
        // port (Integer)
        this._port = getIntegerJson(root, "port");

        // timeout (Integer)
        this._timeout = getIntegerJson(root, "timeout");
        
        if(this._version == null || this._address == null || this._port == -1 || this._timeout == -1){
            this._error = "Null value Attribute in Client.loadConfig()";
            return false;
        }
                     
        //files
        alisa.json.Data files = root.findData("file");
        if (files == null || !files.isArray()){
            this._error = "Error on file."; 
            return false;
        }
        int files_length = files.getArray().countObjects();
        this._files = new ClientFileThread[files_length];
        
        for (int i = 0; i < this._files.length ; i++){
            alisa.json.Object obj = files.getArray().getObject(i);
            this._files[i] = new ClientFileThread(this); //don't for get to declare this!!
            if(!this._files[i].load(obj)){
                this._error = this._files[i].getError();
                return false;
            }
        }
            
        return true;
    }
//-------------------------------------------------------------------------------------------------------


//----------------Function Start-----------------
public void start(){   
    for(int i = 0; i < this._files.length; i++){
        this._files[i].start();
    }
}
//-----------------------------------------------


//-------------------isAllThreadFinish-----------------------
public boolean isAllThreadFinish(){
    for(int i = 0; i < this._files.length; i++){
        if(!this._files[i].isFinish)
            return false;
    }
    return true;
}
//-----------------------------------------------------------

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

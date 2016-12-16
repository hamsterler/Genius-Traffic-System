package tofgui_v2;

public class Line {
    private char _id;
    private int _lane;    
    private int _min;
    private int _max;
    public int distance;
    public boolean detected;
    public int firstDetectRow;
    public int lastDetectRow;
    public int detect_group_num;       
    public int max_distance;
    private boolean _isEnable;
    
    
    public Line(char id){
        this._id = id;
        this._min = -1;
        this._max = -1;
        this.firstDetectRow = -1;
        this.lastDetectRow = -1;
        this.detected = false;
        this.distance = 0;
        this.detect_group_num = -1;
        this.max_distance = 0;
        this._isEnable = false;
    }

    
    //---------------Getter---------------
    public char getId(){
        return this._id;
    }
    public int getLaneId(){
        return this._lane;
    }
    public int getMin(){
        return this._min;
    }
    public int getMax(){
        return this._max;
    }
    public boolean isEnable(){
        return this._isEnable;
    }
    //------------------------------------
    
    //---------------Setter---------------
    public boolean setId(char id){
        this._id = id;
        return true;
    }
    public boolean setLane(int lane){
        this._lane = lane;
        return true;
    }
    public boolean setMinMax(int min, int max){
        this._min = min;
        this._max = max;
        this._isEnable = true;
        return true;
    }
    
    //------------------------------------
    
    
    public boolean detect(){
        if(this.distance >= this._min && this.distance <= this._max)
            return true;
        return false;
    }
}

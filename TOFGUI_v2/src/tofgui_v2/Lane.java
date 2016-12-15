package tofgui_v2;

public class Lane {
    private int _id;
//    private String _line;
    private int _car_count;
    private boolean _detected;
    private int line_num;
    private int[] _line;
    private char[] _line_id;
    public int most_left_line;
    public int most_right_line;
    public Lane(int id){
        this._id = id;
//        this._line = line;
        this._car_count = 0;
        this._detected = false;
        this.line_num = 0;
    }
    
    private boolean _find_most_left_line(){
        int x = 1000;
        for(int i = 0; i < this._line.length; i++){
            if(this._line[i] < x)
                x = this._line[i];
        }
        this.most_left_line = x;
        return true;
    }
    private boolean _find_most_right_line(){
        int x = 0;
        for(int i = 0; i < this._line.length; i++){
            if(this._line[i] > x)
                x = this._line[i];
        }
        this.most_right_line = x;
        return true;
    }
    
    public boolean addCarcount(int num){
        this._car_count += num;
        return true;
    }
    public int getCarcount(){
        return this._car_count;
    }
    public int getLineNum(){
        return this._line.length;
    }
    public char[] getAllLineId(){
        return this._line_id;
    }
    public boolean setLine(int[] line){
        this._line = line;
        _find_most_left_line();
        _find_most_right_line();
        return true;
    }
    public int getId(){
        return this._id;
    }
    
}

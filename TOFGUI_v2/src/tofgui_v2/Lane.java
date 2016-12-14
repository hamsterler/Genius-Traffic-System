package tofgui_v2;

public class Lane {
    private int _id;
//    private String _line;
    private int _car_count;
    private boolean _detected;
    private int line_num;
    private char[] _line;
    public Lane(int id){
        this._id = id;
//        this._line = line;
        this._car_count = 0;
        this._detected = false;
        this.line_num = 0;
    }
    public char[] getLine(){
        return this._line;
    }
    public boolean setLine(char[] line){
        this._line = line;
        return true;
    }
    public int getId(){
        return this._id;
    }
}

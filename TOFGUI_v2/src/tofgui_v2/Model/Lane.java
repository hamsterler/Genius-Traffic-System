package tofgui_v2.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lane {
    private int _id;
    private int _car_count;
    private boolean _detected;
    private int line_num;
    private int _most_left_line;
    private int _most_right_line;
    private List<Integer> _line = new ArrayList<Integer>();
    public int last_used = 0;

    public int time = 0;
    public Lane(int id){
        this._id = id;
        this._car_count = 0;
        this._detected = false;
        this.line_num = 0;
    }
    
    public int get_most_left_line(){
        return this._most_left_line;
    }
    
    public int get_most_right_line(){
        return this._most_right_line;
    }
    public void set_most_left_line(int line){
        this._most_left_line = line;
    }
    public void set_most_right_line(int line){
        this._most_right_line = line;
    }
    
    public boolean resetCarcount(){
        this._car_count = 0;
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
        return this._line.size();
    }

    public boolean setLine(int[] line){
        for(int i = 0; i < line.length; i++){
            this._line.add(line[i]);
        }
        Collections.sort(this._line);
        _most_left_line = this._line.get(0);
        _most_right_line = this._line.get(this._line.size() - 1);
        return true;
    }
    public int getId(){
        return this._id;
    }
    
}

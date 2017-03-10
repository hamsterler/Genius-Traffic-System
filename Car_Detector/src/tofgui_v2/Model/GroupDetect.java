package tofgui_v2.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GroupDetect {
    public int first_row = 0;
    public int end_row = -1;
    private int _line_num = 0;
    public int line_num_check = 0;
    public int status = -1; //0 = count process, -1 = not in use, 1 = already in use
    private List<Integer> _line = new ArrayList<Integer>();
    
    public GroupDetect(){
        this.first_row = -1;
        this.end_row = -1;
        this._line_num = 0;
        this.line_num_check = 0;
        this.status = -1;
    }
    
    public GroupDetect(int first_row){
        this.first_row = first_row;
        this.end_row = -1;
        this._line_num = 0;
        this.line_num_check = 0;
        this.status = 1;
    }
    public GroupDetect(int first_row, int end_row, int line_num){
        this.first_row = first_row;
        this.end_row = end_row;
        this._line_num = line_num;
        this.line_num_check = 0;
        this.status = 1;
    }
    
    public GroupDetect(int first_row, int line_num){
        this.first_row = first_row;
        this.end_row = -1;
        this._line_num = line_num;
        this.line_num_check = 0;
        this.status = 1;
    }
    
    public boolean isMember(int line){
        if(this._line.indexOf(line) != -1)
            return true;
        return false;
    }
    
    public int get_line_num(){
        return this._line_num;
    }
    
    public boolean sortLine(){
        Collections.sort(this._line);
        return true;
    }
    
    public Integer[] getLine(){
//        int[] line = this._line.toArray();
        return this._line.toArray(new Integer[this._line.size()]);
    }
    
    public int get_most_left_line(){
        if(this._line.size() > 0)
            return this._line.get(0);
        return -1;
    }
    
    public int get_most_right_line(){
        return this._line.get(this._line.size() - 1);
    }
    
    public boolean addLine(int line){
        if(this._line.indexOf(line) != -1)
            return false;
        this._line.add(line);
        Collections.sort(this._line);
        _line_num = this._line.size();
        return true;
    }
    
    public boolean removeLine(int line){
        this._line.remove((Object)line);
        this._line_num = this._line.size();
        return true;
    }
    
    public int carSeperate(Lines lines, Lane[] lane){
        int count = 0;
        int[] lane_count = new int[100];
        for(int i = 0; i < lane.length; i++)
            lane_count[lane[i].getId()] = 0;
        int x = 0;
        
        //1. incase -> .|||||...||||.
        for(int i = 0; i < this._line.size(); i++){
            if(lines.line[(int)this._line.get(i)].getLaneId() != -1)
                lane_count[lines.line[(int)this._line.get(i)].getLaneId()]++;
        }    
//            if(i < this._line.size() - 1 && (int)this._line.get(i + 1) - (int)this._line.get(i) > 1){
//                x = i + 1 - x;
//                count++;
//                for(int j = 0; j < lane.length; j++){
//                   
//                    //in case number of detected line > 2/3 of number of lane's line 
//                    if(lane_count[lane[j].getId()] == lane[j].getLineNum() || lane_count[lane[j].getId()] >= (double)((double)2/(double)3)*(double)lane[j].getLineNum()){
//                        lane[j].addCarcount(1);
//                        lane_count[lane[j].getId()] = 0;
//                    }
//                    
//                    //in case |...II...|
////                            |...II...|
////                            |...II...|       
//                    else if(lane_count[lane[j].getId()] < lane[j].getLineNum() && x == lane_count[lane[j].getId()] && x != 0){
//                        lane[j].addCarcount(1);
//                        lane_count[lane[j].getId()] = 0;                    
//                    }
////                    lane_count[lane[j].getId()] = 0;
//                }
            
                for(int j = 0; j < lane.length; j++){
                    if(lane_count[lane[j].getId()] == lane[j].getLineNum() || lane_count[lane[j].getId()] >= (double)((double)2/(double)3)*(double)lane[j].getLineNum()){
                        lane[j].addCarcount(1);
                        lane_count[lane[j].getId()] = 0;
                    }else if(lane_count[lane[j].getId()] == this._line.size()){
                        lane[j].addCarcount(1);
                        lane_count[lane[j].getId()] = 0;
                    }else if( j < lane.length - 1 && lane_count[lane[j].getId()] > 0 && lane_count[lane[j + 1].getId()] > 0){
                        //case |..II|I...|
//                             |..II|I...|
//                             |..II|I...|
                        if(lane_count[lane[j].getId()] > lane_count[lane[j + 1].getId()])
                            lane[j].addCarcount(1);
                        //case |...I|II..|
//                             |...I|II..|
//                             |...I|II..|
                        else if(lane_count[lane[j].getId()] < lane_count[lane[j + 1].getId()])
                            lane[j + 1].addCarcount(1);
                        //case |..II|II..|
//                             |..II|II..|
//                             |..II|II..|
                        else if(lane_count[lane[j].getId()] == lane_count[lane[j + 1].getId()])   
                            lane[j].addCarcount(1);
                        lane_count[lane[j].getId()] = 0;
                        lane_count[lane[j + 1].getId()] = 0;
                    }
                }
//            }   
            
//        }
        
        return count + 1;
    }
    
}

package tofgui_v2;

import java.util.ArrayList;
import java.util.List;


public class GroupDetect {
    public int first_row = 0;
    public int end_row = -1;
    public int line_num = 0;
    public int line_num_check = 0;
    public int status = -1; //0 = count process, -1 = not in use, 1 = already in use
//    private int[] _line = new int[16];
    private List<Integer> _line = new ArrayList<Integer>();
    public GroupDetect(){
        this.first_row = -1;
        this.end_row = -1;
        this.line_num = 0;
        this.line_num_check = 0;
        this.status = -1;
    }
    
    public GroupDetect(int first_row){
        this.first_row = first_row;
        this.end_row = -1;
        this.line_num = 0;
        this.line_num_check = 0;
        this.status = 1;
    }
    public GroupDetect(int first_row, int line_num){
        this.first_row = first_row;
        this.end_row = -1;
        this.line_num = line_num;
        this.line_num_check = 0;
        this.status = 1;
    }
 
    public Integer[] getLine(){
//        int[] line = this._line.toArray();
        return this._line.toArray(new Integer[this._line.size()]);
    }
    
    public boolean addLine(int line){
        if(this._line.indexOf(line) != -1)
            return false;
        this._line.add(line);
        line_num = this._line.size();
        return true;
    }
    
    public boolean removeLine(int line){
        this._line.remove((Object)line);
        this.line_num = this._line.size();
        return true;
    }
    
    public boolean deleteGroupDetect(){
        this.first_row = -1;
        this.end_row = -1;
        this.line_num = 0;
        this.line_num_check = 0;
        this.status = -1;
        this._line.clear();
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
            
            if(i < this._line.size() - 1 && (int)this._line.get(i + 1) - (int)this._line.get(i) > 1){
                x = i + 1 - x;
                count++;
                for(int j = 0; j < lane.length; j++){
                   
                    //in case number of detected line > 2/3 of number of lane's line 
                    if(lane_count[lane[j].getId()] == lane[j].getLineNum() || lane_count[lane[j].getId()] >= (double)((double)2/(double)3)*(double)lane[j].getLineNum()){
                        lane[j].addCarcount(1);
                        lane_count[lane[j].getId()] = 0;
                    }
                    
                    //in case |...II...|
//                            |...II...|
//                            |...II...|       
                    else if(lane_count[lane[j].getId()] < lane[j].getLineNum() && x == lane_count[lane[j].getId()] && x != 0){
                        lane[j].addCarcount(1);
                        lane_count[lane[j].getId()] = 0;                    
                    }
//                    lane_count[lane[j].getId()] = 0;
                }
                
                for(int j = 0; j < lane.length - 1; j++){
                    if(lane_count[lane[j].getId()] > 0 && lane_count[lane[j + 1].getId()] > 0){
                        //case |..II|I...|
//                             |..II|I...|
//                             |..II|I...|
                        if(lane_count[lane[j].getId()] > lane_count[lane[j + 1].getId()])
                            lane[j].addCarcount(j);
                        //case |...I|II..|
//                             |...I|II..|
//                             |...I|II..|
                        else if(lane_count[lane[j].getId()] < lane_count[lane[j + 1].getId()])
                            lane[j + 1].addCarcount(j);
                        //case |..II|II..|
//                             |..II|II..|
//                             |..II|II..|
                        else if(lane_count[lane[j].getId()] == lane_count[lane[j + 1].getId()])   
                            lane[j].addCarcount(j);
                        
                        lane_count[lane[j].getId()] = 0;
                        lane_count[lane[j + 1].getId()] = 0;
                    }
                }
                
            }
        }
        
        for(int j = 0; j < lane.length; j++){
            if(lane_count[lane[j].getId()] == lane[j].getLineNum() || lane_count[lane[j].getId()] >= (double)((double)2/(double)3)*(double)lane[j].getLineNum()){
                lane[j].addCarcount(1);
                lane_count[lane[j].getId()] = 0;
            }
            else if(lane_count[lane[j].getId()] < lane[j].getLineNum() && this.line_num == lane_count[lane[j].getId()]){
                lane[j].addCarcount(1);
                lane_count[lane[j].getId()] = 0;                    
            }
        }
        return count + 1;
    }
    
}

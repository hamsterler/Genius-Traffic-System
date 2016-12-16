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
    
    public GroupDetect(int first_row, int end_row, int line_index){
        this.first_row = first_row;
        this.end_row = end_row;
//        this.line_num = 1;
        this.line_num_check = 0;
        this.status = 1;
    }
    
//    public int[] getLine(){
//        return this._line;
//    }
//    public boolean addLine(int line){
//        for(int i = 0; i < this.line_num; i++){
//            //test
//            if(line == this._line[i])
//                return false;
//            if(line < this._line[i]){
//                for(int j = this.line_num; j > i; j--){
//                    this._line[j] = this._line[j - 1];
//                }
//                this._line[i] = line;
//                if(this.line_num < 16)
//                    this.line_num++;
//                return true;
//            }
//        }
//        this._line[this.line_num] = line;
//        if(this.line_num < 16)
//            this.line_num++;
//        return true;
//    }
//    
//    public boolean removeLine(int line){
//        if(this.line_num < 0)
//            return false;
//        for(int i = 0; i < this.line_num; i++){
//            if(line == this._line[i]){
//                for(int j = i; j < this.line_num; j++){
//                    this._line[j] = this._line[j + 1];
//                }
//                this._line[this.line_num] = 0;
//                this.line_num--;
//                return true;
//            }
//        }
//        this.line_num--;
//        return true;
//    }
//    public int carSeperate(Lines lines, Lane[] lane){
//        int count = 0;
//        int[] lane_count = new int[100];
//        for(int i = 0; i < lane.length; i++)
//            lane_count[lane[i].getId()] = 0;
//        int x = 0;
//        for(int i = 0; i < this.line_num ; i++){
//            if(lines.line[this._line[i]].getLaneId() != -1)
//                lane_count[lines.line[this._line[i]].getLaneId()]++;
//            if(i < this.line_num - 1 && this._line[i + 1] - this._line[i] > 1){
//                x = i + 1 - x;
//                count++;
//                for(int j = 0; j < lane.length; j++){
//                    if(lane_count[lane[j].getId()] == lane[j].getLineNum() || lane_count[lane[j].getId()] >= (double)((double)2/(double)3)*(double)lane[j].getLineNum()){
//                        lane[j].addCarcount(1);
////                        lane_count[lane[j].getId()] = 0;
//                    }
//                    else if(lane_count[lane[j].getId()] < lane[j].getLineNum() && x == lane_count[lane[j].getId()]){
//                        lane[j].addCarcount(1);
////                        lane_count[lane[j].getId()] = 0;                    
//                    }
//                    lane_count[lane[j].getId()] = 0;
//                }
//            }
//        }
//        for(int j = 0; j < lane.length; j++){
//            if(lane_count[lane[j].getId()] == lane[j].getLineNum() || lane_count[lane[j].getId()] >= (double)((double)2/(double)3)*(double)lane[j].getLineNum()){
//                lane[j].addCarcount(1);
//                lane_count[lane[j].getId()] = 0;
//            }
//            else if(lane_count[lane[j].getId()] < lane[j].getLineNum() && this.line_num == lane_count[lane[j].getId()]){
//                lane[j].addCarcount(1);
//                lane_count[lane[j].getId()] = 0;                    
//            }
//        }
//        return count + 1;
//    }
//    public LinkedList getLine(){
//        return this._line;
//    }
    
    public Integer[] getLine(){
//        int[] line = this._line.toArray();
        return this._line.toArray(new Integer[this._line.size()]);
    }
    
    public boolean addLine(int line){
        this._line.add(line);
        line_num = this._line.size();
        return true;
    }
    
    public boolean removeLine(int line){
        this._line.remove(line);
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
        for(int i = 0; i < this._line.size(); i++){
            if(lines.line[(int)this._line.get(i)].getLaneId() != -1)
                lane_count[lines.line[(int)this._line.get(i)].getLaneId()]++;
            if(i < this._line.size() - 1 && (int)this._line.get(i + 1) - (int)this._line.get(i) > 1){
                x = i + 1 - x;
                count++;
                for(int j = 0; j < lane.length; j++){
                    if(lane_count[lane[j].getId()] == lane[j].getLineNum() || lane_count[lane[j].getId()] >= (double)((double)2/(double)3)*(double)lane[j].getLineNum()){
                        lane[j].addCarcount(1);
//                        lane_count[lane[j].getId()] = 0;
                    }
                    else if(lane_count[lane[j].getId()] < lane[j].getLineNum() && x == lane_count[lane[j].getId()]){
                        lane[j].addCarcount(1);
//                        lane_count[lane[j].getId()] = 0;                    
                    }
                    lane_count[lane[j].getId()] = 0;
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

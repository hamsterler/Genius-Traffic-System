package tofgui_v2.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoLaneGenerate {
//    public ArrayList<GroupDetect> group_detect = new ArrayList<GroupDetect>();
    private ArrayList<Lane> _lane = new ArrayList<Lane>(); 
    private int _time = 0;
    int[] lane = new int[3];  // 0=left, 1=right, 2=count
    List<int[]> lanes = new ArrayList<int[]>();
    public AutoLaneGenerate(){
//        this.group_detect = group_detect;
        _lane = new ArrayList<Lane>(); 
        _time = 0;
    }
    
    public ArrayList<Lane> getLane(){
        return this._lane;
    }
    
    public void autoGenLane(ArrayList<GroupDetect> group_detect){
        _time = _time % 10000;
        for(int i = 0; i< group_detect.size(); i++){
            if(group_detect.get(i).status == 0 && group_detect.get(i).get_line_num() > 1 && group_detect.get(i).get_line_num() < 8){
                int left = group_detect.get(i).get_most_left_line();
                int right = group_detect.get(i).get_most_right_line();
                double center = (double)(left + right)/(double)2;
                boolean create = true;
                for(int j = 0; j < this._lane.size(); j++) {
                    if(this._time - this._lane.get(j).last_used > 30)
                        this._lane.remove(j);
                    
                    if(center > this._lane.get(j).get_most_left_line() && center < this._lane.get(j).get_most_right_line()){
                        create = false;
                        if(left < this._lane.get(j).get_most_left_line() && right < this._lane.get(j).get_most_right_line()){
                            this._lane.get(j).set_most_left_line(left);
                            this._lane.get(j).last_used = this._time;
                        }else if(left > this._lane.get(j).get_most_left_line() && right > this._lane.get(j).get_most_right_line()){
                            this._lane.get(j).set_most_right_line(right);
                            this._lane.get(j).last_used = this._time;
                        }
                    }
                    
                    if(left < this._lane.get(j).get_most_left_line() && right > this._lane.get(j).get_most_right_line()){
                    create = false;
                        this._lane.get(j).set_most_left_line(left);
                        this._lane.get(j).set_most_right_line(right);
                        this._lane.get(j).last_used = this._time;
                    }
                    
                    if(create){
                        Lane a = new Lane(this._lane.size());
                        a.set_most_left_line(left);
                        a.set_most_right_line(right);
                        a.last_used = this._time;
                        this._lane.add(a);
                    }
                }
                if(this._time > 10000)
                    this._time = 0;
                this._time++;
            }
        }
        for (int i = 0; i < this._lane.size(); i++) {
            System.out.println("Lane" + i + ":  left = " + this._lane.get(i).get_most_left_line() + "      right = " + this._lane.get(i).get_most_right_line());
        }
        
    }
    
    public void autoGenLane2(GroupDetect group_detect ){
        _time = _time % 10000;
        if(group_detect.status == 0 && group_detect.get_line_num() > 1 && group_detect.get_line_num() < 8){
            int left = group_detect.get_most_left_line();
            int right = group_detect.get_most_right_line();
            double center = (double)(left + right)/(double)2;
            boolean create = true;
            for(int j = 0; j < this._lane.size(); j++) {
                if(this._time - this._lane.get(j).last_used > 30)
                        this._lane.remove(j);
                
                if(center >= this._lane.get(j).get_most_left_line() && center <= this._lane.get(j).get_most_right_line()){
                    create = false;
                    if(left < this._lane.get(j).get_most_left_line() && right <= this._lane.get(j).get_most_right_line()){
                        this._lane.get(j).set_most_left_line(left);
                        this._lane.get(j).last_used = this._time;
                    }else if(left >= this._lane.get(j).get_most_left_line() && right > this._lane.get(j).get_most_right_line()){
                        this._lane.get(j).set_most_right_line(right);
                        this._lane.get(j).last_used = this._time;
                    }
                }

                if(left < this._lane.get(j).get_most_left_line() && right > this._lane.get(j).get_most_right_line()){
                    create = false;
                    this._lane.get(j).set_most_left_line(left);
                    this._lane.get(j).set_most_right_line(right);
                    this._lane.get(j).last_used = this._time;
                }
            }
            if(create){
                Lane a = new Lane(this._lane.size());
                a.set_most_left_line(left);
                a.set_most_right_line(right);
                a.last_used = this._time;
                this._lane.add(a);
            }
            if(this._time > 10000)
                this._time = 0;
            this._time++;
        }
        for (int i = 0; i < this._lane.size(); i++) {
            System.out.println("Lane" + i + ":  left = " + this._lane.get(i).get_most_left_line() + "      right = " + this._lane.get(i).get_most_right_line());
        }
        
    }
    
    public void finalGenerate(){
        int count = 0;
        for (int i = 0; i < this.lanes.size(); i++)
            count += this.lanes.get(i)[2];
        
        int deadLineCount = (int)((double)count/(double)this.lanes.size());
        
        for (int i = 0; i < this.lanes.size(); i++)
            if(this.lanes.get(i)[2] < deadLineCount)
                this.lanes.remove(i);
    }
    
    public boolean collectLaneExample(int left, int right){
        try{
            if(this.lanes.size() > 0){
                for (int i = 0; i < this.lanes.size(); i++) {
                    if(left == this.lanes.get(i)[0] && right == this.lanes.get(i)[1]){
                        this.lanes.get(i)[2]++;
                        return true;
                    }
                }
            }
            int[] a = new int[3];
            a[0] = left;
            a[1] = right;
            a[2] = 1;
            this.lanes.add(a);
        }catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}

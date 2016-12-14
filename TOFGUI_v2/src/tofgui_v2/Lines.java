package tofgui_v2;

public class Lines {
    public Line[] line;
    
    public Lines(int line_num){
        line = new Line[line_num];
    }
    
    public int length(){
        return this.line.length;
    }
     
    public int findById(String id){
        for(int i = 0; i < this.line.length; i++){
            if(id.equals(this.line[i].getId()))
                return i;
        }
        return -1;
    }
}

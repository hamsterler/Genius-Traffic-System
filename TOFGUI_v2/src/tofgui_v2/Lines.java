package tofgui_v2;

public class Lines {
    public Line[] line;
    
    public Lines(){
        line = new Line[16];
        line[0] = new Line('A');
        line[1] = new Line('B');
        line[2] = new Line('C');
        line[3] = new Line('D');
        line[4] = new Line('E');
        line[5] = new Line('F');
        line[6] = new Line('G');
        line[7] = new Line('H');
        line[8] = new Line('I');
        line[9] = new Line('J');
        line[10] = new Line('K');
        line[11] = new Line('L');
        line[12] = new Line('M');
        line[13] = new Line('N');
        line[14] = new Line('O');
        line[15] = new Line('P');
    }
    public Lines(int line_num){
        line = new Line[line_num];
    }
    
    public int length(){
        return this.line.length;
    }
     
    public int findById(String id){
        for(int i = 0; i < this.line.length; i++){
            if(id.equals(this.line[i].getId() + ""))
                return i;
        }
        return -1;
    }
    
    public boolean resetMaxDistance(){
        for(int i = 0; i < this.line.length; i++)
            this.line[i].max_distance = 0;
        return true;
    }
}

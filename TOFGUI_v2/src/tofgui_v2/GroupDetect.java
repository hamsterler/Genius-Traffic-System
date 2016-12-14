package tofgui_v2;

public class GroupDetect {
    public int first_row = 0;
    public int end_row = -1;
    public int line_num = 0;
    public int line_num_check = 0;
    public int status = -1; //0 = count process, -1 = not in use, 1 = already in use
    private int[] _line = new int[16];
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
        this.line_num = 1;
        this.line_num_check = 0;
        this.status = 1;
    }
    
    public int[] getLine(){
        return this._line;
    }
    
    public boolean addLine(int line){
        for(int i = 0; i < this.line_num; i++){
            //test
            if(line == this._line[i])
                return false;
            if(line < this._line[i]){
                for(int j = this.line_num; j > i; j--){
                    this._line[j] = this._line[j - 1];
                }
                this._line[i] = line;
                this.line_num++;
                return true;
            }
        }
        this._line[this.line_num] = line;
        this.line_num++;
        return true;
    }
    
    public boolean removeLine(int line){
        for(int i = 0; i < this.line_num; i++){
            if(line == this._line[i]){
                for(int j = i; j < this.line_num; j++){
                    this._line[j] = this._line[j + 1];
                }
                this._line[this.line_num] = 0;
                this.line_num--;
                return true;
            }
        }
        this._line[this.line_num] = line;
        this.line_num--;
        return true;
    }
            
    
    public int carSeperate(){
        int count = 0;
        for(int i = 0; i < this.line_num - 1; i++){
            if(this._line[i + 1] - this._line[i] > 1)
                count++;
        }
        return count + 1;
    }
    
}

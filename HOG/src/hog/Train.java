package hog;

public class Train {
    public String name;
    int[][][] block;
    double distance;
    public Train(){
        this.name = "";
        this.distance = 0;
    }
    public Train(int[][][] block, String name){
        this.block = block;
        this.name = name;
    }
}

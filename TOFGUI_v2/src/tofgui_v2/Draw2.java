package tofgui_v2;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;


public class Draw2 {
    public int high;
    public int width;
    public int max_line_length;
    public int line_num;
    private final Canvas _canvas;
    private GraphicsContext _gc;
    private String _error = "";
    
    public String getError(){
        return this._error;
    }
    

    public Draw2(Canvas canvas, int line_num){
        this.high = (int)canvas.getHeight();
        this.width = (int)canvas.getWidth();
        this.max_line_length = max_line_length;
        this.line_num = line_num;
        this._canvas =  canvas;
        _gc = this._canvas.getGraphicsContext2D();    
        _gc.setFill(Paint.valueOf("#ffffff"));
        _gc.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
    }
    
    public boolean draw(int[][] detected){
        double xGap = this.width /(double)(detected[0].length + 1); 
        double yGap = this.high/(double)(detected.length + 1);
        try{
            for(int i = 0; i < detected[0].length; i++){
                _gc.setStroke(Color.FORESTGREEN.brighter());
                _gc.setLineWidth(1);
                _gc.beginPath();
                _gc.moveTo((i + 1) * xGap, setY(0));
                _gc.lineTo((i + 1) * xGap, setY(this.high));          
                _gc.stroke();
            }
            for(int i = 0; i < detected.length; i++){
                double y = (detected.length - i) * yGap;
                for(int j = 0; j < detected[i].length; j++){
                    double x = (j + 1)*xGap;
                    if(detected[i][j] != 0){
                        _gc.setStroke(Paint.valueOf("#ff3333"));
                        _gc.setLineWidth(6);
                        _gc.beginPath();
                        _gc.moveTo(x, setY(y - yGap/2));
                        _gc.lineTo(x, setY(y + yGap/2));          
                        _gc.stroke();
                    }
                }
            }
        }catch(Exception ex){
//            this._error = "Draw | draw(): " + ex.getMessage() ;    
            ex.printStackTrace();
            return false;
        }       
        return true;
    }
    
    
    public double setX(double x){
        return (-x) + ((double)this.width/(double)2);
    }
    public double setY(double y){
        return (double)this.high - y;
    }
    
    public Canvas getCanvas(){
        return this._canvas;
    }
    
    public void clearCanvas(){
        this._gc.clearRect(0, 0, this._canvas.getWidth(), this._canvas.getHeight());
    }
}

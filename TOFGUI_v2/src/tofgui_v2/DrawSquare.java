package tofgui_v2;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;


public class DrawSquare {
    public int high;
    public int width;
    public int line_num;
    private final Canvas _canvas;
//    private GraphicsContext _gc;
    private String _error = "";
    double xGap;
    double yGap;
    double xstart;
    double y1;
    double y2;
    public String getError(){
        return this._error;
    }
    

    public DrawSquare(Canvas canvas, int line_num){
        this.high = (int)canvas.getHeight();
        this.width = (int)canvas.getWidth();
        this.line_num = line_num;
        this._canvas =  canvas;
        
        this.xGap = this.width * ((double)4/(double)5) * ((double)1/(double)line_num); 
        this.yGap = this.high/(double)3;
        this.xstart = this.width/(double)10 - (5 * (line_num - 1)/(double)2);
        this.y1 = this.high/(double)5;
        this.y2 = ((double)8/(double)15) * this.high;
        
//        _gc = this._canvas.getGraphicsContext2D();   
//        _gc.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
    }
    
    public boolean drawLine(int index, boolean detected){
        try{
            GraphicsContext _gc = this._canvas.getGraphicsContext2D(); 
//            _gc.setLineDashes(0);
//            _gc.fillRect(0,0, this._canvas.getWidth(), this._canvas.getHeight());
            double x1 = this.xstart + index * this.xGap + (5 * index);
            double x2 = this.xstart + (index + 1) * this.xGap + (5 * index);
            if(detected)
                _gc.setStroke(Color.RED.brighter());
            else
                _gc.setStroke(Color.FORESTGREEN.brighter());
            
            _gc.setLineWidth(3);
            _gc.beginPath();
            _gc.moveTo(x1, y1);
            _gc.lineTo(x2, y1); 
            _gc.lineTo(x2, y2);
            _gc.lineTo(x1, y2); 
            _gc.lineTo(x1, y1);
            _gc.stroke();
            
        }catch(Exception ex){
//            this._error = "Draw | draw(): " + ex.getMessage() ; 
            ex.printStackTrace();
            return false;
        }       
        return true;
    }
    
    public boolean drawLane(Lines lines, Lane[] lane){
        try{
            GraphicsContext _gc = this._canvas.getGraphicsContext2D(); 
            _gc.setStroke(Paint.valueOf("#00bfff"));
//            _gc.setLineDashes(5);
            _gc.setLineWidth(0.8);
            for(int i = 0; i < lane.length; i++){
                int left = lane[i].most_left_line;
                int right = lane[i].most_right_line;
                double x1 = this.xstart + left * this.xGap + (5 * left);
                double x2 = this.xstart + (right + 1) * this.xGap + (5 * right);
                
                _gc.beginPath();
                _gc.moveTo(x1 + 8, y1 - yGap / 5);
                _gc.lineTo(x1 - 2, y1 - yGap / 5);
                _gc.lineTo(x1 - 2, y2 + yGap / 5 );
                _gc.lineTo(x1 + 8, y2 + yGap / 5 );

                _gc.moveTo(x2 - 8, y1 - yGap / 5);
                _gc.lineTo(x2 + 2, y1 - yGap / 5);
                _gc.lineTo(x2 + 2, y2 + yGap / 5);
                _gc.lineTo(x2 - 8, y2 + yGap / 5);
                _gc.stroke();
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
        this._canvas.getGraphicsContext2D().clearRect(0, 0, this._canvas.getWidth(), this._canvas.getHeight());
    }
}

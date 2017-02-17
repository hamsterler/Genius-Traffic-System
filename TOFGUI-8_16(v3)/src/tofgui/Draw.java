package tofgui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;


public class Draw {
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
    
    public Draw(int width, int high, int max_line_length, int line_num){
        this.high = high;
        this.width = width;
        this.max_line_length = max_line_length;
        this.line_num = line_num;
        this._canvas =  new Canvas(this.width,this.high);
        _gc = this._canvas.getGraphicsContext2D();       
    }
  
    public boolean draw_default(){
        try{
            for(int i = 0; i < this.line_num; i++){
                double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.line_num - 1));
                double y = this.max_line_length * Math.sin(angle);
                double x = this.max_line_length * Math.cos(angle);
                _gc.setStroke(Color.FORESTGREEN.brighter());
                _gc.setLineWidth(1);
                _gc.beginPath();
                _gc.moveTo(setX(0), setY(0));
                _gc.lineTo(setX(x), setY(y));          
                _gc.stroke();            
                
                //text
                double text_y = (this.max_line_length + 7) * Math.sin(angle);
                double text_x = (this.max_line_length + 7) * Math.cos(angle);
                _gc.setTextAlign(TextAlignment.CENTER);
                _gc.setFont(new Font(10));
                _gc.fillText(i + 1 + "",  setX(text_x), setY(text_y));
            }
        }catch(Exception ex){
            this._error = "Draw | draw(): " + ex.getMessage() ;    
            return false;
        }       
        return true;
    }
    
    public boolean drawDistancePoint( int[] distance, int[] max, int[] max_distance, Paint color){       
        try{
            for(int i =0; i < this.line_num; i++){
                double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.line_num-1));
                double scale = (double)max_line_length / (double)max_distance[i];
                int pointLength = 2;
                double endX = distance[i] * Math.cos(angle) * scale;
                double endY = distance[i] * Math.sin(angle) * scale;
                double startY =(distance[i] - pointLength ) * Math.sin(angle) * scale;
                double startX = (distance[i] - pointLength ) * Math.cos(angle) * scale;
                _gc.setStroke(color);
                _gc.setLineWidth(6);
                _gc.beginPath();
                _gc.moveTo(setX(startX), setY(startY));
                _gc.lineTo(setX(endX), setY(endY));          
                _gc.stroke();
            }
        }catch(Exception ex){
            this._error = "Draw | drawDistancePoint(): " + ex.getMessage() ;
            return false;
        }            
        return true;
    }
  
    public boolean drawMinMaxLine(int[] min, int[] max, int[] max_distance, Paint color, int line_size){       
        try{
            for(int i = 0;i < this.line_num; i++){
                double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.line_num - 1));
                double ymin = min[i] * Math.sin(angle) * ((double)max_line_length / (double)max_distance[i]);
                double xmin = min[i] * Math.cos(angle) * ((double)max_line_length / (double)max_distance[i]);
                double xmax = (double)max[i] * Math.cos(angle) * ((double)max_line_length / (double)max_distance[i]);
                double ymax =(double)max[i] * Math.sin(angle) * ((double)max_line_length / (double)max_distance[i]);
                _gc.setStroke(color);     
                _gc.setLineWidth(line_size);
                _gc.beginPath();
                _gc.moveTo(setX(xmin), setY(ymin));
                _gc.lineTo(setX(xmax), setY(ymax));          
                _gc.stroke();
            }
        }catch(Exception ex){
            this._error = "Draw | drawMinMaxLin(): " + ex.getMessage() ;
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

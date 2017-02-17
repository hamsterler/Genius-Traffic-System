package tofgui_v2.Draw;

import tofgui_v2.Model.Line;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;


public class DrawLine {
    public int high;
    public int width;
    public int max_line_length;
    public int line_num;
    private final Canvas _canvas;
//    private GraphicsContext _gc;
    private String _error = "";
    
    public String getError(){
        return this._error;
    }
    
    public DrawLine(int width, int high, int max_line_length, int line_num){
        this.high = high;
        this.width = width;
        this.max_line_length = max_line_length;
        this.line_num = line_num;
        this._canvas =  new Canvas(this.width,this.high);
//        _gc = this._canvas.getGraphicsContext2D();       
    }
    
    public DrawLine(Canvas canvas, int max_line_length, int line_num){
        this.high = (int)canvas.getHeight();
        this.width = (int)canvas.getWidth();
        this.max_line_length = max_line_length;
        this.line_num = line_num;
        this._canvas =  canvas;
//        _gc = this._canvas.getGraphicsContext2D();  
//        _gc.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
    }
    
    public boolean drawMinMaxLine(Line[] line, Paint color, int line_size){
        try{
            GraphicsContext _gc = this._canvas.getGraphicsContext2D();  
            for(int i = 0;i < this.line_num; i++){
                double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.line_num - 1));
                double ymin = line[i].getMin() * Math.sin(angle) * ((double)max_line_length / (double)line[i].max_distance);
                double xmin = line[i].getMin() * Math.cos(angle) * ((double)max_line_length / (double)line[i].max_distance);
                double xmax = line[i].getMax() * Math.cos(angle) * ((double)max_line_length / (double)line[i].max_distance);
                double ymax = line[i].getMax() * Math.sin(angle) * ((double)max_line_length / (double)line[i].max_distance);
                _gc.setStroke(color);     
                _gc.setLineWidth(line_size);
                _gc.beginPath();
                _gc.moveTo(setX(xmin), setY(ymin));
                _gc.lineTo(setX(xmax), setY(ymax));          
                _gc.stroke();
            }
        }catch(Exception ex){
//            this._error = "DrawLine | drawMinMaxLin(): " + ex.getMessage() ;
            ex.printStackTrace();
            return false;
        }        
        
        return true;
    }
    
    public boolean drawDistancePoint( Line[] line, Paint color){       
        try{
            GraphicsContext _gc = this._canvas.getGraphicsContext2D();
            for(int i =0; i < this.line_num; i++){
                double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.line_num-1));
                double scale = (double)max_line_length / (double)line[i].max_distance;
                int pointLength = 2;
                double endX = line[i].distance * Math.cos(angle) * scale;
                double endY = line[i].distance * Math.sin(angle) * scale;
                double startY =(line[i].distance - pointLength ) * Math.sin(angle) * scale;
                double startX = (line[i].distance - pointLength ) * Math.cos(angle) * scale;
                _gc.setStroke(color);
                _gc.setLineWidth(6);
                _gc.beginPath();
                _gc.moveTo(setX(startX), setY(startY));
                _gc.lineTo(setX(endX), setY(endY));          
                _gc.stroke();
            }
        }catch(Exception ex){
//            this._error = "DrawLine | drawDistancePoint(): " + ex.getMessage() ;
            ex.printStackTrace();
            return false;
        }            
        return true;
    }
    
    public boolean drawDefaultLine(Line[] line){
        try{
            GraphicsContext _gc = this._canvas.getGraphicsContext2D();
            for(int i = 0; i < line.length; i++){
                double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.line_num - 1));
                double y = this.max_line_length * Math.sin(angle);
                double x = this.max_line_length * Math.cos(angle);
                
                //text
                double text_y = (this.max_line_length + 5) * Math.sin(angle);
                double text_x = (this.max_line_length + 5) * Math.cos(angle);
                _gc.setTextAlign(TextAlignment.CENTER);
                _gc.setFont(new Font(10));
                _gc.fillText(line[i].getId() + "",  setX(text_x), setY(text_y));
                
                _gc.setStroke(Color.FORESTGREEN.brighter());   
                _gc.setLineWidth(1);
                _gc.beginPath();
                _gc.moveTo(setX(0), setY(0));
                _gc.lineTo(setX(x), setY(y));          
                _gc.stroke();            
            }
        }catch(Exception ex){
//            this._error = "DrawLine | draw(): " + ex.getMessage() ;   
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

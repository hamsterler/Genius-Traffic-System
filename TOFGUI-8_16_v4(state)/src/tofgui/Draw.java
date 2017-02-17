package tofgui;

import java.awt.Graphics2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;


public class Draw {
//    public int high;
//    public int width;
    public double max_line_length;
    public int line_num = 8;
    private final Canvas _canvas;
    private GraphicsContext _gc;
    private String _error = "";
    
    public int border_y = 10;
    
    public String getError(){
        return this._error;
    }
    
    public Draw(int width, int high, int max_line_length, int line_num){
//        this.high = high;
//        this.width = width;
        this.max_line_length = max_line_length;
        this.line_num = line_num;
        this._canvas =  new Canvas(width, high);
        _gc = this._canvas.getGraphicsContext2D();       
    }
  
    public boolean draw_default(){
        try{
            this.max_line_length = ((double)8.5/(double)10) * (double)this._gc.getCanvas().getHeight();
            for(int i = 0; i < this.line_num; i++){
                double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.line_num - 1));
                double y = border_y + this.max_line_length * Math.sin(angle);
                double x = this.max_line_length * Math.cos(angle);
                _gc.setStroke(Color.FORESTGREEN.brighter());
                _gc.setLineWidth(1);
                _gc.setLineDashes(10);
                _gc.beginPath();
                _gc.moveTo(setX(0), setY(border_y + 0));
                _gc.lineTo(setX(x), setY(y));          
                _gc.stroke();          
                
                //text
                double text_y = border_y + (this.max_line_length + 15) * Math.sin(angle);
                double text_x = (this.max_line_length + 15) * Math.cos(angle);
                _gc.setTextAlign(TextAlignment.CENTER);
                _gc.setFont(new Font(12));
                _gc.fillText( this.line_num - i + "",  setX(text_x), setY(text_y));
            }
        }catch(Exception ex){
            this._error = "Draw | draw(): " + ex.getMessage() ;    
            return false;
        }       
        return true;
    }
    
    public boolean drawDistancePoint( int[] distance, Paint color){       
        try{
//            this.max_line_length = (int)(((double)4/(double)5) * (double)this._gc.getCanvas().getHeight());
            for(int i =0; i < this.line_num; i++){
                double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.line_num-1));
                double scale = (double)max_line_length / (double)max_max;
                int pointLength = 3;
                double endX = distance[i] * Math.cos(angle) * scale;
                double endY = border_y + distance[i] * Math.sin(angle) * scale;
                double startY = border_y + (distance[i] - pointLength ) * Math.sin(angle) * scale;
                double startX = (distance[i] - pointLength ) * Math.cos(angle) * scale;
                //incase distance > max_max
                double out_of_bound_length = max_line_length + (double)this.max_line_length/(double)10;
                if(distance[i] > max_max){
                    startX = out_of_bound_length *  Math.cos(angle);
                    startY = border_y + out_of_bound_length * Math.sin(angle);
                    endX = (out_of_bound_length + pointLength) * Math.cos(angle);
                    endY = border_y + (out_of_bound_length + pointLength) * Math.sin(angle);
                }
                _gc.setStroke(color);
                _gc.setLineWidth(8);
                _gc.setLineDashes(0);
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
    
        public boolean drawAutoMinMax(int[] min,int[] max){       
        try{
//            this.max_line_length = (int)(((double)4/(double)5) * (double)this._gc.getCanvas().getHeight());
            for(int i = 0; i < this.line_num; i++){
                if(min[i] == 0 && max[i] == 0)
                    continue;
                
                double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.line_num-1));
                double scale = (double)max_line_length / (double)max_max;
                double dashLength = 10;
                double dash_x = Math.cos(angle + Math.PI/(double)2) * dashLength;
                double dash_y = Math.sin(angle + Math.PI/(double)2) * dashLength;
                double min_startY = border_y + min[i] * Math.sin(angle) * scale + dash_y;
                double min_startX = min[i] * Math.cos(angle) * scale + dash_x;
                double min_endX = min[i] * Math.cos(angle) * scale - dash_x;
                double min_endY = border_y + min[i] * Math.sin(angle) * scale - dash_y;
                
                double max_startY = border_y + max[i] * Math.sin(angle) * scale + dash_y;
                double max_startX = max[i] * Math.cos(angle) * scale + dash_x;
                double max_endX = max[i] * Math.cos(angle) * scale - dash_x;
                double max_endY = border_y + max[i] * Math.sin(angle) * scale - dash_y;
                
                //dash box
                _gc.setStroke(Color.valueOf("#F39C12"));
                _gc.setLineWidth(2);
                _gc.setLineDashes(4);
                _gc.beginPath();
                _gc.moveTo(setX(min_startX), setY(min_startY));
                _gc.lineTo(setX(min_endX), setY(min_endY)); 
                _gc.lineTo(setX(max_endX), setY(max_endY)); 
                _gc.lineTo(setX(max_startX), setY(max_startY)); 
                _gc.lineTo(setX(min_startX), setY(min_startY));
                _gc.stroke();
                
                //dash Line
//                _gc.setStroke(Color.valueOf("#7DCEA0"));
//                _gc.setLineWidth(2);
//                _gc.setLineDashes(0);
//                _gc.beginPath();
//                _gc.moveTo(setX(min_startX), setY(min_startY));
//                _gc.lineTo(setX(min_endX), setY(min_endY));   
//                _gc.stroke();
//                
//                _gc.setStroke(Color.valueOf("#F39C12"));
//                _gc.setLineWidth(2);
//                _gc.setLineDashes(0);
//                _gc.beginPath();
//                _gc.moveTo(setX(max_startX), setY(max_startY));
//                _gc.lineTo(setX(max_endX), setY(max_endY));  
//                _gc.stroke();
            }
        }catch(Exception ex){
            this._error = "Draw | drawDistancePoint(): " + ex.getMessage() ;
            return false;
        }            
        return true;
    }
    
    private int max_max = 0;
    public void updateMaxMax(int[] max){
        max_max = max[0];
        for(int i = 0;i < this.line_num; i++){
            if (max[i] > this.max_max)
                this.max_max = max[i];
        }
    }
    public boolean drawMinMaxLine(int[] min, int[] max, Paint color, int line_size){       
        try{
//            this.max_line_length = (int)(((double)4/(double)5) * (double)this._gc.getCanvas().getHeight());
            for(int i = 0;i < this.line_num; i++){
                double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.line_num - 1));
                double ymin = border_y + min[i] * Math.sin(angle) * ((double)max_line_length / (double)max_max);
                double xmin = min[i] * Math.cos(angle) * ((double)max_line_length / (double)max_max);
                double xmax = (double)max[i] * Math.cos(angle) * ((double)max_line_length / (double)max_max);
                double ymax = border_y + (double)max[i] * Math.sin(angle) * ((double)max_line_length / (double)max_max);
                _gc.setStroke(color);     
                _gc.setLineWidth(line_size);
                _gc.setLineDashes(0);
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
        return (x) + ((double)this._gc.getCanvas().getWidth()/(double)2);
    }
    public double setY(double y){
//        return (double)this.high - y;
        return y;
    }
    
    public Canvas getCanvas(){
        return this._canvas;
    }
    
    public void clearCanvas(){
        this._gc.clearRect(0, 0, this._canvas.getWidth(), this._canvas.getHeight());
    }
    
    public void resize(int width, int height)
    {
        Canvas canvas = this._gc.getCanvas();
        canvas.setLayoutX(10);
        canvas.setLayoutY(10);
        canvas.setWidth(width - 20);
        canvas.setHeight(height - 20);
    }
}

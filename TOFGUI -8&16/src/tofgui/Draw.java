/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tofgui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;


public class Draw {
    public int high;
    public int width;
    public int max_line_length;
    public int max_num_line;
    private final Canvas _canvas;
    private GraphicsContext _gc;
    public Draw(int width, int high, int max_line_length, int max_num_line){
        this.high = high;
        this.width = width;
        this.max_line_length = max_line_length;
        this.max_num_line = max_num_line;
        this._canvas =  new Canvas(this.width,this.high);
        _gc = this._canvas.getGraphicsContext2D();
    }
  
    public void draw(){
        for(int i = 0; i < this.max_num_line; i++){
            double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.max_num_line - 1));
            int y =(int)(this.max_line_length * Math.sin(angle));
            int x = (int)(this.max_line_length * Math.cos(angle));
            _gc.setStroke(Color.FORESTGREEN.brighter());
            _gc.setLineWidth(1);
            _gc.beginPath();
            _gc.moveTo(setX(0), setY(0));
            _gc.lineTo(setX(x), setY(y));          
            _gc.stroke();            
        }
    }
    
    public void drawDistancePoint(int max_num, int[] distance, int[] max, Paint color){       
        for(int i =0; i < max_num; i++){
            double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(max_num-1));
            double scale = (double)max_line_length / (double)max[i];
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
    }
    
    public void drawEachLine(int line_num,int max_num, int distance, int max_distance){       
        double angle = (Math.PI/(double)4) + line_num * Math.PI/(double)(2*(max_num-1));
        int line_length = 0;
        if(distance >= max_distance)
            line_length = this.max_line_length;
        else
            line_length = (int)(((double)distance/(double)max_distance) * this.max_line_length);
        int y =(int)(line_length * Math.sin(angle));
        int x = (int)(line_length * Math.cos(angle));
        _gc.setStroke(Color.RED.brighter());
        _gc.setLineWidth(5);
        _gc.beginPath();
        _gc.moveTo(setX(0), setY(0));
        _gc.lineTo(setX(x), setY(y));          
        _gc.stroke();            
    }
    
    public void drawMinMaxLine(int max_num, int[] min, int[] max, Paint color, int line_size){       
        
        for(int i =0;i<max_num; i++){
            double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(max_num-1));
            int ymin =(int)(min[i] * Math.sin(angle) * ((double)max_line_length / (double)max[i]));
            int xmin = (int)(min[i] * Math.cos(angle) * ((double)max_line_length / (double)max[i]));
            int xmax = (int)((double)max_line_length * Math.cos(angle));
            int ymax =(int)((double)max_line_length * Math.sin(angle));
            _gc.setStroke(color);     
            _gc.setLineWidth(line_size);
            _gc.beginPath();
            _gc.moveTo(setX(xmin), setY(ymin));
            _gc.lineTo(setX(xmax), setY(ymax));          
            _gc.stroke();
        }            
    }
    public double setX(double x){
        return (-x) + ((double)this.width/(double)2);
    }
    public double setY(double y){
        return (double)this.high - y;
    }
    
    public int setX(int x){
        return (-x) + (int)((double)this.width/(double)2);
    }
    public int setY(int y){
        return this.high - y;
    }
    
    public Canvas getCanvas(){
        return this._canvas;
    }
    
    public void clearCanvas(){
        this._gc.clearRect(0, 0, this._canvas.getWidth(), this._canvas.getHeight());
    }
}

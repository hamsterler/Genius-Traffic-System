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
  
    public synchronized void draw(){
        for(int i = 0; i < this.max_num_line; i++){
            double angle = (Math.PI/(double)4) + i * Math.PI/(double)(2*(this.max_num_line - 1));
            int y =(int)(this.max_line_length * Math.sin(angle));
            int x = (int)(this.max_line_length * Math.cos(angle));
            _gc.setStroke(Color.FORESTGREEN.brighter());
            _gc.setLineWidth(4);
            _gc.beginPath();
            _gc.moveTo(setX(0), setY(0));
            _gc.lineTo(setX(x), setY(y));          
            _gc.stroke();            
        }
    }
    
    public synchronized void drawEachLine(int line_num,int max_num, int distance, int max_distance){       
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

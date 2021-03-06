/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tofgui_v2;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tofgui_v2.Model.MJPEG;

/**
 *
 * @author admin
 */
public class main extends Application {

    private static String _excelFileName = "";
    private static HSSFSheet _sheet;
    private static HSSFWorkbook _wb;
    
    private Image _image = null;
    
    private AnchorPane _pane = null;
    private FXMLDocumentController _controller = null;
    
    private WebCam webcam = null;
    MJPEG _mjpeg = null;
    
    //private Webcam _webcam = null;
    static int webcam_id = 0;
    
    private int _last_ms = 0;
    private int _gc_count = 0;

//    private Serial _serial;
    private CPU _cpu;
    @Override
    public void start(Stage stage) throws Exception 
    {
        FXMLLoader fxmlLoader = new FXMLLoader();
        this._pane = fxmlLoader.load(getClass().getResource("FXMLDocument.fxml").openStream());
        this._controller = (FXMLDocumentController)fxmlLoader.getController();
        this._controller.ap.setStyle("-fx-background-color: #5D6D7E");
        
        Scene scene = new Scene(this._pane);        
        stage.setScene(scene);
        stage.show();
        stage.setTitle("WebCam Display");
        
        //serial webcam
//        this.webcam = new WebCam();
//        this.webcam.load(webcam_id);
//        this.webcam.start();
        
        //Ethernet webcam
        String url = "http://192.168.1.111/Streaming/channels/2/preview"; // HIKVISION
        String user = "admin";
        String pass = "geniusTR123";
        this._mjpeg = new MJPEG(url, user, pass);        
        this._mjpeg.start();
        //------------------
        
        String port = "COM8";
//        this._serial = new Serial(port, _controller);
        this._cpu = new CPU(_controller);
       
        _excelFileName = "D:/Test7.xls";//name of excel file
	String sheetName = "Sheet1";//name of sheet
	_wb = new HSSFWorkbook();
	_sheet = _wb.createSheet(sheetName) ;
        
        //_resize();
        _paint2(); 
        AnimationTimer timer = new AnimationTimer() 
        {
            @Override
            public void handle(long now) 
            {
               count = now;
               _paint2();
            }
        };
        timer.start();
        
        //---------------------------set button action------------------------------
        //pause
        this._controller.pauseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                 if(!_cpu.pauseStatus()){
                     _cpu.pause();
                     _controller.pauseButton.setText("Resume");
                 }else{
                     _cpu.unpause();
                     _controller.pauseButton.setText("Pause");
                 }
            }
        });
        
        //reset
        this._controller.resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                 _cpu.resetLinesMaxDistance();
            }
        });
        
        //countReset
        this._controller.countResetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                 _cpu.coutReset();
            }
        });
        
        //loadConfig
        this._controller.loadConfigButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                _cpu.readInputConfig();   
                _cpu.readOutputConfig();
//                _cpu.readConfig();
            }
        });
        
         // Stop when window is closing
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() 
        {
            @Override
            public void handle(WindowEvent event) 
            {
                if (webcam != null) webcam.dispose();
            }
        });
        //--------------------------------------------------------------------------
        
        scene.widthProperty().addListener(new ChangeListener<Number>() 
        {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) 
            {                
//                _resize();
            }
        });
        
        scene.heightProperty().addListener(new ChangeListener<Number>() 
        {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) 
            {
//                _resize();
            }
        });

    } 

    private long count = 0;

    public static void main(String[] args) {
        
        args = new String[1];
        args[0] = "1";
        
        try
        {
            webcam_id = Integer.parseInt(args[0]);
            System.out.println(args[0]);
        }
        catch (Exception ex) {}
                
        launch(args);
        
        
    }
    
//    private void _paint()
//    {
//        int delay = 50; // 20 fps
//        int ms = (int)(count/(1000000*delay));
//        if (ms == this._last_ms) return;
//        
//        this._last_ms = ms;
//
//        Canvas canvas = this._controller.canvasCam;
//        GraphicsContext g = canvas.getGraphicsContext2D();
//        
//        g.setFill(Paint.valueOf("#000000"));
//        g.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
//
//        try
//        {
//           
//            BufferedImage image = this.webcam.image;
//            if (image != null)
//            {                                 
//                this._image = SwingFXUtils.toFXImage(image, null);
//                if (this._image != null)
//                {
//                    double h = canvas.getHeight();
//                    double w = h * this._image.getWidth() / this._image.getHeight();
//                    if (w > canvas.getWidth())
//                    {
//                        w = canvas.getWidth();
//                        h = w * this._image.getHeight() / this._image.getWidth();
//                    }
//                    double x = (canvas.getWidth() - w) / 2;
//                    double y = (canvas.getHeight() - h) / 2;
//                    g.drawImage(this._image, x, y, w, h);
//                }
//            }
//        }
//        catch (Exception ex) { System.err.println(ex.getMessage());}  
//    }
    
    private void _paint2()
    {
        int delay = 50; // 20 fps
        int ms = (int)(count/(1000000*delay));
//        int ms = 10000;
        if (ms == this._last_ms) return;
        
        this._last_ms = ms;

        Canvas canvas = this._controller.canvasCam;
        GraphicsContext g = canvas.getGraphicsContext2D();
        
        g.setFill(Paint.valueOf("#000000"));
        g.fillRect(0,0, canvas.getWidth(), canvas.getHeight());

        try
        {
            this._image = SwingFXUtils.toFXImage(this._mjpeg.getBitmap(), null);
            if (this._image != null)
            {
                double h = canvas.getHeight();
                double w = h * this._image.getWidth() / this._image.getHeight();
                if (w > canvas.getWidth())
                {
                    w = canvas.getWidth();
                    h = w * this._image.getHeight() / this._image.getWidth();
                }
                double x = (canvas.getWidth() - w) / 2;
                double y = (canvas.getHeight() - h) / 2;
                g.drawImage(this._image, x, y, w, h);
            }
        }
        catch (Exception ex) {}   
                                
        
//        g.setStroke(Paint.valueOf("#ffffff"));
//        g.setLineWidth(3);
//        g.strokeLine(0, 0, canvas.getWidth(), canvas.getHeight());
        
        
        String s = String.format("%d", ms);
        if (this._mjpeg.isConnected()) 
        {
            g.setFill(Paint.valueOf("#00a000"));
            s += " connected";
        } 
        else
        {
            g.setFill(Paint.valueOf("#ff0000"));
            s += " disconnected";
        }
        g.setTextBaseline(VPos.TOP);
        g.fillText(s, 10, 10);
    }
    
    private void _resize()
    {
        Canvas canvas = this._controller.canvasCam;
        canvas.setLayoutX(10);
        canvas.setLayoutY(10);
        canvas.setWidth(_pane.getWidth() - 20);
        canvas.setHeight(_pane.getHeight() - 20);
    }
    
}

package tofgui_v2;

import alisa.Sort;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebCam extends Thread {
    
    private Webcam _webcam = null;
    
    public void load(int webcam_id)
    {
        // get default webcam and open it
        List<Webcam> webcam_list = Webcam.getWebcams();
        for (int i=0; i<webcam_list.size(); i++)
        {
            System.out.println(i + " " + webcam_list.get(i).getName());
        }
        this._webcam = webcam_list.get(webcam_id);
        
        // using Custom ViewSize
        Dimension[] d = new Dimension[] 
        {
            WebcamResolution.PAL.getSize(),
            WebcamResolution.HD720.getSize(),
            new Dimension(2000, 1000),
            new Dimension(200, 100),
        };
        this._webcam.setCustomViewSizes(d);
        Dimension selected_dimension = d[1];
//        Dimension selected_dimension = new Dimension(100,50);
        // using standard ViewSize
//        Dimension []d = this._webcam.getViewSizes();
//        Dimension selected_dimension = d[d.length - 1];
        
        this._webcam.setViewSize(selected_dimension);
        this._webcam.open();
        this.image_width =  selected_dimension.width;
        this.image_height = selected_dimension.height;

        this.gray = new int[ this.image_width / 2][this.image_height / 2];
        this.gray2 = new int[ this.image_width / 2][this.image_height / 2];
        System.out.println(this.image_width + " x " + this.image_height);
    }
    
    @Override
    public void run()
    {
        while (true)
        {
            if (this._abort) 
            {
                return;
            }
            
            try 
            {
                this.image = this._webcam.getImage();

                Thread.sleep(100);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(WebCam.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void dispose()
    {
        this._abort = true;
    }
    
    public int[][]gray = null;
    public int[][]gray2 = null;
    
    public int image_width = 0;
    public int image_height = 0;
    public BufferedImage image = null;
    
    private boolean _abort = false;
}

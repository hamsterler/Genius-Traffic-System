
package tof_test;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class TOF_test {
    
    
    private String _port_name = "";
    
    private int[] _distance = new int[16];
    
    public static void main(String[] args) {
        TOF_test test = new TOF_test();
        Serial2 serial = new Serial2("COM8"); 
        serial.connect();
        Thread serialThread = new Thread(new Runnable(){
            public void run(){
                while(true){
//                    status = _serial.getError();
                    serial.run();
//                    test._distance = serial.getDistance();
//                    System.out.print("Distance:   ");
//                    for (int i = 0; i < 8; i++) {
//                        System.out.print(test._distance[i] + "    ");
//                    }
//                    System.out.println("");
                    
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TOF_test.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        serialThread.start();
    }
    
    
}

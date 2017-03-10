package leddar_vu;

import java.io.IOException;
import static java.lang.System.setProperty;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Main 
{    
    public static void main(String[] args) 
    {
        // read config.txt
        String port = "";
        try 
        {
            List<String> config = Files.readAllLines(Paths.get("config.txt"));
            port = config.get(0);
        }
        catch (IOException ex) 
        {
            System.out.println("Error on reading config.txt : " + ex.getMessage());
            return;
        }
        
        // Windows        
        Serial serial = new Serial(port);
        serial.start();        
        
        // press any key to exit
        try 
        {
            System.in.read();        
        }
        catch (Exception ex) {}
        
        serial.disconnect();
    }

}

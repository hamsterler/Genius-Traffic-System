package tof1;

/**
 *
 * @author Supattra
 */
public class TOF1 
{
    public static void main(String[] args) 
    {
    
       CPU cpu = new CPU("COM4");
       cpu.readConfig();
       try
       {
           //0.Version
            if (!cpu.getVersion()) 
            {
                System.out.println(cpu.getError());
            }
            else
            {
                System.out.println("0. CPU "+ cpu.getDevice() + ", "+ cpu.getMajorVersion() + "." + cpu.getMinorVersion());
                System.out.println("----------------------");
            }
             Thread.sleep(100);
             
              //1.SendData
            if (!cpu.sendData()) 
            {
                System.out.println(cpu.getError());
            }
            else
            {
                System.out.println("1. SendData "+ cpu.sendData());
                System.out.println("----------------------");
            }
             Thread.sleep(100); 
             
            
             //2.GetData
            if (!cpu.getData()) 
            {
                System.out.println(cpu.getError());
            }
            else
            {
                System.out.println("2. GetData "+ cpu.getData());
                System.out.println("----------------------");
            }
             Thread.sleep(100); 
             
             
             //3.GetDistance
            if (!cpu.getDistance()) 
            {
                System.out.println(cpu.getError());
            }
            else
            {
                System.out.println("3. Distance valid = "+ cpu.getValid() + ", Detected =  "+ cpu.getDetected());
                System.out.println("----------------------");
            }
             Thread.sleep(100);
       }
       catch(Exception ex)
       {
           System.out.println("Exception" + ex.getMessage());
       }
       
       cpu.dispose();
     
   
       
//        String port = "COM4";
//        Serial serial = new Serial(port);
//        serial.readConfig();
//        serial.start();
// 
//        try 
//        {
//            System.in.read();        
//        }
//        catch (Exception ex) {}
//        
//        serial.disconnect();
        
    }
    
}

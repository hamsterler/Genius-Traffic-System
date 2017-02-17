/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluetooth.test;

import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

/**
* Class that discovers all bluetooth devices in the neighbourhood
* and displays their name and bluetooth address.
*/
public class BluetoothTest implements DiscoveryListener{

//object used for waiting
private static Object lock=new Object();

//vector containing the devices discovered
private static Vector vecDevices=new Vector();

//main method of the application
public static void main(String[] args) throws IOException {
//LocalDevice local = LocalDevice.getLocalDevice();  
//DiscoveryAgent agent = local.getDiscoveryAgent();
//boolean complete = agent.startInquiry(DiscoveryAgent.GIAC, new DiscoveryListener() {
//   public void deviceDiscovered(RemoteDevice device, DeviceClass cod) {
//       try {
//           System.out.println("Discovered: " + device.getFriendlyName(true));
//       } catch (IOException ex) {
//           Logger.getLogger(BluetoothTest.class.getName()).log(Level.SEVERE, null, ex);
//       }
//    } 
//
//    @Override
//    public void inquiryCompleted(int i) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void servicesDiscovered(int i, ServiceRecord[] srs) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void serviceSearchCompleted(int i, int i1) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//});
//while(!complete) {
//    // wait until discovery completes before continuing 
//}
//}
//
//    @Override
//    public void deviceDiscovered(RemoteDevice rd, DeviceClass dc) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void inquiryCompleted(int i) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void servicesDiscovered(int i, ServiceRecord[] srs) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void serviceSearchCompleted(int i, int i1) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//}
//create an instance of this class
    BluetoothTest bluetoothDeviceDiscovery = new BluetoothTest();

    //display local device address and name
    LocalDevice localDevice = LocalDevice.getLocalDevice();
    System.out.println("Local Device Info:");
    System.out.println("    Address: "+localDevice.getBluetoothAddress());
    System.out.println("    Name: "+localDevice.getFriendlyName());

    //find devices
    DiscoveryAgent agent = localDevice.getDiscoveryAgent();

    System.out.println("----------Starting device inquiry…----------");
    agent.startInquiry(DiscoveryAgent.GIAC, bluetoothDeviceDiscovery);

    try {
        synchronized(lock){
            lock.wait();
        }
    }
    catch (InterruptedException e) {
        e.printStackTrace();
    }

    System.out.println("----------Device Inquiry Completed.----------");

    //print all devices in vecDevices
    int deviceCount=vecDevices.size();

    if(deviceCount <= 0){ 
        System.out.println("No Devices Found .");
    } else{ 
//print bluetooth device addresses and names in the format [ No. address (name) ] 
    System.out.println("Bluetooth Devices: "); 
    for (int i = 0; i < deviceCount; i++) { 
        RemoteDevice remoteDevice=(RemoteDevice)vecDevices.elementAt(i);
        System.out.println("\t" + (i+1)+". "+remoteDevice.getBluetoothAddress()+" ("+remoteDevice.getFriendlyName(true)+")"); 
        System.out.println("\t\tAuthenticated: " + remoteDevice.isAuthenticated());
        System.out.println("\t\tTrusted: " + remoteDevice.isTrustedDevice());
        
    }
    
} 
} 
//end main //methods of DiscoveryListener /** * This call back method will be called for each discovered bluetooth devices. */ 
public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) { 
    System.out.println("Device discovered: "+btDevice.getBluetoothAddress()); 
    //add the device to the vector 
    if(!vecDevices.contains(btDevice)){ 
        vecDevices.addElement(btDevice); 
    } 
} //no need to implement this method since services are not being discovered 
public void servicesDiscovered(int transID, ServiceRecord[] servRecord) { } 
//no need to implement this method since services are not being discovered 
public void serviceSearchCompleted(int transID, int respCode) { } /** * This callback method will be called when the device discovery is * completed. */ 
public void inquiryCompleted(int discType) { 
    synchronized(lock){ 
        lock.notify();
    } 
    switch (discType) { 
        case DiscoveryListener.INQUIRY_COMPLETED : System.out.println("INQUIRY_COMPLETED"); break; 
        case DiscoveryListener.INQUIRY_TERMINATED : System.out.println("INQUIRY_TERMINATED"); break; 
        case DiscoveryListener.INQUIRY_ERROR : System.out.println("INQUIRY_ERROR"); break; 
        default : System.out.println("Unknown Response Code"); break; 
    } 
}//end method 
}//end class
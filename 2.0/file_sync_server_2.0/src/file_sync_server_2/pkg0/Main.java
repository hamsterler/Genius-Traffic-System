package file_sync_server_2.pkg0;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
   
    public static void main(String[] args){        
        
        System.out.println("File Sync Server " + Config.Version + " (" + Config.Update + ")");
        System.out.println(); 

        // create server
        Server server = new Server();
        if (!server.loadConfig()){
            System.out.println("Error on loading configuration");
            System.out.println("<<Error>>: " + server.getError());
            return;
        }         
        
        while (true){            
            ServerSocket server_socket = null; 
            try{ 
                server_socket = new ServerSocket(server.getPort());                
                while (true){
                    // waiting for acception
                    Socket socket = server_socket.accept();
                    
                    Receiver receiver = new Receiver(socket, server);
                    System.out.println("Start Receive");        
                    receiver.start();
                }
            } 
            catch (Exception e) { }

            // close
            if (server_socket != null){
                try { server_socket.close(); } catch (IOException ex) { }
                server_socket = null;
            }
            
            try { Thread.sleep(1000); } catch (InterruptedException ex) { }
        }   
    } 
    
}

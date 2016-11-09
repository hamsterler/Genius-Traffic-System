
package file_sync_client_2.pkg0;

public class Main 
{
    public static void main(String[] args) throws Exception 
    {
        System.out.println("File Sync Client " + Config.Version + " (" + Config.Update + ")");
        System.out.println();
        int count = 0;
        while(true){
            count++;
            System.out.println("Client" + count);
            Client client = new Client();
            if (!client.loadConfig()){
                System.out.println("Error on loading configuration");
                System.out.println("<<Error>>: " + client.getError());
            }      
            client.start();
            //wait until all thread of this client done thier job
            while(!client.isAllThreadFinish()){     
                //waiting            
            }

         }
    }
}
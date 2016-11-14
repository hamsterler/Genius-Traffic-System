package file_sync_client_2.pkg0;

public class Hash {
    public byte[] hash(byte[] input){
        byte[] output = new byte[16];
        int i = 0;  //  <<<< current input position  
        int ii = 0;  
        int x = 0;   //  <<<< current output position   
        int n = input.length;  //  <<<<  input length;
        for(x = 0 ;x < output.length; x++){
            i = x % n;
            ii = (i + x + 1) % n;
            if(x % 3 == 0)
                output[x] = (byte)((input[i] * input[ii]) & 0xff);
            else if(x % 3 == 1)
                output[x] = (byte)((input[i] + input[ii]) & 0xff);
            else if(x % 3 == 2)
                output[x] = (byte)((int)(Math.pow(input[i],x)) & 0xff);                
        }
        return output;
    }
}

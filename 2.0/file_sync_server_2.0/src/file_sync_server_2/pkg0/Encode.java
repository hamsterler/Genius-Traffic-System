package file_sync_server_2.pkg0;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import javax.crypto.Cipher;

public class Encode{

    public byte[] encrypt(byte[] message, String password) throws UnsupportedEncodingException{
        String text = new String(message,"ISO-8859-1");
        String secret = "";
        try{
            Key key = alisa.security.AES.generateKey(password);           
            if (key == null) System.out.println("Error on password");            
            else{
                Cipher cipher = alisa.security.AES.createEncryptCipher(key);
                secret = alisa.security.AES.encrypt(cipher, text);        
            }
        }
        catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
            System.out.println("Maybe your key is wrong.");
        }   
        return secret.getBytes("ISO-8859-1");
    }

    public byte[] decrypt(byte[] b, String password) throws UnsupportedEncodingException{
        String message = "";
        try{
            String secret = new String(b, "ISO-8859-1");
            secret = secret.trim();
            Key key = alisa.security.AES.generateKey(password);
            if (key == null) 
                System.out.println("Error on password");
            else{
                Cipher cipher = alisa.security.AES.createDecryptCipher(key);
                message = alisa.security.AES.decrypt(cipher, secret);
            }
        }
        catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
            System.out.println("Maybe your key is wrong.");
            return null;
        }  
        return message.getBytes("ISO-8859-1");
    }
}


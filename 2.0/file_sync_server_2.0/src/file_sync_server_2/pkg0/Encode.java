package file_sync_server_2.pkg0;

import java.security.Key;
import javax.crypto.Cipher;

public class Encode{
    
    public static byte[] encrypt(byte[] message, byte[] password){
        byte[] secret = null;
        try{                  
            secret = AES.encrypt(message, password);      
        }
        catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
            return null;
        }   
        return secret;
    }
    
    public static byte[] decrypt(byte[] secret, byte[] password){
        byte[] message = null;
        try{
            message = AES.decrypt(secret, password);
        }
        catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
            return null;
        }  
        return message;
    }
    
//     public byte[] encrypt(byte[] message, String password){
//         byte[] secret = null;
//         try{
//             Key key = alisa.security.AES.generateKey(password);           
//             if (key == null) 
//                 System.out.println("Error on password");            
//             else{
//                 Cipher cipher = alisa.security.AES.createEncryptCipher(key);
//                 secret = alisa.security.AES.encrypt(cipher, message);      
//             }
//         }
//         catch (Exception ex){
//             System.out.println("Error: " + ex.getMessage());
//             System.out.println("Maybe your key is wrong.");
//             return null;
//         }   
//         return secret;
//     }
//    
//     public byte[] decrypt(byte[] secret, String password){
//         byte[] message = null;
//         try{
//             Key key = alisa.security.AES.generateKey(password);
//             if (key == null) 
//                 System.out.println("Error on password");
//             else{
//                 Cipher cipher = alisa.security.AES.createDecryptCipher(key);
//                 message = alisa.security.AES.decrypt(cipher, secret);
//             }
//         }
//         catch (Exception ex){
//             System.out.println("Error: " + ex.getMessage());
//             System.out.println("Maybe your key is wrong.");
//             return null;
//         }  
//         return message;
//     }
}
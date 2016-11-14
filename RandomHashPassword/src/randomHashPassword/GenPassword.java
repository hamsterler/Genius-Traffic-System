/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package randomHashPassword;

import java.io.FileOutputStream;
import java.io.IOException;


public class GenPassword {
    
    public void genPasswordToFile(int number_of_Password, int password_length, String path){      
        byte[] write = new byte[number_of_Password * (password_length+2)];
        int current_position = 0;
        for(int j = 0; j < number_of_Password; j++){
            byte[] key = randomPassword(password_length);   
            System.arraycopy(key, 0, write, current_position , key.length);
            current_position += key.length;
            write[current_position] = '\r';
            current_position++;
            write[current_position] = '\n';
            current_position++;
        }
        writeBin(write, write.length, path );
//----------------------Show result-------------------------        
//            System.out.print("password = ");
//            for(int i = 0; i < key.length; i++)
//                System.out.print((key[i] & 0xff) + " ");
//            System.out.println();
//----------------------------------------------------------                      
    }
    
    public byte[] randomPassword(int key_size){
        boolean little = false;
        boolean big = false;
        boolean symbol = false;
        boolean number = false;
        byte[] key = new byte[key_size];
        try{
            for(int i = 0; i < key.length; i++){
                int random = (int)(Math.random() * 93 + 33);
                if(random == '"'){
                    i--;
                    continue;
                }
                key[i] = (byte)(random);
                if(random >= 97 && random <= 122)
                    little = true;
                else if((random >= 123 && random <= 255) || (random >= 91 && random <=96) || (random >= 58 && random <= 64) || (random >= 1 && random <= 47) )
                    symbol = true;
                else if(random >= 65 && random <= 90)
                    big = true;
                else if(random >= 48 && random <= 57)
                    number = true;
                else{
                    System.out.println("Out of bound");
                }
            }
        } catch(NullPointerException e){}
        
        if(little == true && big == true && symbol == true && number == true)
            return key;
        else
            return randomPassword(key_size);
    }
    public boolean writeBin(byte[] data, int size, String path) 
    { 
        try 
        {             
            FileOutputStream f = new FileOutputStream(path);
            f.write(data, 0, size);

            f.close();
        } 
        catch (IOException e) 
        {
            System.out.println("Writing file error : " + e.getMessage());
        }     
        return true;
    }
}
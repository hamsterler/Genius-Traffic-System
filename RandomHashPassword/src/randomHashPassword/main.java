
package randomHashPassword;

public class main {
    public static void main(String[] args) {
        int number_of_password = 100;
        int password_length = 16; //between 8 to 16
        String file_path = "D:/password.txt";
        GenPassword gen = new GenPassword();
        Hash hash = new Hash();
        //gen password in to a file
        gen.genPasswordToFile(number_of_password, password_length, file_path);
        byte[] password = gen.randomPassword(16);
        byte[] hashPass = hash.hash(password);
        //----------------------Show result-------------------------        
        System.out.print("Password = ");
        for(int i = 0; i < password.length; i++)
            System.out.print((password[i] & 0xff) + " ");
        System.out.println();
        
        System.out.print("Hash Password = ");
        for(int i = 0; i < hashPass.length; i++)
            System.out.print((hashPass[i] & 0xff) + " ");
        System.out.println();
        //---------------------------------------------------------- 
    }
    
}

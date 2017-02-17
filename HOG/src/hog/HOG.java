
package hog;
import java.util.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class HOG {
    
    public List<Train> trains = new ArrayList<Train>();
    public int size_x = 128;
    public int size_y = 128;
    public int pixel_per_cell = 4;
    public int cell_per_block = 2; // 4x4
    public int k_constant = 5;
//    BufferedWriter w;
    public static void main(String[] args) {
        try {
            HOG hog = new HOG();
            
//            w.write("POTATO!!!");
//            w.close();


            int k = 1;
            File folder = new File("input");
            hog.openFolder(folder);
            hog.write_output();
//            hog.w.close();
            
//            File test_folder = new File("test_images");
//            hog.openTestFolder(test_folder);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void write_output() throws IOException{
        File statText = new File("Output_Test.txt");
        FileOutputStream is = new FileOutputStream(statText);
        OutputStreamWriter osw = new OutputStreamWriter(is);    
        BufferedWriter w = new BufferedWriter(osw);
        w.write("");
        for (int l = 0; l < trains.size(); l++) {
            w.append(trains.get(l).name + " ");
//            String txt = "";
            for (int j = 0; j < trains.get(l).block[0].length; j++) {
                String txt = "";
                for (int i = 0; i < trains.get(l).block.length; i++) {
                    for (int k = 0; k < trains.get(l).block[0][0].length; k++) {
                        txt += trains.get(l).block[i][j][k] + " ";
                    }
                }
                w.append(txt);
            }
//            w.append(txt + "\r\n");
            w.append("\r\n");
        }
        w.close();
    }
    public void openFolder(File folder) throws IOException{
        String folder_name = folder.getName();
        for (final File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    openFolder(file);
                } else {
                    process(file, folder_name);
                }
            }
    }
    
    public void openTestFolder(File folder) throws IOException{
        for (final File file : folder.listFiles()) {
                test_process(file);
            }
    }

    public void test_process(File file) throws IOException{
        BufferedImage image = ImageIO.read(file);
        
        byte[][] grey = grey(image);
       
        //----normalize---
        for(int j = 0;j < grey[0].length ; j++)
            for (int i = 0; i < grey.length ; i++) {
                grey[i][j] = (byte)Math.sqrt((int)(grey[i][j] & 0xff));
            }
        byte[][] edge = edge(grey);

        //--------------------canny edge--------------------
        byte[][] canny_edge = canny_edge(grey);
        
        int test_type = 2; 
        byte[][] resize = resize(canny_edge, size_x, size_y, test_type);
//        byte[][] resize = grey;

        byte[][] im = resize;
         
        //----------------Gradient----------------
//        int[][][] cell = gradient(im, 10);
        int[][][] cell = gradient2(im, pixel_per_cell);
        int[][][] block = block(cell);
        export(cell, file, im, "output/test_image/");
//        int[][][] block = gradient3(im, pixel_per_cell, cell_per_block);

        for (int i = 0; i < trains.size(); i++) {
            double distance = findDistance2(block, trains.get(i).block);
//            double distance = distance3(block, trains.get(i).block);
            trains.get(i).distance = distance;
        }
        System.out.println("File: " + file.getName());
//        for (int i = 0; i < trains.size(); i++) {
//            System.out.println("Brand: " + trains.get(i).name + "     Distance: " + trains.get(i).distance);
//        }
        //---------------------------Show top3 nearest------------------------
        for (int k = 0; k < k_constant; k++) {           
            String name = "";
            double min = 0;
            int index = 0;
            for (int i = 0; i < trains.size(); i++) {
                if(trains.get(i).distance != 0){
                    min = trains.get(i).distance;
                    name = trains.get(i).name;
                    index = i;
                }
            }
            for (int i = 0; i < trains.size(); i++) {
                if(trains.get(i).distance < min && trains.get(i).distance != 0){
                    min = trains.get(i).distance;
                    name = trains.get(i).name;
                    index = i;
                }
            }
            trains.get(index).distance = 0;
            System.out.println("    Name" + k + ": " + name + "     Distance: " + min);
        
        }
    }
    public void process(File file, String folder_name) throws IOException{
        BufferedImage image = ImageIO.read(file);
//            BufferedImage image = ImageIO.read(new File("human.jpg"));
//        System.out.println("width = " + image.getWidth() + "    height = " + image.getHeight() );

        byte[][] grey = grey(image);
//        System.out.println("width1: " + grey2.length + "hiegh1: " + grey2[0].length);
//        byte[][] grey = trim_image(grey2,0);
//        System.out.println("width2: " + grey.length + "hiegh2: " + grey[0].length);
       
        //--------------------------------------------
        
        //----------------resize------------------
        int block_num = 16;     // 16x16
//        int pixel_per_cell = 4; // 4x4
        
        //----normalize---
        for(int j = 0;j < grey[0].length ; j++){
            for (int i = 0; i < grey.length ; i++) {
                grey[i][j] = (byte)Math.sqrt((int)(grey[i][j] & 0xff));
            }
        }
        byte[][] edge = edge(grey);
        
         //--------------------canny edge--------------------
        byte[][] canny_edge = canny_edge(grey);
        
        int train_type = 1;
        byte[][] resize = resize(canny_edge, size_x, size_x, train_type);
//        byte[][] resize = grey;
        
//        byte[][] resize = resize2(grey, block_num, cell_per_block, pixel_per_cell);
//            byte[][] resize = new byte[grey.length/3][grey[0].length/3];
//            for(int j = 0;j < resize[0].length; j++)
//                for (int i = 0; i < resize.length; i++) {
//                    int sum = grey[i * 3][j * 3]     + grey[i * 3 + 1][j * 3]     + grey[i * 3 + 2][j * 3] +
//                             grey[i * 3][j * 3 + 1] + grey[i * 3 + 1][j * 3 + 1] + grey[i * 3 + 2][j * 3 + 1] +
//                             grey[i * 3][j * 3 + 2] + grey[i * 3 + 1][j * 3 + 2] + grey[i * 3 + 2][j * 3 + 2];
//                    sum = sum / 9;
//                    resize[i][j] = (byte)sum;
//                }
//            
        BufferedImage gim = new BufferedImage(resize.length, resize[0].length, BufferedImage.TYPE_INT_BGR);
        
       
        byte[][] im = resize;
           
        //----------------Gradient----------------
//        int[][][] cell = gradient(im, 10);
        int[][][] cell = gradient2(im, pixel_per_cell);
        int[][][] block = block(cell);
        
        
//        int[][][] block = gradient3(im, pixel_per_cell, cell_per_block);
//        this.trains.add(new Train(block, folder_name));
        this.trains.add(new Train(block, folder_name));    
        String output_folder = "output/";
        export(cell, file, im, output_folder);
        
    }
    
    public void raw_export(int[][][] cell, File file, byte[][] im, String output_folder) throws IOException{
        BufferedImage gim = new BufferedImage(im.length, im[0].length, BufferedImage.TYPE_INT_BGR);
        for(int j = 0;j < im[0].length; j++)
            for (int i = 0; i < im.length; i++) {
                int sum = (int)(im[i][j] &0xff);
                gim.setRGB(i, j, new Color(sum, sum, sum).getRGB());
            }      
//            BufferedImage destImage = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, 200, 100, Scalr.OP_ANTIALIAS);

        //---export image---
        String output_name = file.getName();
        File outputfile = new File(output_folder + output_name);
        ImageIO.write(gim, "png", outputfile);
    }
    
    public void export(int[][][] cell, File file, byte[][] resize, String output_folder) throws IOException{
        BufferedImage gim = new BufferedImage(resize.length, resize[0].length, BufferedImage.TYPE_INT_BGR);
        byte[][] im2 = new byte[resize.length][resize[0].length];
        int max = 0;
        for(int j = 0;j < cell[0].length - 1; j++)
            for(int i = 0; i < cell.length - 1; i++){
                for(int k = 0; k <= 8; k++){
                    if(cell[i][j][k] > max)
                        max = cell[i][j][k];
                }
            }
        for(int j = 0;j < cell[0].length - 1; j++){
            for(int i = 0; i < cell.length - 1; i++){
                int v1 = (int)(((double)cell[i][j][4]/(double)max) * (double)255);  // << |
                int v2 = (int)(((double)((cell[i][j][0] + cell[i][j][8]) / 2) / (double)max) * (double)255);  // << -
                int max3 = 0;
                int max4 = 0;
                for(int x = 1; x <= 3; x++){
                    if(cell[i][j][x] > max3)
                        max3 = cell[i][j][x];
                    if(cell[i][j][4 + x] > max4)
                        max4 = cell[i][j][4 + x];
                }
                int v3 = (int)(((double)max3 / (double)max) * (double)255);  // << /
                int v4 = (int)(((double)max4 / (double)max) * (double)255);  // << \
                for(int k = 1; k <= pixel_per_cell; k++){
                    if(k <= pixel_per_cell/2){
                        if((im2[i * pixel_per_cell + pixel_per_cell/2 + 1][j * pixel_per_cell + k] & 0xff) < v1)
                            im2[i * pixel_per_cell + pixel_per_cell/2 + 1][j * pixel_per_cell + k] = (byte)v1;
                        if((im2[i * pixel_per_cell + k][j * pixel_per_cell + pixel_per_cell/2] & 0xff)  < v2)
                            im2[i * pixel_per_cell + k][j * pixel_per_cell + pixel_per_cell/2] = (byte)v2;
                    }else{
                        if((im2[i * pixel_per_cell + pixel_per_cell/2][j * pixel_per_cell + k] & 0xff) < v1)
                            im2[i * pixel_per_cell + pixel_per_cell/2][j * pixel_per_cell + k] = (byte)v1;
                        if((im2[i * pixel_per_cell + k][j * pixel_per_cell + pixel_per_cell/2 + 1] & 0xff) < v2)
                            im2[i * pixel_per_cell + k][j * pixel_per_cell + pixel_per_cell/2 + 1] = (byte)v2;
                    }
                    if((im2[i * pixel_per_cell + (pixel_per_cell + 1 - k)][j * pixel_per_cell + k] & 0xff) < v3)
                        im2[i * pixel_per_cell + (pixel_per_cell + 1 - k)][j * pixel_per_cell + k] = (byte)v3;
                    if((im2[i * pixel_per_cell + k][j * pixel_per_cell + k] & 0xff) < v4)
                        im2[i * pixel_per_cell + k][j * pixel_per_cell + k] = (byte)v4;
                }
               
            }
        }
        //-----------------------------------------

        for(int j = 0;j < im2[0].length; j++)
            for (int i = 0; i < im2.length; i++) {
                int sum = (int)(im2[i][j] &0xff);
                gim.setRGB(i, j, new Color(sum, sum, sum).getRGB());
            }      
//            BufferedImage destImage = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, 200, 100, Scalr.OP_ANTIALIAS);

        //---export image---
        String output_name = file.getName();
        File outputfile = new File(output_folder + output_name);
        ImageIO.write(gim, "png", outputfile);
    }
    
    public void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                System.out.println(fileEntry.getName());
            }
        }
    }
    
    public byte[][] resize(byte[][] image, int new_width, int new_high, int type){
        int high = image[0].length;
        int width = image.length;
        int w = width / new_width;
        int h = high / new_high;
        
        if(width % new_width != 0) 
            w += 1;
        if(high % new_high != 0)
            h += 1;
        
//        System.out.println("w = " + w + "   h = " + h);
        
        if(w > h)
            h = w;
        else 
            w = h;
        
        byte[][] output = new byte[new_width][new_high];
        for (int j = 0; j < output[0].length; j++) {
            for (int i = 0; i < output.length; i++) {
                int sum = 0;
                int divide = h*w;
                for (int k = 0; k < h; k++) 
                    for (int l = 0; l < w; l++){ 
                        int x = i * w + l;
                        int y = j * h + k;
                        if(x >= image.length || y >= image[0].length){
                            divide--;
                            continue;
                        }
                        sum += image[i * w + l][j * h + k] & 0xff;
                    }
                if(divide == 0 && type == 1){ // for train image
                    if(i == 0 && j > 0)
                        output[i][j] = output[i][j - 1];
                    else if(j == 0 && i > 0)
                        output[i][j] = output[i - 1][j];
                    else 
                        output[i][j] = output[i][j - 1];
                    continue;
                }else if(divide == 0 && type == 2){ // for train image
                    output[i][j] = 0;
                    continue;
                }
                
                output[i][j] = (byte)(sum/divide);
            }
        }
        return output;
    }
    
    public byte[][] resize2(byte[][] image, int block_num, int cell_per_block, int pixel_per_block){
        int cell_num = block_num * ((cell_per_block / 2) + 1);
        int new_width = cell_num * cell_per_block;
        int new_high = new_width;
        return resize(image, new_width, new_width, 1);
    }
    
    public double findDistance(int[][][] block1, int[][][]block2){
        double sum = 0;
        for (int j = 0; j < block1[0].length; j++) {
            for (int i = 0; i < block1.length; i++) {
//                double sum = 0;
                for (int k = 0; k < block1[0][0].length; k++) {
                    sum += Math.pow(block2[i][j][k] - block1[i][j][k], 2);
                }
            }
            
        }
        sum = Math.sqrt(sum);
        
        return sum;
    }
    
    
    public double findDistance2(int[][][] block1, int[][][]block2){
        double[] sum = new double[9];
        for (int j = 0; j < block1[0].length; j++) {
            for (int i = 0; i < block1.length; i++) {
//                double sum = 0;
                for (int k = 0; k < block1[0][0].length; k++) {
                    sum[k] += Math.pow(block2[i][j][k] - block1[i][j][k], 2);
                }
            }
            
        }
        for (int i = 0; i < block1[0][0].length; i++) {
            sum[i] = Math.sqrt(sum[i]);
        }
        double sums = 0;
        
        //1
//        for (int i = 0; i < block1[0][0].length; i++) {
//            sums += Math.pow(sum[i], 2);
//        }
//        sums = Math.sqrt(sums);
        //2
        for (int i = 0; i < block1[0][0].length; i++) {
            double y = sum[i] * Math.sin((20 * i - 10) * Math.PI / 180);
            double x = sum[i]  * Math.cos((20 * i - 10) * Math.PI / 180);
            sums += Math.sqrt( Math.pow(x, 2) + Math.pow(y, 2));
        }
        //3
//        for (int i = 0; i < block1[0][0].length; i++) {
//            sums += sum[i];
//        }
//        sums = Math.sqrt(sums);
        return sums;
    }
    
    public byte[][] grey(BufferedImage image){
        byte[][] grey = new byte[image.getWidth()][image.getHeight()];
        for(int j = 0;j < image.getHeight(); j++){
            for (int i = 0; i < image.getWidth(); i++) {
                Color c = new Color(image.getRGB(i, j));
                int red = (int)(c.getRed() * 0.21);
                int green = (int)(c.getGreen() * 0.72);
                int blue = (int)(c.getBlue() *0.07);
                int sum = red + green + blue;
                grey[i][j] = (byte)sum;
            }        
        }
        return grey;
    }
    
    public byte[][] canny_edge(byte[][] image){
        byte[][] canny_edge = new byte[image.length][image[0].length];
        for(int j = 0;j < image[0].length - 1; j++){
            for (int i = 0; i < image.length - 1; i++) {
                int e = Math.abs(image[i][j] & 0xff  - image[i + 1][j + 1] & 0xff ) + Math.abs(image[i + 1][j] & 0xff  - image[i][j + 1] & 0xff );

                e = e * e;
                if (e > 255) e = 255;
                canny_edge[i][j] = (byte)e;
            }
        }
        return canny_edge;
    }
    
    public byte[][] edge(byte[][] resize){
        byte[][] edge = new byte[resize.length][resize[0].length];
        byte[][] output = new byte[resize.length][resize[0].length];
        for(int j = 0;j < resize[0].length - 1; j++){
            for (int i = 0; i < resize.length - 1; i++) {
                int a = resize[i][j] & 0xff;
                int b = resize[i + 1][j + 1] & 0xff;
                int c = resize[i + 1][j] & 0xff;
                int d = resize[i][j + 1] & 0xff;
                int x = Math.abs(a - b);
                int y = Math.abs(c - d);
                int z = (x + y) * (x + y);
                if(z > 255) z = 255;
                edge[i][j] = (byte)z;
                output[i][j] = (byte)z;
            }
        }
        return edge;
    }
    
    public int[][][] gradient(byte[][] im, int pixel_per_cell){
        int[][][] cell = new int[im.length/pixel_per_cell][im[0].length/pixel_per_cell][9];
        int[][][] cell2 = new int[im.length/pixel_per_cell][im[0].length/pixel_per_cell][2];
        double[][] angle = new double[im.length][im[0].length];
        double[][] gradiant = new double[im.length][im[0].length];
        for(int j = 1;j < im[0].length - 1; j++)
            for (int i = 1; i < im.length - 1; i++) {
                double gx = (im[i + 1][j] & 0xff) - (im[i - 1][j] & 0xff);
                double gy = (im[i][j - 1] & 0xff) - (im[i][j + 1] & 0xff);
                gradiant[i][j] = Math.sqrt(Math.pow(gx, 2) + Math.pow(gy, 2));
                angle[i][j] = Math.atan2(gy, gx) * 180/Math.PI;

            }

        for(int j = 0;j < cell[0].length - 1; j++){
            for (int i = 0; i < cell.length - 1; i++){
                for(int k = 1; k <= pixel_per_cell; k++){
                    for(int l = 1; l <= pixel_per_cell; l++){
                        
                        if(angle[i * pixel_per_cell + k][j * pixel_per_cell + l] >= 0 && angle[i * pixel_per_cell + k][j * pixel_per_cell + l] < 20)
                            cell[i][j][0] += gradiant[i * pixel_per_cell + k][j * pixel_per_cell + l];
                        else if(angle[i * pixel_per_cell + k][j * pixel_per_cell + l] >= 20 && angle[i * pixel_per_cell + k][j * pixel_per_cell + l] < 40)
                            cell[i][j][1] += gradiant[i * pixel_per_cell + k][j * pixel_per_cell + l];
                        else if(angle[i * pixel_per_cell + k][j * pixel_per_cell + l] >= 40 && angle[i * pixel_per_cell + k][j * pixel_per_cell + l] < 60)
                            cell[i][j][2] += gradiant[i * pixel_per_cell + k][j * pixel_per_cell + l];
                        else if(angle[i * pixel_per_cell + k][j * pixel_per_cell + l] >= 60 && angle[i * pixel_per_cell + k][j * pixel_per_cell + l] < 80)
                            cell[i][j][3] += gradiant[i * pixel_per_cell + k][j * pixel_per_cell + l];   
                        else if(angle[i * pixel_per_cell + k][j * pixel_per_cell + l] >= 80 && angle[i * pixel_per_cell + k][j * pixel_per_cell + l] < 100)
                            cell[i][j][4] += gradiant[i * pixel_per_cell + k][j * pixel_per_cell + l];
                        else if(angle[i * pixel_per_cell + k][j * pixel_per_cell + l] >= 100 && angle[i * pixel_per_cell + k][j * pixel_per_cell + l] < 120)
                            cell[i][j][5] += gradiant[i * pixel_per_cell + k][j * pixel_per_cell + l];
                        else if(angle[i * pixel_per_cell + k][j * pixel_per_cell + l] >= 120 && angle[i * pixel_per_cell + k][j * pixel_per_cell + l] < 140)
                            cell[i][j][6] += gradiant[i * pixel_per_cell + k][j * pixel_per_cell + l]; 
                        else if(angle[i * pixel_per_cell + k][j * pixel_per_cell + l] >= 140 && angle[i * pixel_per_cell + k][j * pixel_per_cell + l] < 160)
                            cell[i][j][7] += gradiant[i * pixel_per_cell + k][j * pixel_per_cell + l];
                        else if(angle[i * pixel_per_cell + k][j * pixel_per_cell + l] >= 160 && angle[i * pixel_per_cell + k][j * pixel_per_cell + l] <= 180)
                            cell[i][j][8] += gradiant[i * pixel_per_cell + k][j * pixel_per_cell + l];
                    }
                }
            }
        }
        return cell;
    }
    
    public int[][][] gradient2(byte[][] im, int pixel_per_cell){
        int[][][] cell = new int[im.length/pixel_per_cell][im[0].length/pixel_per_cell][9];
        double[][] angle = new double[im.length][im[0].length];
        double[][] gradiant = new double[im.length][im[0].length];
        for(int j = 1;j < im[0].length - 1; j++)
            for (int i = 1; i < im.length - 1; i++) {
                double gy = (im[i + 1][j] & 0xff) - (im[i - 1][j] & 0xff);
                double gx = (im[i][j - 1] & 0xff) - (im[i][j + 1] & 0xff);
                gradiant[i][j] = Math.sqrt(Math.pow(gx, 2) + Math.pow(gy, 2));
                angle[i][j] = Math.atan2(gy, gx) * 180/Math.PI;

            }

        for(int j = 0;j < cell[0].length - 1; j++){
            for (int i = 0; i < cell.length - 1; i++){
                for(int k = 1; k <= pixel_per_cell; k++){
                    for(int l = 1; l <= pixel_per_cell; l++){
                        double ang = angle[i * pixel_per_cell + k][j * pixel_per_cell + l];
                        double gra = gradiant[i * pixel_per_cell + k][j * pixel_per_cell + l];
                        if(ang <=10){
                            cell[i][j][0] += gra;
                            continue;
                        }else if(ang >= 170){
                            cell[i][j][0] += gra;
                            continue;
                        }else{
                            ang = ang - 10;
                            double a = ang % 20;
                            int index = (int)(ang/20);
                            double ratio = a/20;
                            double value = ratio * gra;
                            cell[i][j][index] += value;
                            cell[i][j][index + 1] += gra - value;
                        }
                    }
                }
            }
        }
        return cell;
    }
    
    public int[][][] gradient3(byte[][] im, int pixel_per_cell, int cell_per_block){
        int[][][] cell = new int[im.length/pixel_per_cell][im[0].length/pixel_per_cell][2];
//        int[][][] cell2 = new int[im.length/pixel_per_cell][im[0].length/pixel_per_cell][2];
        double[][] angle = new double[im.length][im[0].length];
        double[][] gradiant = new double[im.length][im[0].length];
        int[][] gx = new int[im.length][im[0].length];
        int[][] gy = new int[im.length][im[0].length];
        
        for(int j = 1;j < im[0].length - 1; j++)
            for (int i = 1; i < im.length - 1; i++) {
                gx[i][j] = (im[i + 1][j] & 0xff) - (im[i - 1][j] & 0xff);
                gy[i][j] = (im[i][j - 1] & 0xff) - (im[i][j + 1] & 0xff);
//                gradiant[i][j] = Math.sqrt(Math.pow(gx, 2) + Math.pow(gy, 2));
//                angle[i][j] = Math.atan2(gy, gx) * 180/Math.PI;

            }
        for(int j = 0;j < cell[0].length - 1; j++){
            for (int i = 0; i < cell.length - 1; i++){
                for(int k = 1; k <= pixel_per_cell; k++){
                    for(int l = 1; l <= pixel_per_cell; l++){
                        cell[i][j][0] += gx[i * pixel_per_cell + k][i * pixel_per_cell + l];
                        cell[i][j][1] += gy[i * pixel_per_cell + k][i * pixel_per_cell + l];
                    }
                }
            }
        }
        int block_num_x = (cell.length / (cell_per_block / 2)) - 1;
        int block_num_y = (cell[0].length / (cell_per_block / 2)) - 1;
        int[][][] block = new int[cell.length - 1][cell[0].length - 1][2];
        for(int j = 0;j < block_num_y; j++){
            for (int i = 0; i < block_num_x; i++) {
                for (int y = 0; y < cell_per_block; y++) {
                    for (int x = 0; x < cell_per_block; x++) {
                        block[i][j][0] += cell[i * cell_per_block/2 + x][i * cell_per_block/2 + y][0];
                        
                        block[i][j][1] += cell[i * cell_per_block/2 + x][i * cell_per_block/2 + y][1];
                    }
                }
            }
        }
        return block;
    }
    
    public double distance3(int[][][] block1, int[][][] block2){
//        int[][] dx = new int[block1.length][block1[0].length];
//        int[][] dy = new int[block1.length][block1[0].length];
        double sum = 0;
        for (int i = 0; i < block1.length; i++) {
            for (int j = 0; j < block1[0].length; j++) {
                double dx = block1[i][j][0] - block2[i][j][0];
                double dy = block1[i][j][1] - block2[i][j][1];
                sum += Math.pow(dx, 2) + Math.pow(dy, 2);
            }
        }
        sum = Math.sqrt(sum);
        return sum;
    }
    
    public int[][][] block(int[][][] cell){
        int[][][] block = new int[cell.length - 1][cell[0].length - 1][9];
        for(int j = 0;j < cell[0].length - 1; j++){
            for (int i = 0; i < cell.length - 1; i++) {
                for (int k = 0; k < 9; k++) {
                    block[i][j][k] += cell[i    ][j    ][k];

                    block[i][j][k] += cell[i + 1][j    ][k];

                    block[i][j][k] += cell[i    ][j + 1][k];

                    block[i][j][k] += cell[i + 1][j + 1][k];
                }
            }
        }
        return block;
    }
    
    public byte[][] trim_image(byte[][] image, int constant){
        int y_start = -1;
        int y_end = -1;
        int status = 0; 
        for (int j = 0; j < image[0].length; j++) {
            int a = image[0][j] & 0xff;
            int sum = 0;
            for (int i = 0; i < image.length; i++) {
                sum += image[i][j] & 0xff;
            }
            double avr = sum / image.length;
//            System.out.println("a: " + a + "    avr: " + avr);
            int x = Math.abs(a - (int)avr);
            if( status == 0 && x > constant){
                y_start = j;
                status = 1;
            }
            if(status == 1 && x  <= constant){
                y_end = j + 2;
                status = 2;
            }
        }
        if(y_start != -1 && y_end == -1){
            y_end = y_start;
            y_start = 0;
        }
        if(y_start == -1 && y_end == -1){
            y_start = 0;
            y_end = image[0].length;
        }
        int x_start = -1;
        int x_end = -1;
        for (int i = 0; i < image.length; i++) {
            int a = image[i][0] & 0xff;
            int sum = 0;
            for (int j = 0; j < image[0].length; j++) {
                sum += image[i][j] & 0xff;
            }
            double avr = sum / image[0].length;
            int x = Math.abs(a - (int)avr);
            if( status == 2 && x > constant){
                x_start = i;
                status = 3;
            }
            if(status == 3 && x <= constant){
                x_end = i;
                status = -1;
            }
        }
        if(x_start != -1 && x_end == -1){
            x_end = x_start;
            x_start = 0;
        }
        if(x_start == -1 && x_end == -1){
            x_start = 0;
            x_end = image.length;
        }
        if(y_start == y_end){
            y_start = 0;
            y_end = image[0].length;
        }
        if(x_start == x_end){
            x_start = 0;
            x_end = image.length;
        }
        
        System.out.println("x_start: " + x_start + "    x_end: " + x_end);
        System.out.println("y_start: " + y_start + "    y_end: " + y_end);
        
        byte[][] trim_image = new byte[x_end - x_start][y_end - y_start];
        for (int i = 0; i < trim_image.length; i++) {
            for (int j = 0; j < trim_image[0].length; j++) {
                trim_image[i][j] = image[x_start + i][y_start + j];
            }
        }
        return trim_image;
    }
}

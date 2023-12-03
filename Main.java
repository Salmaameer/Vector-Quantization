import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.Buffer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.print.DocFlavor.INPUT_STREAM;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;

public class Main {
    

    public static void main(String[] args) throws IOException{
        VectorQuantizer v = new VectorQuantizer(2,2,4);
        int codeBookSze = 4;

       int[][] n = {{1,2,7,9,4,11},{3,4,6,6,12,12},{4,9,15,14,9,9},{10,10,20,18,8,8},{4,3,17,16,1,4},{4,5,18,18,5,6}};
    
            //   v.compress("");
            int[][] imagePixels = n;
            Vector<int[][]> data = v.divideToblocks(imagePixels);
            double[][] average = v.averageBlock(data, 9);
            // Vector<double[][]> temp = new Vector<>();
            // temp.add(average);
            Vector<double[][]> allAverages = new Vector<>();
    
            
            allAverages.add(average);
    
           
            while (allAverages.size() != codeBookSze ){
                allAverages = v.splitAverage(allAverages);
                v.assignToAverage(data, allAverages);
            }

            Vector<Integer> compressedData = new Vector<>();

            // if the nearest vector will be changed or not
            for(int i = 0 ; i < 50 ; ++i){
                v.assignToAverage(data, allAverages);
            }
    
            
    
            FileOutputStream file = new FileOutputStream("output.txt");
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(allAverages);
            out.writeObject(compressedData);
            out.close();
            
      
        // Vector<int[][]> data = v.divideToblocks(n);
        // double[][] t = v.averageBlock(v.divideToblocks(n),9);

        // for(int i = 0 ; i < 2 ; i++){
        //     for(int j = 0 ; j < 2 ; j++){
        //         System.out.println(t[i][j]);
        //     }
        // }
        // Vector<double[][]> avr = new Vector<>();
        // avr.add(t);

        // avr =v.splitAverage(avr);

        // for (double[][] is : avr) {
        //     for(int i = 0 ; i < is.length ; ++i){
        //         for(int j  =0 ; j < is[i].length ; ++j){
        //             System.out.print(is[i][j]);
        //         }
        //            System.out.println();

        //     }
        //     System.out.println();
        // }

        // v.assignToAverage(data, avr);
        // while (avr.size() != 4 ){

        //     avr = v.splitAverage(avr);
        //     System.out.println("real split");
        //     for (double[][] is : avr) {
        //     for(int i = 0 ; i < is.length ; ++i){
        //         for(int j  =0 ; j < is[i].length ; ++j){
        //             System.out.print(is[i][j]);
        //         }
        //            System.out.println();

        //     }
        //     System.out.println();
        // }

        //     v.assignToAverage(data, avr);
           
        

        // }

        

    }
}

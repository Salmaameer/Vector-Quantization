import java.io.File;
import java.nio.Buffer;
import java.util.Vector;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * VectorQuantizer
 * Parameters : nBits for each pixel , vecSize , codeBook size

Read image
divide image into pixels
 
Divide the image into array of vectors 

Loop over image vector
	calculate average of them

Loop while < codebookSize
	split averages 
	make matches 

Check if matches is right

Re calculate all averages 

Make codebook 

Match each vector to its average code

Write it to the file
 */

class VectorQuantizer {
    int blockHeight ;
    int blockWidth;
    int nPixelBits;
    int codeBookSze;
    int nBlocks;

    


    public VectorQuantizer(int blockh, int blockW ,int nPixelBits, int codeBookSze ) {
        this.blockHeight = blockh;
        this.blockWidth = blockW;
        this.nPixelBits = nPixelBits;
        this.codeBookSze = codeBookSze;
    }
    
    public void compress(String imagePath){


        
        int[][] imagePixels = readImage(imagePath);
        Vector<int[][]> data = divideToblocks(imagePixels);
        double[][] average = averageBlock(data);
        Vector<double[][]> temp = new Vector<>();
        temp.add(average);
        Vector<double[][]> allAverages = splitAverage(temp);

        int[][] compressedData = new int[imagePixels.length][imagePixels.length];

        for (int[][] block : data) {
            double minDis = Integer.MAX_VALUE;
            int[][] nearestVector = null;
            
        //     for(int i = 0 ; i < allAverages.size() ; ++i){
        //         double[][] t = allAverages.get(i);
        //         double dist = calculateDistance(t, block);
        //         if (dist < minDis) {
        //             minDis =  dist;
        //             nearestVector = i;
        //         } 
        // }
        // compressedData.add(nearestVector);
        }
        




        // Vector<int[][]> averageVecs = splitAverage(blocks);

       
    

        // // make two average one is ceiled the other is floored
        //     int[][] temp = new int[blockHeight][blockWidth];

        //     for(int i = 0 ; i < average.length ; ++i){
        //         for(int j = 0 ; i < average[i].length ; ++j){
        //         temp[i][j] = (int) Math.ceil(average[i][j]);
        //     }
        // }
        // averageVecs.add(temp);
        // temp = new int[blockHeight][blockWidth];
        // for(int i = 0 ; i < average.length ; ++i){
        //         for(int j = 0 ; i < average[i].length ; ++j){
        //         temp[i][j] = (int) Math.floor(average[i][j]);
        //     }
        // }
        // averageVecs.add(temp);

        
        // // -2 because we did the first and second averages already
        // for(int i = 0 ; i < codeBookSze-2 ; ++i){

        // }

    }

    public int[][] readImage(String imagePath) {
        
            
        File fImg = new File(imagePath);
        BufferedImage image = null ;
        try{
            image = ImageIO.read(fImg);
        System.out.println("Image read successfully.");
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }


        int height = image.getHeight();
        int width = image.getWidth();
        int nblock = (height* width )/(blockHeight*blockWidth);
        setnBlocks(nblock);
        

        int imgVec[][] = new int[height][width];


        

        for(int i = 0 ; i < height ; i++){
            for(int j = 0 ; j < width ; j++){
                
                imgVec[i][j] = image.getRGB(i,j);
            }
        }

        
        return imgVec;

    }


    public Vector<int[][]> divideToblocks(int[][] imgVec){

        Vector<int[][]> blocksVec = new Vector<>();
        for(int i = 0; i < imgVec.length ; i+=blockHeight){
            for(int j = 0 ; j < imgVec[i].length; j += blockWidth){
                int[][] block = new int[blockHeight][blockWidth];
                for(int x = i ; x < ( i + blockHeight) ; ++x){
                    for(int y = j ; y < ( j + blockWidth) ; ++y){
                        block[x - i][y - j] = imgVec[x][y];
                    }
                }

                blocksVec.add(block);

            }
        }

        return blocksVec;
    }


    public double[][] averageBlock(Vector<int[][]> vec){

        double[][] result = new double[blockHeight][blockWidth];

        for(int i = 0 ; i < result.length ; ++i){
                for(int j = 0 ; j < result[i].length ; ++j){
                    result[i][j] =  0.0 ;
                }

             }

        for (int[][] array : vec) {
        
            for(int i = 0 ; i < array.length ; ++i){
                for(int j = 0 ; j < array[i].length ; ++j){
                    result[i][j] += array[i][j];
                }

             }
        }

            for(int i = 0 ; i < result.length ; ++i){
                for(int j = 0 ; j < result[i].length ; ++j){
                    result[i][j] /= nBlocks ;
                }

             }

             return result;

    }

    public Vector<double[][]> splitAverage(Vector<double[][]> averages) {

        Vector<double[][]> newAvr = new Vector<>();

        for (double[][] ds : averages) {

            double[][] temp = new double[blockHeight][blockWidth];
            for (int i = 0; i < ds.length; ++i) {
                for (int j = 0; j < ds[i].length; ++j) {
                    if(ds[i][j] == Math.round(ds[i][j])){// it means that number is integer
                        temp[i][j] = ds[i][j] + 1;
                    }else{
                        temp[i][j] = Math.ceil(ds[i][j]);
                    }
                }
            }
            newAvr.add(temp);
            temp = new double[blockHeight][blockWidth];
            for (int i = 0; i < ds.length; ++i) {
                for (int j = 0; j < ds[i].length; ++j) {
                    if(ds[i][j] == Math.round(ds[i][j])){// it means that number is integer
                        temp[i][j] = ds[i][j]  - 1;
                    }else{
                        temp[i][j] = Math.floor(ds[i][j]);
                    }
                }
            }
            newAvr.add(temp);
        }
        return newAvr;
    }


    public double calculateDistance(double[][] avr , int[][] block){
        double distance = 0;
        for(int i = 0 ; i < avr.length; ++i){
            for (int j = 0 ; j < avr[i].length ; ++j){
                distance += Math.pow((avr[i][j] - block[i][j]), 2);
            }
        }

        return distance;
    }

    public int getnBlocks() {
        return nBlocks;
    }


    public void setnBlocks(int nBlocks) {
        this.nBlocks = nBlocks;
    }
    
}
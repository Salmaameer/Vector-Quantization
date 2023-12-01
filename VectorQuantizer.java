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


    public int[][] averageBlock(Vector<int[][]> vec){

        int[][] result = new int[blockHeight][blockWidth];

        // Vector<int[][]> average = new Vector<>();
        

        // for (int[][] array : average) {
        
        //     for(int i = 0 ; i < array.length ; ++i){
        //         for(int j = 0 ; j < array[i].length ; ++j){
        //             array[i][j] = 0;
        //         }

        //      }
        // }

        for(int i = 0 ; i < result.length ; ++i){
                for(int j = 0 ; j < result[i].length ; ++j){
                    result[i][j] = 0 ;
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

public int getnBlocks() {
        return nBlocks;
    }


    public void setnBlocks(int nBlocks) {
        this.nBlocks = nBlocks;
    }
    
}
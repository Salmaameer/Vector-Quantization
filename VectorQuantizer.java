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
    int codeBookSze;
    int nBlocks;
    Vector<Integer> compressedData = new Vector<>();

    


    public VectorQuantizer(int blockh, int blockW , int codeBookSze ) {
        this.blockHeight = blockh;
        this.blockWidth = blockW;
        this.codeBookSze = codeBookSze;
    }
    
    public void compress(String imagePath) throws IOException{


        
        int[][] imagePixels = readImage(imagePath);
        //int[][] imagePixels = vec;
        Vector<int[][]> data = divideToblocks(imagePixels);
        double[][] average = averageBlock(data,nBlocks);
        // Vector<double[][]> temp = new Vector<>();
        // temp.add(average);
        Vector<double[][]> allAverages = new Vector<>();

        
        allAverages.add(average);

       
        while (allAverages.size() != codeBookSze ){

            allAverages = splitAverage(allAverages);
            assignToAverage(data, allAverages);
           
        }
        // if the nearest vector will be changed or not
        for(int i = 0 ; i < 50 ; ++i){
            assignToAverage(data, allAverages);
        }



        FileOutputStream file = new FileOutputStream("output.txt");
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(allAverages);
		out.writeObject(compressedData);
		out.close();
        

    }

    public int[][] readImage(String imagePath) throws IOException {
        
            
        File fImg = new File(imagePath);
        BufferedImage image ;
        
            image = ImageIO.read(fImg);
        


        int height = image.getHeight();
        int width = image.getWidth();
        int nblock = (height * width )/(blockHeight * blockWidth);
        setnBlocks(nblock);
        

        int imgVec[][] = new int[height][width];


        

        for(int i = 0 ; i < height ; i++){
            for(int j = 0 ; j < width ; j++){
                
                imgVec[i][j] = image.getRGB(j,i);
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
// public Vector<double[][]> averageBlock(Vector<int[][]> vec,int nBlck){

//         Vector<double[][]> result = new Vector<>();
        
//         for (double[][] ds : result) {
//             for(int i = 0 ; i < ds.length ; ++i){
//                 for(int j = 0 ; j < ds[i].length ; ++j){
//                     ds[i][j] =  0 ;
//                 }

//              }
//         }

        

//         for (int[][] array : vec) {
        
//             for(int i = 0 ; i < array.length ; ++i){
//                 for(int j = 0 ; j < array[i].length ; ++j){
//                     result[i][j] += array[i][j];
//                 }

//              }
//         }

//             for(int i = 0 ; i < result.length ; ++i){
//                 for(int j = 0 ; j < result[i].length ; ++j){
//                     result[i][j] /= nBlck ;
//                 }

//              }

//              return result;

//     }

    public double[][] averageBlock(Vector<int[][]> vec,int nBlck){

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
                    result[i][j] /= nBlck ;
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
                        temp[i][j] = ds[i][j]  - 1;
                    }else{
                        temp[i][j] = Math.floor(ds[i][j]);
                    }
                }
            }
            newAvr.add(temp);
            temp = new double[blockHeight][blockWidth];
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

    public int nearestDistance(Vector<double[][]> avr, int[][] block) {
        double minDis = Integer.MAX_VALUE;
        int nearestIndex = -1;
        for (int i = 0; i < avr.size(); ++i) {

            double[][] t = avr.get(i);
            double dist = calculateDistance(t, block);
            if (dist < minDis) {
                minDis = dist;
                nearestIndex = i;
            }
            else 
            {
                
            }
        }
        System.out.println(nearestIndex);
        return nearestIndex;
    }
    
    public void assignToAverage(Vector<int[][]> data,Vector<double[][]> averages){
        //get all data assigned to the array  and update averages
        Vector<double[][]> sum = new Vector<>();
        Vector<Integer> counter = new Vector<>();
        for(int i = 0 ; i < averages.size() ; ++i){
            sum.add(new double[blockHeight][blockWidth]);
            counter.add(0);
        }

        for(int i = 0; i < data.size() ; ++i){ 
            int nearstIdx = nearestDistance(averages, data.elementAt(i));
            System.out.println(nearstIdx);
            compressedData.add(nearstIdx);
            //sum all data in the same index
            double[][] sumArr = sum.get(nearstIdx);
            for(int k = 0 ; k < data.get(i).length ; ++k){
                for(int j = 0 ; j < data.get(i)[k].length ; ++j){
                    //System.out.println("b" + sumArr[k][j]);
                    sumArr[k][j] += data.get(i)[k][j];
                    //System.out.println("a" + sumArr[k][j]);

                    // System.out.println( data.get(i)[k][j]);

                    
                }
             }

             sum.setElementAt(sumArr,nearstIdx);
             counter.setElementAt(counter.get(nearstIdx)+1, nearstIdx);//counter ++
             sumArr = null;
        }
///////////////////
System.out.println("Sum");
        for (int k = 0 ; k < sum.size() ; ++k) {

            for(int i = 0 ; i < sum.get(k).length ; ++i){
                for(int j = 0 ; j < sum.get(k)[i].length ; ++j){
                   System.out.println(sum.get(k)[i][j]);
                }

             }
        }
/////////////////////
        //cal avrage 
        for (int k = 0 ; k < sum.size() ; ++k) {

            for(int i = 0 ; i < sum.get(k).length ; ++i){
                for(int j = 0 ; j < sum.get(k)[i].length ; ++j){
                    sum.get(k)[i][j] /= (double) counter.get(k) ;
                }

             }
        }

        //splitting 
        averages = sum;
        System.out.println("before");
        for (double[][] is : averages) {
            for(int i = 0 ; i < is.length ; ++i){
                for(int j  =0 ; j < is[i].length ; ++j){
                    System.out.print(is[i][j]);
                }
                   System.out.println();

            }
            System.out.println();
        }
        ////////////////////
        for(int i = 0 ; i < counter.size() ; i++){
            
                System.out.println("c " + i + ":" +counter.get(i));
            
        }
       

    }

    public void readCompressedFile() throws IOException, ClassNotFoundException{
        FileInputStream fis = new FileInputStream("Codebook.txt");
		ObjectInputStream in = new ObjectInputStream(fis);
		Vector<double[][]> codebook = (Vector<double[][]>) in.readObject();
		Vector<Integer> compImage = (Vector<Integer>) in.readObject();
		in.close();

        decompress(codebook, compImage);
    }
    

    public void decompress(Vector<double[][]> codebook, Vector<Integer> compImage){
        Vector<double[][]> image = new Vector<>();
        for(int i = 0; i < compImage.size(); i++){
            double[][] temp = codebook.get(compImage.get(i));
            image.add(temp);
        }
        int y = (int)Math.sqrt(compImage.size()) * blockWidth;
        int img[][] = new int[y][y];

        int i = 0, j = 0;
		for (int n = 0; n < image.size(); ++n) {
			if (j == 0 && n != 0) {
				i = (i + blockWidth) % y;
			}
			for (int r = i; r < (i + blockWidth); ++r) {
				for (int c = j; c < (j + blockWidth); ++c) {
					img[r][c] = (int) image.get(n)[r - i][c - j];
				}
			}
			j = (j + blockWidth) % y;
		}
        writeImage(img, null, y, y);
    }

    public static void writeImage(int[][] image, Vector<Integer> output, int height, int width) {
        String outputFilePath = "output.jpg";
		if (output != null) {
			image = new int[height][width];
			for (int i = 0; i < output.size(); ++i) {
				int r = i / width;
				int c = i % width;
				image[r][c] = output.get(i);
			}
		}
		File fileout = new File(outputFilePath);
		BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image2.setRGB(x, y, (image[y][x] << 16) | (image[y][x] << 8) | (image[y][x]));
			}
		}
		try {
			ImageIO.write(image2, "jpg", fileout);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


    public int getnBlocks() {
        return nBlocks;
    }


    public void setnBlocks(int nBlocks) {
        this.nBlocks = nBlocks;
    }


    
    
}


import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.print.DocFlavor.INPUT_STREAM;
import java.awt.image.BufferedImage;

/**
 * VectorQuantizer
 * Parameters : nBits for each pixel , vecSize , codeBook size
 * 
 * Read image
 * divide image into pixels
 * 
 * Divide the image into array of vectors
 * 
 * Loop over image vector
 * calculate average of them
 * 
 * Loop while < codebookSize
 * split averages
 * make matches
 * 
 * Check if matches is right
 * 
 * Re calculate all averages
 * 
 * Make codebook
 * 
 * Match each vector to its average code
 * 
 * Write it to the file
 */

class VectorQuantizer {
    int blockHeight;
    int blockWidth;
    int codeBookSze;
    int nBlocks;
    int imgHeight;
    int imgWidth;
    Vector<Integer> compressedData;

    public VectorQuantizer(int blockh, int blockW, int codeBookSze) {
        this.blockHeight = blockh;
        this.blockWidth = blockW;
        this.codeBookSze = codeBookSze;
    }

    public void compress(String imagePath) throws IOException {


        //int[][] arr = {{1,2,7,9,4,11},{3,4,6,6,12,12},{4,9,15,14,9,9},{10,10,20,18,8,8},{4,3,17,16,1,4},{4,5,18,18,5,6}};
        int[][] imagePixels = readImage(imagePath);
        //int[][] imagePixels = arr;
        Vector<int[][]> data = divideToblocks(imagePixels);
        //System.out.println(nBlocks);
        //System.out.println(data.size());
        double[][] average = averageBlock(data, nBlocks);
        //printBlock(average);
        Vector<double[][]> allAverages = new Vector<>();
        allAverages.add(average);
        while (allAverages.size() != codeBookSze) {
            allAverages = splitAverage(allAverages);
            assignToAverage(data, allAverages);
        }
        System.out.println(compressedData.size());
        //if the nearest vector will be changed or not
        for (int i = 0; i < 50; ++i) {
            assignToAverage(data, allAverages);
        }

       

        FileOutputStream file = new FileOutputStream("output.bin");
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeInt(imagePixels.length); // image height
        out.writeInt(imagePixels[0].length); // image width
        out.writeObject(allAverages);
        //System.out.println(compressedData.size());
        out.writeObject(compressedData);
        out.close();
    }
    public int[][] readImage(String filePath) throws IOException {
        // give image file as parameter to function and reads an image file and converts its pixel values to a 2D array of floats
        File file = new File(filePath);
        BufferedImage img;
        // built in to read image
        img = ImageIO.read(file);
        // retrive width and height of image
        int width = img.getWidth();
        int height = img.getHeight();

        int numberOfBlocks = (width * height) / codeBookSze;
        nBlocks = numberOfBlocks;
        
        // to store pixels
        int[][] Pixels = new int[height][width];
        int RGB;

        // loop on each pixel of image and extract from each pixel RGB values
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                RGB = img.getRGB(i, j);
                int red = (RGB & 0x00ff0000) >> 16;
                int green = (RGB & 0x0000ff00) >> 8;
                int blue = (RGB & 0x000000ff);
                Pixels[j][i] = Math.max(Math.max(red, green), blue);
            }
        }
        return Pixels;
    }

    // public int[][] readImage(String imagePath) throws IOException {
    //     BufferedImage image = null;
    //     try {
    //         File input_file = new File(imagePath);
    //         image = ImageIO.read(input_file);
    //     } catch (IOException e) {
    //         System.out.println("Error: " + e);
    //     }

    //     int height = image.getHeight();
    //     int width = image.getWidth();
    //     imgHeight = height;
    //     imgWidth = width;
    //     int nblock = (height / blockHeight ) * (width * blockHeight);
    //     setnBlocks(nblock);

    //     int imgVec[][] = new int[height][width];
    //     BufferedImage imk = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    //     for (int i = 0; i < height; i++) {
    //         for (int j = 0; j < width; j++) {
    //             int a = image.getRGB(j, i);
    //             int value = (a >> 16) & 0xFF; // Extract the red component
    //             int rgb = (value << 16) | (value << 8) | value; // gray scale
    //             imk.setRGB(j, i, rgb);
    //             imgVec[i][j] = rgb;
    //             //System.out.println(imgVec[i][j]+'&');
    //         }
    //     }
    //     File fileout = new File("demo.jpg");

    //     try {
    //         ImageIO.write(imk, "jpg", fileout);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }

        
    //     return imgVec;
    // }

    public Vector<int[][]> divideToblocks(int[][] imgVec) {
        
        Vector<int[][]> blocksVec = new Vector<>();
        for (int i = 0; i + blockHeight <= imgVec.length; i += blockHeight) {
            for (int j = 0; j + blockWidth <= imgVec[i].length; j += blockWidth) {
                int[][] block = new int[blockHeight][blockWidth];
                for (int x = i; x < (i + blockHeight); ++x) {
                    for (int y = j; y < (j + blockWidth); ++y) {
                        block[x - i][y - j] = imgVec[x][y];
                    }
                }
                blocksVec.add(block);
            }
        }
        return blocksVec;

    }

    public double[][] averageBlock(Vector<int[][]> vec, int nBlck) {
        double[][] result = new double[blockHeight][blockWidth];
        for (int i = 0; i < result.length; ++i) {
            for (int j = 0; j < result[i].length; ++j) {
                result[i][j] = 0.0;
            }
        }

        for (int[][] array : vec) {
            for (int i = 0; i < array.length; ++i) {
                for (int j = 0; j < array[i].length; ++j) {
                    result[i][j] += array[i][j];
                }
            }
        }

        //printBlock(result);

        for (int i = 0; i < result.length; ++i) {
            for (int j = 0; j < result[i].length; ++j) {
                result[i][j] /= nBlck;
                //System.out.println(result[i][j]);
            }
        }
        //printBlock(result);
        return result;
    }

    public Vector<double[][]> splitAverage(Vector<double[][]> averages) {
        Vector<double[][]> newAvr = new Vector<>();
       
        
        for (double[][] ds : averages) {
            double[][] temp = new double[blockHeight][blockWidth];
            for (int i = 0; i < ds.length; ++i) {
                for (int j = 0; j < ds[i].length; ++j) {
                    if (ds[i][j] == Math.round(ds[i][j])) {// it means that number is integer
                        temp[i][j] = ds[i][j] - 1;
                    } else {
                        temp[i][j] = Math.floor(ds[i][j]);
                    }
                }
            }

            newAvr.add(temp);

            temp = new double[blockHeight][blockWidth];
            for (int i = 0; i < ds.length; ++i) {
                for (int j = 0; j < ds[i].length; ++j) {
                    if (ds[i][j] == Math.round(ds[i][j])) {// it means that number is integer
                        temp[i][j] = ds[i][j] + 1;
                    } else {
                        temp[i][j] = Math.ceil(ds[i][j]);
                    }
                }
            }

            newAvr.add(temp);
        }
       
        return newAvr;
    }

    public long calculateDistance(double[][] avr, int[][] block) {

        long distance = 0;
        for (int i = 0; i < avr.length; ++i) {
            for (int j = 0; j < avr.length; ++j) {
                distance += Math.abs((avr[i][j] - block[i][j]));
                //System.out.println("distance: " + distance);
            }
        }
        
        return distance;
    }

    public int nearestDistance(Vector<double[][]> avr, int[][] block) {

        long minDis = Long.MAX_VALUE;
        int nearestIndex = -1;
        for (int i = 0; i < avr.size(); ++i) {
            double[][] t = avr.get(i);
            // printBlock(t);
            // printBlock(block);
            long dist = calculateDistance(t, block);
            if (dist < minDis) {
                minDis = dist;
                nearestIndex = i;
            }
            //  System.out.println("N " + nearestIndex);
        }
        return nearestIndex;
    }

    // returns the new blocks after calculating avgs.
    public Vector<double[][]> assignToAverage(Vector<int[][]> data, Vector<double[][]> averages) {
        Vector<Integer>newcompressedData = new Vector<>();
        // get all data assigned to the array and update averages
        Vector<double[][]> sum = new Vector<>();
        Vector<Integer> counter = new Vector<>();
        for (int i = 0; i < averages.size(); ++i) {
            sum.add(new double[blockHeight][blockWidth]);
            counter.add(0);
        }

        // compressedData = null;
        for (int i = 0; i < data.size(); ++i) {
            int nearstIdx = nearestDistance(averages, data.elementAt(i));
            newcompressedData.add(nearstIdx);
            // sum all data in the same index
            double[][] sumArr = sum.get(nearstIdx);
            for (int k = 0; k < data.get(i).length; ++k) {
                for (int j = 0; j < data.get(i)[k].length; ++j) {

                    sumArr[k][j] += data.get(i)[k][j];

                }
            }

            sum.setElementAt(sumArr, nearstIdx);
            counter.setElementAt(counter.get(nearstIdx) + 1, nearstIdx);// counter ++
            sumArr = null;
        }
        this.compressedData = newcompressedData;

        // cal avrage
        for (int k = 0; k < sum.size(); ++k) {
            for (int i = 0; i < sum.get(k).length; ++i) {
                for (int j = 0; j < sum.get(k)[i].length; ++j) {
                    // if the codebook does not has any vectors, can not calculate avg and just
                    // remain the same.
                    if (sum.get(k)[i][j] == 0) {
                        sum.get(k)[i][j] = averages.get(k)[i][j];
                    } else
                        sum.get(k)[i][j] /= (double) counter.get(k);
                }
            }
        }

    
        // return the sum instead of this assign, because it does not
        // reflect in the real averages.
        return sum;
    }

    public void decompress() throws IOException, ClassNotFoundException {

        FileInputStream fis = new FileInputStream("output.bin");
        ObjectInputStream in = new ObjectInputStream(fis);
        int imgHeight = in.readInt();
        int imgWidth = in.readInt();
        Vector<double[][]> codebook = (Vector<double[][]>) in.readObject();
        Vector<Integer> compImage = (Vector<Integer>) in.readObject();
        in.close();

        
        //System.out.println(compImage.size());

        Vector<double[][]> image = new Vector<>();
        for (int i = 0; i < compImage.size(); i++) {
            //System.out.println(compImage.get(i));
            double[][] temp = codebook.get(compImage.get(i));
            //printBlock(temp);
            image.add(temp);
        }

        File fileout = new File("dImage.jpg");
        BufferedImage image2 = new BufferedImage(imgWidth, imgHeight,
                BufferedImage.TYPE_INT_RGB);

    
        for (double[][] ds : image) {
            for (int x = 0; x < ds.length; x++) {
                for (int y = 0; y < ds[x].length; y++) {

                    int value = (int) ds[x][y];
                    int rgb = (value << 16) | (value << 8) | value;
                    //System.out.println(value + "&");
                    image2.setRGB(y, x, value);
                }
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

    public Vector<Integer> getCompressedData() {
        return this.compressedData;
    }

    public Boolean equal(Vector<double[][]> lastAvgs, Vector<double[][]> allAvgs) {
        for (int i = 0; i < allAvgs.size(); i++) {
            double[][] array1 = allAvgs.get(i);
            double[][] array2 = lastAvgs.get(i);

            if (array1.length != array2.length || !Arrays.deepEquals(array1, array2)) {
                return false;
            }
        }
        // Vectors are equal
        return true;
    }

    void printBlock(double[][] block){
       for (int i = 0; i < block.length; ++i) {
                for (int j = 0; j < block[i].length; ++j) {
                    System.out.print(block[i][j] + " ");
                }
            System.out.println();
            }

            System.out.println();
    }
    void printBlock(int[][] block){
       for (int i = 0; i < block.length; ++i) {
                for (int j = 0; j < block[i].length; ++j) {
                    System.out.print(block[i][j] + " ");
                }
            System.out.println();
            }

            System.out.println();
    }
    

}
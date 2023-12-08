import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;
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
    static int blockHeight;
    static int blockWidth;
    public static int codeBookSze;
    public static int nBlocks;
    public Vector<Integer> compressedData;
    static double[][] reconstructedImage;
    public static int imgHeight;
    public static int imgWidth;

    public VectorQuantizer(int blockh, int blockW, int codeBookSze) {
        this.blockHeight = blockh;
        this.blockWidth = blockW;
        this.codeBookSze = codeBookSze;
    }

    public void compress(String imagePath) throws IOException {
        double[][] imagePixels = readImg(imagePath);
        Vector<double[][]> data = divideToBlocks(imagePixels);
        double[][] average = averageBlock(data, nBlocks);
        Vector<double[][]> allAverages = new Vector<>();
        allAverages.add(average);
        allAverages = splitAverage(allAverages);

        while (allAverages.size() != codeBookSze) {
            allAverages = assignToAverage(data, allAverages);
            allAverages = splitAverage(allAverages);
        }

        // if the nearest vector will be changed or not
        for (int i = 0; i < 50; ++i) {
            Vector<double[][]> prevAvgs = allAverages;

            allAverages = assignToAverage(data, allAverages);
            if(prevAvgs == allAverages)
                break;
        }

        FileOutputStream file = new FileOutputStream("output.bin");
        ObjectOutputStream out = new ObjectOutputStream(file);

        out.writeObject(allAverages);
        out.writeObject(compressedData);
        out.close();
    }

    public static double[][] readImg(String filePath) throws IOException {
        // give image file as parameter to function and reads an image file and converts
        // its pixel values to a 2D array of floats
        File file = new File(filePath);
        BufferedImage img;
        // built in to read image
        img = ImageIO.read(file);
        // retrive width and height of image
        int width = img.getWidth();
        imgWidth = width;
        int height = img.getHeight();
        imgHeight = height;

        int numberOfBlocks = (width * height) / codeBookSze;
        nBlocks = numberOfBlocks;
        // to store pixels
        double[][] Pixels = new double[height][width];
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

    public Vector<double[][]> divideToBlocks(double[][] img) {
        Vector<double[][]> splitted = new Vector<>();
        int numRows = img.length;
        int numCols = img[0].length;

        for (int i = 0; i < numRows; i += blockHeight) {
            for (int j = 0; j < numCols; j += blockWidth) {
                int rowEnd = Math.min(i + blockHeight, numRows);
                int colEnd = Math.min(j + blockWidth, numCols);
                double[][] block = new double[blockHeight][blockWidth];

                for (int r = i; r < rowEnd; ++r) {
                    for (int c = j; c < colEnd; ++c) {
                        block[r - i][c - j] = img[r][c];
                    }
                }
                splitted.add(block);
            }
        }
        return splitted;
    }

    public double[][] averageBlock(Vector<double[][]> data, int nBlck) {
        double[][] result = new double[blockHeight][blockWidth];
        for (int i = 0; i < result.length; ++i) {
            for (int j = 0; j < result[i].length; ++j) {
                result[i][j] = 0.0;
            }
        }

        for (double[][] array : data) {
            for (int i = 0; i < array.length; ++i) {
                for (int j = 0; j < array[i].length; ++j) {
                    result[i][j] += array[i][j];
                }
            }
        }

        for (int i = 0; i < result.length; ++i) {
            for (int j = 0; j < result[i].length; ++j) {
                result[i][j] /= nBlck;
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

    public double calculateDistance(double[][] avr, double[][] block) {
        double distance = 0.0;
        for (int i = 0; i < avr.length; ++i) {
            for (int j = 0; j < avr[i].length; ++j) {
                distance += Math.pow(avr[i][j] - block[i][j], 2);
            }
        }
        return Math.sqrt(distance);
    }
    

    public int nearestDistance(Vector<double[][]> avr, double[][] block) {
        double minDis = Integer.MAX_VALUE;
        int nearestIndex = -1;
        for (int i = 0; i < avr.size(); ++i) {
            double[][] t = avr.get(i);
            double dist = calculateDistance(t, block);
            if (dist < minDis) {
                minDis = dist;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }


    public Vector<double[][]> assignToAverage(Vector<double[][]> data, Vector<double[][]> averages) {
        compressedData = new Vector<>();
        
        // Initialize accumulators for sums and counters
        Vector<double[][]> sum = new Vector<>();
        Vector<Integer> counter = new Vector<>();
        
        // Initialize sum and counter vectors based on the number of averages
        for (int i = 0; i < averages.size(); ++i) {
            sum.add(new double[blockHeight][blockWidth]);
            counter.add(0);
        }
    
        // Assign each block to its nearest average and update counters and sums
        for (int i = 0; i < data.size(); ++i) {
            int nearestIdx = nearestDistance(averages, data.elementAt(i));
            this.compressedData.add(nearestIdx);
            
            double[][] block = data.get(i);
            double[][] currentSum = sum.get(nearestIdx);
            int currentCounter = counter.get(nearestIdx);
            
            // Accumulate the block values into the current sum
            for (int k = 0; k < block.length; ++k) {
                for (int j = 0; j < block[k].length; ++j) {
                    currentSum[k][j] += block[k][j];
                }
            }
            
            // Increment the counter for the assigned average
            counter.set(nearestIdx, currentCounter + 1);
            sum.set(nearestIdx, currentSum);
        }
    
        // Calculate averages based on accumulated sums and counters
        Vector<double[][]> newAverages = new Vector<>();
        for (int k = 0; k < sum.size(); ++k) {
            double[][] currentSum = sum.get(k);
            int currentCounter = counter.get(k);
            
            // Create a new average matrix
            double[][] newAvg = new double[blockHeight][blockWidth];
            
            // Calculate the average only if the counter is not zero
            if (currentCounter != 0) {
                for (int i = 0; i < currentSum.length; ++i) {
                    for (int j = 0; j < currentSum[i].length; ++j) {
                        newAvg[i][j] = currentSum[i][j] / (double) currentCounter;
                    }
                }
            } else {
                // If no blocks were assigned, retain the existing average
                newAvg = averages.get(k);
            }
            
            // Add the new average to the list of averages
            newAverages.add(newAvg);
        }
    
        return newAverages;
    }
    

    public String decompress() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("output.bin");
        ObjectInputStream in = new ObjectInputStream(fis);
        Vector<double[][]> codebook = (Vector<double[][]>) in.readObject();
        Vector<Integer> compImage = (Vector<Integer>) in.readObject();
        in.close();

        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

        int index = 0;
        for (int y = 0; y < imgHeight; y += blockHeight) {
            for (int x = 0; x < imgWidth; x += blockWidth) {
                if (index >= compImage.size()) {
                    break;
                }

                int[][] blockPixels = new int[blockHeight][blockWidth];
                double[][] codebookEntry = codebook.get(compImage.get(index));

                // Convert codebook entry to pixel values
                for (int i = 0; i < blockHeight; i++) {
                    for (int j = 0; j < blockWidth; j++) {
                        int value = (int) codebookEntry[i][j];
                        int rgb = (value << 16) | (value << 8) | value;
                        blockPixels[i][j] = rgb;
                    }
                }

                // Set pixels in the image
                for (int i = 0; i < blockHeight; i++) {
                    for (int j = 0; j < blockWidth; j++) {
                        if (x + j < imgWidth && y + i < imgHeight) {
                            image.setRGB(x + j, y + i, blockPixels[i][j]);
                        }
                    }
                }
                index++;
            }
        }

        // Save the reconstructed image
        File fileout = new File("decompressedImage.jpg");
        try {
            ImageIO.write(image, "jpg", fileout);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "decompressedImage.jpg";
    }

    public void setnBlocks(int nBlocks) {
        this.nBlocks = nBlocks;
    }
}
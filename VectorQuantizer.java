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
        int[][] imagePixels = readImage(imagePath);
        Vector<int[][]> data = divideToblocks(imagePixels);
        double[][] average = averageBlock(data, nBlocks);
        Vector<double[][]> allAverages = new Vector<>();
        allAverages.add(average);
        while (allAverages.size() != codeBookSze) {
            allAverages = splitAverage(allAverages);
            assignToAverage(data, allAverages);
        }

        // if the nearest vector will be changed or not
        for (int i = 0; i < 50; ++i) {
            assignToAverage(data, allAverages);
        }

        FileOutputStream file = new FileOutputStream("output.bin");
        ObjectOutputStream out = new ObjectOutputStream(file);
        // out.writeInt(imagePixels.length); // image height
        // out.writeInt(imagePixels[0].length); // image width
        out.writeObject(allAverages);
        out.writeObject(compressedData);
        out.close();
    }

    public int[][] readImage(String imagePath) throws IOException {
        BufferedImage image = null;
        try {
            File input_file = new File(imagePath);
            image = ImageIO.read(input_file);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

        int height = image.getHeight();
        int width = image.getWidth();
        imgHeight = height;
        imgWidth = width;
        int nblock = (height * width) / (blockHeight * blockWidth);
        setnBlocks(nblock);

        int imgVec[][] = new int[height][width];
        BufferedImage imk = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int a = image.getRGB(j, i);
                int value = (a >> 16) & 0xFF; // Extract the red component
                int rgb = (value << 16) | (value << 8) | value; // gray scale
                imk.setRGB(j, i, rgb);
                imgVec[i][j] = rgb;
            }
        }
        File fileout = new File("demo.jpg");

        try {
            ImageIO.write(imk, "jpg", fileout);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgVec;
    }

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

    public long calculateDistance(double[][] avr, int[][] block) {

        long distance = 0;
        for (int i = 0; i < avr.length; ++i) {
            for (int j = 0; j < avr.length; ++j) {
                distance += (avr[i][j] - block[i][j]);
                // System.out.println("distance: " + distance);
            }
        }
        return distance;
    }

    public int nearestDistance(Vector<double[][]> avr, int[][] block) {

        long minDis = Long.MAX_VALUE;
        int nearestIndex = -1;
        for (int i = 0; i < avr.size(); ++i) {
            double[][] t = avr.get(i);
            long dist = calculateDistance(t, block);
            if (dist < minDis) {
                minDis = dist;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    // returns the new blocks after calculating avgs.
    public Vector<double[][]> assignToAverage(Vector<int[][]> data, Vector<double[][]> averages) {
        this.compressedData = new Vector<>();
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
            // System.out.println(nearstIdx);
            this.compressedData.add(nearstIdx);
            // sum all data in the same index
            double[][] sumArr = sum.get(nearstIdx);
            for (int k = 0; k < data.get(i).length; ++k) {
                for (int j = 0; j < data.get(i)[k].length; ++j) {
                    // System.out.println("b" + sumArr[k][j]);

                    sumArr[k][j] += data.get(i)[k][j];

                }
            }

            sum.setElementAt(sumArr, nearstIdx);
            counter.setElementAt(counter.get(nearstIdx) + 1, nearstIdx);// counter ++
            sumArr = null;
        }

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

    // public void decompress() throws IOException, ClassNotFoundException {

    // FileInputStream fis = new FileInputStream("output.bin");
    // ObjectInputStream in = new ObjectInputStream(fis);
    // int imgHeight = in.readInt();
    // int imgWidth = in.readInt();
    // Vector<double[][]> codebook = (Vector<double[][]>) in.readObject();
    // Vector<Integer> compImage = (Vector<Integer>) in.readObject();
    // in.close();

    // // System.out.println("hi :" + imgHeight + "w:"+imgWidth);

    // // Vector<double[][]> image = new Vector<>();
    // // for (int i = 0; i < compImage.size(); i++) {
    // // double[][] temp = codebook.get(compImage.get(i));
    // // image.add(temp);
    // // }

    // // File fileout = new File("dImage.jpg");
    // // BufferedImage image2 = new BufferedImage(imgWidth, imgHeight,
    // BufferedImage.TYPE_INT_RGB);

    // // int value = 230;
    // // for (int x = 0; x < imgHeight; x++) {
    // // for (int y = 0; y < imgWidth; y++) {
    // // image2.setRGB(y, x, value);
    // // value++;

    // // }}

    // // for (double[][] ds : image) {
    // // for (int x = 0; x < ds.length; x++) {
    // // for (int y = 0; y < ds[x].length; y++) {

    // // int value = (int) ds[x][y];
    // // int rgb = (value << 16) | (value << 8) | value;
    // // // System.out.println(rgb + "&");
    // // image2.setRGB(y, x, rgb);
    // // }
    // // }
    // // }
    // // try {
    // // ImageIO.write(image2, "jpg", fileout);
    // // } catch (IOException e) {
    // // e.printStackTrace();
    // // }

    // }

    // public void decompress() throws IOException, ClassNotFoundException {
    // FileInputStream fis = new FileInputStream("output.bin");
    // ObjectInputStream in = new ObjectInputStream(fis);
    // int imgHeight = in.readInt() - 100;
    // int imgWidth = in.readInt() - 100;
    // Vector<double[][]> codebook = (Vector<double[][]>) in.readObject();
    // Vector<Integer> compImage = (Vector<Integer>) in.readObject();
    // in.close();

    // BufferedImage decompressedImage = new BufferedImage(imgWidth, imgHeight,
    // BufferedImage.TYPE_INT_RGB);

    // int blockIndex = 0;
    // for (int y = 0; y < imgHeight; y += blockHeight) {
    // for (int x = 0; x < imgWidth; x += blockWidth) {
    // int[][] blockPixels = new int[blockHeight][blockWidth];
    // if(compImage.get(blockIndex) >= codebook.size()){
    // break;
    // }
    // double[][] codebookEntry = codebook.get(compImage.get(blockIndex));

    // // Populate blockPixels with values from codebook
    // for (int i = 0; i < blockHeight; i++) {
    // for (int j = 0; j < blockWidth; j++) {
    // int value = (int) codebookEntry[i][j];
    // int rgb = (value << 16) | (value << 8) | value;
    // decompressedImage.setRGB(x + j, y + i, rgb);
    // blockPixels[i][j] = rgb;
    // }
    // }
    // blockIndex++;
    // }
    // }

    // File fileout = new File("decompressedImage.jpg");
    // try {
    // ImageIO.write(decompressedImage, "jpg", fileout);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }

    public void decompress() throws IOException, ClassNotFoundException {

        FileInputStream fis = new FileInputStream("output.bin");
        ObjectInputStream in = new ObjectInputStream(fis);
        @SuppressWarnings("unchecked")
        Vector<double[][]> averages = (Vector<double[][]>) in.readObject();
        @SuppressWarnings("unchecked")
        Vector<Integer> compImage = (Vector<Integer>) in.readObject();
        in.close();

        Vector<double[][]> linearImage = new Vector<>();
        for (int j = 0; j < compImage.size(); ++j) {
            double[][] temp = averages.get(compImage.get(j));
            linearImage.add(temp);
        }

        // int vectorSize = averages.get(0).length;
        // int y = (int)Math.sqrt(compImage.size()) * vectorSize;
        // int image[][] = new int[y][y];
        // int i = 0, j = 0;
        // for (int n = 0; n < linearImage.size(); ++n) {
        // if (j == 0 && n != 0) {
        // i = (i + vectorSize) % y;
        // }
        // for (int r = i; r < (i + vectorSize); ++r) {
        // for (int c = j; c < (j + vectorSize); ++c) {
        // image[r][c] = (int) linearImage.get(n)[r - i][c - j];
        // }
        // }
        // j = (j + vectorSize) % y;
        // }
        // writeImage(image, null, "output.jpg", y, y);
        int imageSize = (int) Math.sqrt(compImage.size()) * averages.get(0).length;
        int[][] image = new int[imageSize+10][imageSize+10];
        int row = 0;
        int col = 0;

        for (int i = 0; i < compImage.size(); i++) {
            double[][] codebookEntry = averages.get(compImage.get(i));

            for (int r = 0; r < codebookEntry.length; r++) {
                for (int c = 0; c < codebookEntry[r].length; c++) {
                    image[row + r][col + c] = (int) codebookEntry[r][c];
                }
            }

            col += codebookEntry[0].length;
            if (col >= imageSize) {
                col = 0;
                row += codebookEntry.length;
            }
        }

        writeImage(image, null, "output.jpg", imageSize, imageSize);
    }

    public static void writeImage(int[][] pixels, Vector<Integer> output, String outputFilePath, int height,
            int width) {
        if (output != null) {
            pixels = new int[height][width];
            for (int i = 0; i < output.size(); ++i) {
                int r = i / width;
                int c = i % width;
                pixels[r][c] = output.get(i);
            }
        }
        File fileout = new File(outputFilePath);
        BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image2.setRGB(x, y, (pixels[y][x] << 16) | (pixels[y][x] << 8) | (pixels[y][x]));
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

}
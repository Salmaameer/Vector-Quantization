import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Vector;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

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
    Vector<Integer> compressedData;

    public VectorQuantizer(int blockh, int blockW, int codeBookSze) {
        this.blockHeight = blockh;
        this.blockWidth = blockW;
        this.codeBookSze = codeBookSze;
    }

    public void compress(String imagePath) throws IOException {
        int[][] imagePixels = readImage(imagePath);
        // int[][] imagePixels = vec;
        Vector<int[][]> data = divideToblocks(imagePixels);
        double[][] average = averageBlock(data, nBlocks);
        // Vector<double[][]> temp = new Vector<>();
        // temp.add(average);
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

        FileOutputStream file = new FileOutputStream("output.txt");
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(allAverages);
        // out.writeObject(compressedData);
        out.close();
    }

    public int[][] readImage(String imagePath) throws IOException {
        File fImg = new File(imagePath);
        BufferedImage image;
        image = ImageIO.read(fImg);
        int height = image.getHeight();
        int width = image.getWidth();
        int nblock = (height * width) / (blockHeight * blockWidth);
        setnBlocks(nblock);
        int imgVec[][] = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                imgVec[i][j] = image.getRGB(j, i);
            }
        }
        return imgVec;
    }

    public Vector<int[][]> divideToblocks(int[][] imgVec) {
        Vector<int[][]> blocksVec = new Vector<>();
        for (int i = 0; i < imgVec.length; i += blockHeight) {
            for (int j = 0; j < imgVec[i].length; j += blockWidth) {
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

    public double calculateDistance(double[][] avr, int[][] block) {
        ///////////////////////
        System.out.println("codebook: ");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.print(avr[i][j]);
            }
            System.out.println();
        }
        System.out.println();
        /////////////////////
        double distance = 0;
        for (int i = 0; i < avr.length; ++i) {
            for (int j = 0; j < avr.length; ++j) {
                distance += Math.pow((avr[i][j] - block[i][j]), 2);
                System.out.println("distance: " + distance);
            }
        }
        return distance;
    }

    public int nearestDistance(Vector<double[][]> avr, int[][] block) {
        //////////////////////////////
        System.out.print("nearest index of block: ");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.print(block[i][j]);
            }
            System.out.println();
        }
        System.out.println();
        ////////////////////////////

        double minDis = Integer.MAX_VALUE;
        int nearestIndex = -1;
        for (int i = 0; i < avr.size(); ++i) {
            double[][] t = avr.get(i);
            double dist = calculateDistance(t, block);
            // System.out.println("distance: " + i + " " + dist);
            if (dist < minDis) {
                minDis = dist;
                nearestIndex = i;
            }
        }
        System.out.println(nearestIndex);
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

                    // System.out.println("a" + sumArr[k][j]);
                    // System.out.println( data.get(i)[k][j]);
                }
            }

            sum.setElementAt(sumArr, nearstIdx);
            counter.setElementAt(counter.get(nearstIdx) + 1, nearstIdx);// counter ++
            sumArr = null;
        }

        System.out.println("Sum");
        for (int k = 0; k < sum.size(); ++k) {
            for (int i = 0; i < sum.get(k).length; ++i) {
                for (int j = 0; j < sum.get(k)[i].length; ++j) {
                    System.out.println(sum.get(k)[i][j]);
                }
            }
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

        // splitting
        // averages = sum; // return the sum instead of this assign, because it does not
        // reflect in the real averages.

        System.out.println("before");
        for (double[][] is : averages) {
            for (int i = 0; i < is.length; ++i) {
                for (int j = 0; j < is[i].length; ++j) {
                    System.out.print(is[i][j]);
                }
                System.out.println();
            }
            System.out.println();
        }

        ////////////////////
        for (int i = 0; i < counter.size(); i++) {
            System.out.println("c " + i + ":" + counter.get(i));
        }

        System.out.println("compressed data: ");
        for (int i = 0; i < compressedData.size(); i++) {
            System.out.print(compressedData.get(i) + " ");
        }

        System.out.println();
        return sum;
    }

    public void readCompressedFile() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("output.txt");
        ObjectInputStream in = new ObjectInputStream(fis);
        int blockHeight = in.readInt();
        int blockWidth = in.readInt();
        Vector<double[][]> codebook = (Vector<double[][]>) in.readObject();
        Vector<Integer> compImage = (Vector<Integer>) in.readObject();
        // String compImage = in.readUTF();
        in.close();

        System.out.println("block height: " + blockHeight);
        System.out.println("block width: " + blockWidth);
        System.out.println("codebook: ");

        for (double[][] code : codebook) {
            for (int i = 0; i < blockHeight; i++) {
                for (int j = 0; j < blockWidth; j++) {
                    System.out.print(code[i][j]);
                }
                System.out.println();
            }
            System.out.println();
        }

        System.out.println("img: " + compImage);

        decompress(codebook, compImage);
    }

    public void decompress(Vector<double[][]> codebook, Vector<Integer> compImage) {
        Vector<double[][]> image = new Vector<>();
        for (int i = 0; i < compImage.size(); i++) {
            double[][] temp = codebook.get(compImage.get(i));
            image.add(temp);
        }

        // size of the image
        int y = (int) Math.sqrt(compImage.size()) * blockWidth;
        double img[][] = new double[y][y];

        int row = 0;
        int col = 0; 

        for (int blockIndex = 0; blockIndex < image.size(); ++blockIndex) {
            // If it's not the first block in a row and at the start of a new row
            if (col == 0 && blockIndex != 0) {
                // Move to the next row by updating the row index
                row = (row + blockWidth) % y;
            }

            // Extract the current block from the compressed image
            double[][] currentBlock = image.get(blockIndex);

            // Iterate through each pixel within the current block
            for (int r = row; r < row + blockWidth; ++r) {
                for (int c = col; c < col + blockWidth; ++c) {
                    // Assign the pixel value from the current block to the corresponding position
                    // in the reconstructed image
                    img[r][c] = currentBlock[r - row][c - col];
                }
            }

            // Move to the next column for the next block or wrap around to the next row if
            // needed
            col = (col + blockWidth) % y;
        }

        System.out.println("compressed image: ");
        for (int k = 0; k < y; k++) {
            for (int m = 0; m < y; m++) {
                System.out.print(img[k][m] + " ");
            }
            System.out.println();
        }
        // writeImage(img, null, y, y);
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
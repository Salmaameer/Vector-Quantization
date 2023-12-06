import java.io.FileOutputStream;
import java.util.Vector;
import java.io.IOException;
import java.io.ObjectOutputStream;


public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        int blockHeight = 2;
        int blockWidth = 2;
        int codeBookSze = 4;
        Vector<Integer> compressedData = new Vector<>();


        VectorQuantizer v = new VectorQuantizer(blockHeight, blockWidth, codeBookSze);

        int[][] n = {{1,2,7,9,4,11},{3,4,6,6,12,12},{4,9,15,14,9,9},{10,10,20,18,8,8},{4,3,17,16,1,4},{4,5,18,18,5,6}};

        int[][] imagePixels = n;
        Vector<int[][]> data = v.divideToblocks(imagePixels);
        double[][] average = v.averageBlock(data, 9);
        Vector<double[][]> allAverages = new Vector<>();


        allAverages.add(average);

        while (allAverages.size() != codeBookSze ){
            allAverages = v.splitAverage(allAverages);
            allAverages = v.assignToAverage(data, allAverages);
        }
        Vector<double[][]> lastAvgs = allAverages;
        do{
            lastAvgs = allAverages;
            allAverages = v.assignToAverage(data, allAverages);
        }while(!v.equal(lastAvgs, allAverages));
        
        compressedData = v.getCompressedData();

        FileOutputStream file = new FileOutputStream("output.txt");
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeInt(blockHeight);
        out.writeInt(blockWidth);
        out.writeObject(allAverages);
        out.writeObject(compressedData);
        out.close();

        ////////////// Decompression
        v.readCompressedFile();
    }
}
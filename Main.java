import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        int blockHeight = 6;
        int blockWidth = 6;
        int codeBookSze = 16;
       
        VectorQuantizer v = new VectorQuantizer(blockHeight, blockWidth, codeBookSze);
        v.compress("download.jpg");
        v.decompress();
    }
}
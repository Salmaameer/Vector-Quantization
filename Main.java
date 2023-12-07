import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        
        int blockHeight = 4;
        int blockWidth = 4;
        int codeBookSze = 8;
       
        VectorQuantizer v = new VectorQuantizer(blockHeight, blockWidth, codeBookSze);
        v.compress("download.jpg");
        v.decompress();
    }
}
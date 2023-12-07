import java.io.FileOutputStream;
import java.util.Vector;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        
        int blockHeight = 4;
        int blockWidth = 4;
        int codeBookSze = 8;
       
        VectorQuantizer v = new VectorQuantizer(blockHeight, blockWidth, codeBookSze);
        v.compress("/Users/salmaameer/Downloads/download (1).jpeg");
        v.decompress();
        
    }
}
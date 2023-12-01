import java.io.File;
import java.nio.Buffer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * VectorQuantizer
 * Parameters : nBits for each pixel , vecSize , codeBook size

Read image
 
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

    public void readImage(String imagePath) {
        try{
            
        File fImg = new File(imagePath);
        BufferedImage image = ImageIO.read(fImg);
        System.out.println("Image read successfully.");
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

    }


    
}
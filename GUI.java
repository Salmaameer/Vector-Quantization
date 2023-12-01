import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class GUI extends JFrame{
    JButton chooseBtn, compressBtn, decompressBtn;
    JLabel label;
    
    public GUI(){
        super("Vector Quantization");
        chooseBtn = new JButton("Choose Image");
        chooseBtn.setBounds(50,300,150,40);
        compressBtn = new JButton("Compress");
        compressBtn.setBounds(420,300,100,40);
        decompressBtn = new JButton("Decompress");
        decompressBtn.setBounds(540,300,110,40);

        label = new JLabel();
        label.setBounds(10,10,670,250);
        add(chooseBtn);
        add(compressBtn);
        add(decompressBtn);
        add(label);
    
        chooseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser file = new JFileChooser();
                file.setCurrentDirectory(new File(System.getProperty("user.home")));
                // filter the files
                FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images", "jpg","png", "jpeg");
                file.addChoosableFileFilter(filter);

                int result = file.showSaveDialog(null); // to show file chooser dialog in center.
                
                if(result == JFileChooser.APPROVE_OPTION){
                    File selectedFile = file.getSelectedFile();
                    String path = selectedFile.getAbsolutePath();
                    label.setIcon(ResizeImage(path));
                }
            }
        });
    
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700,400);
        setVisible(true);
    }
     
    // Method to resize image with the same size of a Jlabel
    public ImageIcon ResizeImage(String ImagePath){
        ImageIcon MyImage = new ImageIcon(ImagePath);
        Image img = MyImage.getImage();
        Image scaledImg = img.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon image = new ImageIcon(scaledImg);
        return image;
    }
    
    public static void main(String[] args){
        new GUI();
    }
}


import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


public class GUI extends JFrame{
    JButton chooseBtn, compressBtn, decompressBtn;
    JLabel label;
    VectorQuantizer vq = new VectorQuantizer(4, 4, 16);
    String imgPath;

    
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
                    imgPath = selectedFile.getAbsolutePath();
                    label.setIcon(ResizeImage(imgPath));
                }
            }
        });

        compressBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // try {
                //     vq.compress(imgPath);
                // } catch (IOException e1) {
                //     // TODO Auto-generated catch block
                //     e1.printStackTrace();
                // }
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


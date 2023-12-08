import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI extends JFrame {
    JButton chooseBtn, compressBtn, decompressBtn;
    JLabel label;
    JTextArea heightTextArea, widthTextArea, codebookTextArea;
    VectorQuantizer vq;
    String imgPath;

    public GUI() {
        super("Vector Quantization");
        chooseBtn = new JButton("Choose Image");
        chooseBtn.setBounds(50, 300, 150, 40);
        compressBtn = new JButton("Compress");
        compressBtn.setBounds(420, 300, 100, 40);
        decompressBtn = new JButton("Decompress");
        decompressBtn.setBounds(540, 300, 110, 40);

        label = new JLabel();
        label.setBounds(10, 10, 670, 250);
        add(chooseBtn);
        add(compressBtn);
        add(decompressBtn);
        add(label);

        JLabel heightLabel = new JLabel("Vector Height:");
        heightLabel.setBounds(450, 50, 100, 20);
        add(heightLabel);

        heightTextArea = new JTextArea();
        heightTextArea.setBounds(550, 50, 50, 20);
        add(heightTextArea);

        JLabel widthLabel = new JLabel("Vector Width:");
        widthLabel.setBounds(450, 100, 100, 20);
        add(widthLabel);

        widthTextArea = new JTextArea();
        widthTextArea.setBounds(550, 100, 50, 20);
        add(widthTextArea);

        JLabel codebookLabel = new JLabel("Codebook Size:");
        codebookLabel.setBounds(450, 150, 100, 20);
        add(codebookLabel);

        codebookTextArea = new JTextArea();
        codebookTextArea.setBounds(550, 150, 50, 20);
        add(codebookTextArea);

        chooseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser file = new JFileChooser();
                file.setCurrentDirectory(new File(System.getProperty("user.home")));
                // filter the files
                FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images", "jpg", "png", "jpeg");
                file.addChoosableFileFilter(filter);

                int result = file.showSaveDialog(null); // to show file chooser dialog in center.

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = file.getSelectedFile();
                    imgPath = selectedFile.getAbsolutePath();
                    displayImage(imgPath);
                }
            }
        });

        compressBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int height = Integer.parseInt(heightTextArea.getText());
                    int width = Integer.parseInt(widthTextArea.getText());
                    int codebookSize = Integer.parseInt(codebookTextArea.getText());

                    vq = new VectorQuantizer(height, width, codebookSize);
                    vq.compress(imgPath);
                    System.out.println("Image compressed successfully!");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        decompressBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String path = vq.decompress();
                    displayImage(path);
                } catch (IOException | ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        });

        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 400);
        setVisible(true);
    }

    public void displayImage(String imagePath) {
        ImageIcon imageIcon = new ImageIcon(imagePath);
        Image img = null;
        try {
            img = ImageIO.read(new File(imagePath));
            // Get the original image dimensions
            int imgWidth = img.getWidth(null);
            int imgHeight = img.getHeight(null);
            // Set label size to the original image dimensions
            label.setSize(imgWidth, imgHeight);
            // Scale the image to original dimensions
            Image scaledImg = img.getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(scaledImg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        label.setIcon(imageIcon);
    }

    public static void main(String[] args) {
        new GUI();
    }
}

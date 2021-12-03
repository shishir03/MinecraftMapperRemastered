package io.github.shishir03.minecraftmapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageReader {
    File image;

    public ImageReader(String filepath) {
        image = new File(filepath);
    }

    public int readPixel(int x, int y) {
        try {
            BufferedImage bi = ImageIO.read(image);
            return bi.getRGB(x, y);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static double checkSimilarity(int color1, int color2) {
        Color c1 = Color.decode(Integer.toString(color1));
        Color c2 = Color.decode(Integer.toString(color2));

        return Math.sqrt(Math.pow(c1.getRed() - c2.getRed(), 2) +
                Math.pow(c1.getGreen() - c2.getGreen(), 2) +
                Math.pow(c1.getBlue() - c2.getBlue(), 2));
    }

    public static void main(String[] args) {
        int oceanColor = 0x020514;


    }
}

package com.tmall.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class ImageUtil {
    public static BufferedImage change2jpg(File file) {
        try {
            Image image = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
            PixelGrabber grabber = new PixelGrabber(image, 0, 0, -1, -1, true);
            grabber.grabPixels();
            int width = grabber.getWidth();
            int height = grabber.getHeight();
            final int[] RGB_MASKS = {0xff0000, 0xff00, 0xff};
            final ColorModel RGB_OPAQUE = new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);
            DataBuffer buffer = new DataBufferInt((int[]) grabber.getPixels(), width * height);
            WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
            BufferedImage bufferedImage = new BufferedImage(RGB_OPAQUE, raster, false, null);
            return bufferedImage;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 使用了swing自带的修改图片大小的API
    public static void resizeImage(File srcFile, int width, int height, File destFile) {
        try {
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            Image image = ImageIO.read(srcFile);
            image = resizeImage(image, width, height);
            ImageIO.write((RenderedImage) image, "jpg", destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Image resizeImage(Image srcImage, int width, int height) {
        try {
            BufferedImage bufferedImage = null;
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            bufferedImage.getGraphics().drawImage(srcImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
            return bufferedImage;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

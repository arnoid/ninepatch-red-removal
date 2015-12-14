package org.arnoid;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

public class NinePatchRedBorderRemoval {
    private static final String PNG_FORMAT = "PNG";
    private static final String PNG_9_PATCH_FILE_EXTENSION = ".9.png".toLowerCase();

    public static final void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Please specify path to resources");
            return;
        }

        String path = args[0];

        File f = new File(path);
        handle(f);
    }

    private static void handle(File f) {
        if (!f.exists()) {
            System.out.println("File [" + f.getAbsolutePath() + "] does not exist");
            return;
        }

        System.out.println("Processing file [" + f.getAbsolutePath() + "]");

        if (f.isFile() && f.getAbsolutePath().toLowerCase().endsWith(PNG_9_PATCH_FILE_EXTENSION)) {
            processFile(f);
        } else if (f.isDirectory()) {
            processDir(f);
        } else {
            //This will never happen?
        }
    }

    private static void processDir(final File d) {
        for (String path : d.list()) {
            handle(new File(d.getPath() + File.separator + path));
        }
    }

    private static void processFile(final File f) {
        BufferedImage img;
        try {
            img = ImageIO.read(f);
        } catch (IOException e) {
            System.out.println("Unable to read image: [" + e.getMessage() + "]");
            e.printStackTrace();
            return;
        }

        ColorModel colorModel = img.getColorModel();

        if (colorModel == null) {
            System.out.println("Color model is null. Is file [" + f.getName() + "] is an image");
            return;
        }

        boolean modified = false;

        for (int i = 0; i < img.getWidth(); i++) {
            modified = modified | fixPixel(i, 0, img, colorModel);//first row
            modified = modified | fixPixel(i, img.getHeight() - 1, img, colorModel);//last row
        }

        for (int i = 0; i < img.getHeight(); i++) {
            modified = modified | fixPixel(0, i, img, colorModel);//first column
            modified = modified | fixPixel(img.getWidth() - 1, i, img, colorModel);//last column
        }

        if (modified) {
            System.out.println("Flushing bitmap to file [" + f + "]");
            try {
                ImageIO.write(img, PNG_FORMAT, f);
            } catch (IOException e) {
                System.out.println("Unable to flush bitmap to file [" + f + "]");
                e.printStackTrace();
            }
        } else {
            System.out.println("No changes in bitmap of file [" + f + "]");
        }
    }

    private static boolean fixPixel(final int x, final int y, final BufferedImage img, final ColorModel colorModel) {

        final boolean modified;

        final int rgb = img.getRGB(x, y);

        int a = rgb >> 32 & 0xff;
        int r = rgb >> 16 & 0xff;
        int g = rgb >> 8 & 0xff;
        int b = rgb & 0xff;

        if (r > 0 && g == 0 && b == 0) {
            int newColor = (a << 32) | (0 << 16) | (0 << 8) | 0;
            img.setRGB(x, y, newColor);

            modified = true;
        } else {
            modified = false;
        }

        return modified;
    }

}

package pex.util;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageUtils {

    private static final int K = 16;
    private static final ColorUtils COLOR_UTILS = new ColorUtils();
    private static final String white = COLOR_UTILS.getColorNameFromRgb(255, 255, 255);


    /**
     * Finds and returns the 3 dominant color names in order of 1st, 2nd and 3rd dominant colors in a given image.
     * uses k-mean clustering to reduce the image quality or quantize the image.
     *
     * @param bufferedImage
     * @param excludeWhite
     * @return String array containing the name of 3 dominant colors in the given image
     */
    public static String[] get3DominantColors(BufferedImage bufferedImage, boolean excludeWhite) {

        long startTime = System.currentTimeMillis();

        if(bufferedImage==null) {
            System.out.println("No BufferedImage found!");
            return null;
        }

        Map<String, Integer> colors = new HashMap<>();

        MBFImage image = ImageUtilities.createMBFImage(bufferedImage, false);

        //create cluster/classes.
        FloatKMeans cluster = FloatKMeans.createExact(K);

        // convert to float vector because FloatKMeans takes vector
        float[][] imageData = image.getPixelVectorNative(new float[image.getWidth() * image.getHeight()][3]);

        //convert to requested number of classes/cluster
        FloatCentroidsResult result = cluster.cluster(imageData);

        //print the coordinates of each centroid
        float[][] centroids = result.centroids;

        //Apply classification. Use a HardAssigner to assign each pixel in our image to its respective class using the centroids
        //learned during the FloatKMeans.


        HardAssigner<float[], ?, ?> assigner = result.defaultHardAssigner();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                float[] pixel = image.getPixelNative(x, y);
                int centroid = assigner.assign(pixel);
                Color color = new Color(centroids[centroid][0], centroids[centroid][1], centroids[centroid][2]);
                String colorName = COLOR_UTILS.getColorNameFromColor(color);

                if (excludeWhite && colorName.equals(white))
                    continue;

                if (colors.containsKey(colorName))
                    colors.replace(colorName, colors.get(colorName) + 1);
                else
                    colors.put(colorName, 1);

            }
        }

        System.out.println("Image Quantize completed in: "+ (System.currentTimeMillis() - startTime) + " ms");

        startTime = System.currentTimeMillis();
        String[] sortedColors = getLargest3(colors);

        System.out.println("Time to find 3 dominant color: "+ (System.currentTimeMillis() - startTime) + " ms");
        return sortedColors;
    }


    /**
     * reduces the image size to anywhere from 100x100 to 200x200
     *
     * @param img
     * @return
     */
    public static BufferedImage scaleDown(BufferedImage img) {
        long startTime = System.currentTimeMillis();

        if(img == null)
            return null;

        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        int w = img.getWidth();
        int h = img.getHeight();
        while (w > 200 || h > 200) {
            w = (int) (w * 0.5);
            h = (int) (h * 0.5);
        }

        BufferedImage scratchImage = new BufferedImage(w, h, type);
        Graphics2D g2 = scratchImage.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, 0, 0, w, h, 0, 0, img.getWidth(), img.getHeight(), null);
        g2.dispose();

        System.out.println("Image resized to "+ h + "X" + w +", in:  " + (System.currentTimeMillis() - startTime) + " ms");

        return scratchImage;
    }



    /**
     * Retrieves image from the given URL and returns after resizing
     *
     * @param url
     * @return
     */
    public static BufferedImage getImageFromUrl(StringBuilder url) {
        if(url==null || url.length()==0)
            return null;

        long startTime = System.currentTimeMillis();
        BufferedImage image = null;

        try {
            image = ImageIO.read(new URL(url.toString()));
            System.out.println("Time to retrieve image from URL: "+ (System.currentTimeMillis() - startTime) + " ms");
            image = scaleDown(image);
        } catch (IOException e) {
            System.out.println("Failed to retrieve image from: " + url);
            e.printStackTrace();
        }
        return image;
    }


    /**
     * Finds the largest 3 String keys that has highest value in order.
     *
     * @param colors
     * @return
     */
    private static String[] getLargest3(Map<String, Integer> colors){

        String[] colorNames = new String[]{"", "", ""};
        int[] colorValue    = new int[3];

        for (String c : colors.keySet()){
            int value = colors.get(c);
            int tempValue = value;
            String color = new String(c);
            String tempColor = new String(c);

            if (value > colorValue[0]){
                value = colorValue[0];
                color = colorNames[0];
                colorValue[0] = tempValue;
                colorNames[0] = tempColor;
            }

            if (value > colorValue[1]){
                tempValue = colorValue[1];
                tempColor = colorNames[1];
                colorValue[1] = value ;
                colorNames[1] = color;
                value = tempValue;
                color = tempColor;
            }

            if (value > colorValue[2]){
                colorValue[2] = value ;
                colorNames[2] = color;
            }

        }

        return colorNames;
    }

}

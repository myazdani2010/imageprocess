package pex.util;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ImageUtils {

    private static final int K = 16;
    private static final ColorUtils COLOR_UTILS = new ColorUtils();


    /**
     * uses k-mean clustering to reduce the image quality or quantize the image.
     *
     * @param bufferedImage
     * @return
     */
    public static BufferedImage quantize(BufferedImage bufferedImage) {

        long startTime = System.currentTimeMillis();

        MBFImage image = ImageUtilities.createMBFImage(bufferedImage, false);
        image = ColourSpace.convert(image, ColourSpace.CIE_Lab);

        //create cluster/classes.
        FloatKMeans cluster = FloatKMeans.createExact(K);

        // convert to float vector because FloatKMeans takes vector
        float[][] imageData = image.getPixelVectorNative(new float[image.getWidth() * image.getHeight()][K]);

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
                image.setPixelNative(x, y, centroids[centroid]);
            }
        }

        //convert it back to RGB
        image = ColourSpace.convert(image, ColourSpace.RGB);

        System.out.println("Image Quantize completed in: "+ (System.currentTimeMillis() - startTime) + " ms");
        return ImageUtilities.createBufferedImage(image);
    }


    /**
     * reduces the image size to anywhere 100x100 to 200x200
     *
     * @param img
     * @return
     */
    public static BufferedImage scaleDown(BufferedImage img) {
        long startTime = System.currentTimeMillis();
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        BufferedImage scratchImage = null;
        Graphics2D g2 = null;

        int w = img.getWidth();
        int h = img.getHeight();
        while (w > 200 || h > 200) {
            w = (int) (w * 0.5);
            h = (int) (h * 0.5);
        }

        scratchImage = new BufferedImage(w, h, type);
        g2 = scratchImage.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(ret, 0, 0, w, h, 0, 0, img.getWidth(), img.getHeight(), null);

        ret = scratchImage;

        if (g2 != null) {
            g2.dispose();
        }

        if (w != ret.getWidth() || h != ret.getHeight()) {
            scratchImage = new BufferedImage(w, h, type);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }

        System.out.println("Image resized to "+ h + "X" + w +", in:  " + (System.currentTimeMillis() - startTime) + " ms");

        return ret;
    }


    /**
     * find the dominant colors in a given image. The returned result are sorted based on the highest to lowest dominant color.
     *
     * @param image
     * @param colorPalette
     * @param excludeWhite
     * @return
     */
    public static String[] getDominantColors(BufferedImage image, int colorPalette, boolean excludeWhite) {
        long startTime = System.currentTimeMillis();

        Map<String, Integer> colors = new HashMap<>();

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {

                Color color = new Color(image.getRGB(j, i));
                String colorName = COLOR_UTILS.getColorNameFromColor(color);

                if (colors.containsKey(colorName))
                    colors.replace(colorName, colors.get(colorName) + 1);
                else
                    colors.put(colorName, 1);
            }
        }

        if (excludeWhite)
            colors.remove(COLOR_UTILS.getColorNameFromRgb(255, 255, 255));

        Map<String, Integer> sortedMap = sortByValue(colors);
        String[] sortedColors = sortedMap.keySet().toArray(new String[sortedMap.size()]);

        System.out.println("Dominant color search completed in: " + (System.currentTimeMillis() - startTime) + " ms");
        return getFirstElements(sortedColors, colorPalette);
    }


    public static BufferedImage getImageFromUrl(StringBuilder url) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(new URL(url.toString()));
            image = scaleDown(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    private static Map sortByValue(Map unsortedMap) {
        Map sortedMap = new TreeMap(new ValueComparator(unsortedMap));
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }

    private static String[] getFirstElements(String[] arr, int count) {
        if (count >= arr.length)
            return arr;

        String[] items = new String[count];
        for (int i = 0; i < count; i++)
            items[i] = arr[i];

        return items;
    }

}

class ValueComparator implements Comparator {
    Map map;

    public ValueComparator(Map map) {
        this.map = map;
    }

    public int compare(Object keyA, Object keyB) {
        Comparable valueA = (Comparable) map.get(keyA);
        Comparable valueB = (Comparable) map.get(keyB);
        return valueB.compareTo(valueA);
    }
}

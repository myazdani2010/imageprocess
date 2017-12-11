package pex;

import pex.util.FileUtils;
import pex.util.ImageUtils;

import java.awt.image.BufferedImage;

/**
 * Image Processing Main class. Reads a list of images url from txt file and find 3 most prevalent colors
 * in the RGB scheme in each image, and write the result into a CSV file in a form of url;color;color;color.
 *
 * Process following steps:
 * 1: Read urls.txt file and creates an array of string urls
 * 2: For each url do:
 *      2-1: Create image file from url
 *      2-2: Resize the image to maximum 200X200
 *      2-3: Quantize the image using k-mean clustering where k=16, to convert the image to 16 color classes
 *      2-4: Count pixels from quantized image and find 3 dominant colors excluding White
 *      2-5: change the append the 3 colors to the url String in th array.
 * 3: Save the final result into the CSV file
 *
 *
 * @author Mohammad.Yazdani
 */
public class Main {

    public static void main(String[] args)
    {

        long startTime = System.currentTimeMillis();

        StringBuilder[] urls = FileUtils.readAllLines("urls.txt", false);
        System.out.println("Processing " + urls.length + " images");

        for (int i=0 ; i<5  ; i++){
            System.out.println("------------------------------");
            System.out.println(i + "- " + urls[i]);

            BufferedImage image = ImageUtils.getImageFromUrl(urls[i]);
            image = ImageUtils.quantize(image);
            String[] colors = ImageUtils.getDominantColors(image, 3, true);
//            urls[i] = urls[i] + ";" +String.join(";", colors);
            urls[i].append(";" +String.join(";", colors));
        }
        System.out.println("------------------------------");

        FileUtils.writeToCSV(urls, "urls.csv");

        System.out.println("Process completed in:  " + (System.currentTimeMillis() - startTime)/1000 + " seconds");

    }

}



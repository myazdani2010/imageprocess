package pex.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

public class FileUtils {

    /**
     * Creates CSV file.
     *
     * @param lines
     * @param fileName
     */
    public static void writeToCSV(StringBuilder[] lines, String fileName){
        String path = new File(fileName).getAbsolutePath();

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(path);

            for (StringBuilder line : lines) {
                fileWriter.append(line);
                fileWriter.append("\n");
            }
        } catch (Exception e) {
            System.out.println("Error in writing CSV file !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing file writer!!!");
                e.printStackTrace();
            }
        }
        System.out.println("File Write completed: " + path);
    }


    /**
     * converts txt file to String array separated by line.
     *
     * @param fileName
     * @param removeDuplicates
     * @return
     */
    public static StringBuilder[] readAllLines(String fileName, boolean removeDuplicates) {
        System.out.println("Reading file: " + fileName);
        List<StringBuilder> lines = new ArrayList<>();
        String path = new File(fileName).getAbsolutePath();

        try {
            lines = readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error in Reading file: " + path);
            e.printStackTrace();
        }
        if (removeDuplicates)
            return removeDuplicates(lines);

        System.out.println("File Read completed: " + path);
        return toArray(lines);
    }


    private static StringBuilder[] removeDuplicates(List<StringBuilder> lines) {
        Set<StringBuilder> uniqueLines = new LinkedHashSet<>();
        for (StringBuilder l : lines)
            uniqueLines.add(l);
        return toArray(uniqueLines);
    }


    private static StringBuilder[] toArray(Collection<StringBuilder> stringList){
        if (stringList==null) return null;
        StringBuilder[] elements = new StringBuilder[stringList.size()];
        int i = 0;
        for (StringBuilder s: stringList)
            elements[i++] = s;

        return elements;
    }


    private static List<StringBuilder> readAllLines(Path path, Charset cs) throws IOException {
        try (BufferedReader reader = newBufferedReader(path, cs)) {
            List<StringBuilder> result = new ArrayList<>();
            for (;;) {
                String lineStr = reader.readLine();
                if (lineStr == null)
                    break;
                result.add(new StringBuilder(lineStr));
            }
            return result;
        }
    }


    private static BufferedReader newBufferedReader(Path path, Charset cs) throws IOException {
        CharsetDecoder decoder = cs.newDecoder();
        Reader reader = new InputStreamReader(newInputStream(path), decoder);
        return new BufferedReader(reader);
    }


    private static InputStream newInputStream(Path path, OpenOption... options) throws IOException
    {
        return path.getFileSystem().provider().newInputStream(path, options);
    }



}

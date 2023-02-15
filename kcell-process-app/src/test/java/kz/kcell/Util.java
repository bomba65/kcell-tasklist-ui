package kz.kcell;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Util {
    public static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    public static void writeToFile(ByteArrayOutputStream out, String path) {
        File file = new File(path);
        File parentDirectory = file.getParentFile();
        if (!parentDirectory.exists() && !parentDirectory.mkdirs()) {
            throw new IllegalStateException("Couldn't create directory: " + parentDirectory);
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

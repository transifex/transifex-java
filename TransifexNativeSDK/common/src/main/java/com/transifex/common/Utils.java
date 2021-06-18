package com.transifex.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import androidx.annotation.NonNull;

public class Utils {

    /**
     * Reads an input stream to a string.
     */
    public @NonNull
    static String readInputStream(@NonNull InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        reader.close();

        return result.toString();
    }


    /**
     * Deletes a directory including its contents
     *
     * @return <code>true</code> if and only if the file or directory is successfully deleted;
     * <code>false</code> otherwise
     */
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /**
     * Deletes the directory's contents
     *
     * @return <code>true</code> if it's a directory and it's content is successfully deleted or
     * it's already empty; <code>false</code> otherwise
     */
    public static boolean deleteDirectoryContents(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        boolean success = false;
        if (allContents != null) {
            success = true;
            for (File file : allContents) {
                success &= deleteDirectory(file);
            }
        }
        return success;
    }

    /**
     * URL encodes the provided string.
     */
    public static String urlEncode(String string) throws UnsupportedEncodingException {
        // Fixes URLEncoder's escaping of " " with "+" so that it works with URLs
        return URLEncoder.encode(string, "UTF-8").replace("+", "%20");
    }
}

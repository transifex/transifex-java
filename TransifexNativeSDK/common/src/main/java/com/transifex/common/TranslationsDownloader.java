package com.transifex.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A class that makes use of {@link CDSHandler} to fetch translations from the CDS and save them
 * to files.
 */
public class TranslationsDownloader {

    private static final String TAG = CDSHandler.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(TAG);

    private final CDSHandler mCDSHandler;

    public TranslationsDownloader(@NonNull CDSHandler cdsHandler) {
        mCDSHandler = cdsHandler;
    }

    /**
     * An {@link CDSHandler.FetchCallback} implementation that writes the provided input streams
     * to files.
     * <p>
     * Each downloaded file is added on the <code>filesMap</code> using the respective locale code
     * as key.
     */
    private static class DownloadTranslationsCallback implements CDSHandler.FetchCallback {

        File directory;
        String filename;

        HashMap<String, File> filesMap = new HashMap<>(0);

        public DownloadTranslationsCallback(@NonNull File directory, @NonNull String filename) {
            this.directory = directory;
            this.filename = filename;
        }

        @Override
        public void onFetchingTranslations(@NonNull String[] localeCodes) {
            filesMap = new HashMap<>(localeCodes.length);
        }

        @Override
        public void onTranslationFetched(@Nullable InputStream inputStream, @NonNull String localeCode, @Nullable Exception exception) {
            if (inputStream == null) {
                return;
            }

            File localeDir = new File(directory.getAbsolutePath() + File.separator + localeCode);
            localeDir.mkdirs();

            File localeFile = new File(localeDir.getAbsolutePath() + File.separator + filename);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(localeFile, false);

                FileChannel fileChannel = fileOutputStream.getChannel();
                ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                readableByteChannel.close();
                fileChannel.close();

                filesMap.put(localeCode, localeFile);
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error writing file " + localeFile.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException when reading response for locale " + localeCode + " : " + e);
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {}
    }

    /**
     * Fetches translations from CDS and saves them to files.
     * <p>
     * For each locale, a subdirectory under the provided directory is created and a file containing
     * the translations is created. Note that the provided directory should already exist. If a
     * translation file already exists, it's overwritten.
     *
     * @param localeCode An optional locale to fetch translations from; if  set to <code>null</code>,
     *                   it will fetch translations for the locale codes configured in the
     *                   {@link CDSHandler} instance provided in the constructor.
     * @param tags An optional set of tags. If defined, only strings that have all of the given tags
     *             will be fetched.
     * @param directory  The directory on which to save the translations. The directory should
     *                   already exist.
     * @param filename   The name of the translation file for a locale.
     *
     * @return A key-value map where each locale code points to the downloaded file containing the
     * translations. If an error occurs, some or all locale codes will be missing from the map.
     */
    @NonNull
    public HashMap<String, File> downloadTranslations(@Nullable String localeCode,
                                                      @Nullable Set<String> tags,
                                                      @NonNull File directory,
                                                      @NonNull String filename) {
        if (!directory.isDirectory()) {
            LOGGER.log(Level.SEVERE, "The provided directory does not exist: " + directory.getAbsolutePath());
            return new HashMap<>(0);
        }
        if (filename == null || filename.isEmpty()) {
            LOGGER.log(Level.SEVERE, "The provided filename is not correct: " + filename);
            return new HashMap<>(0);
        }

        DownloadTranslationsCallback callback = new DownloadTranslationsCallback(directory, filename);
        mCDSHandler.fetchTranslations(localeCode, tags, callback);

        return  callback.filesMap;
    }
}

package com.transifex.common;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A class that allows storing and loading a {@link LocaleData.TranslationMap} to/from disk.
 * <p>
 * The {@link LocaleData.TranslationMap} is represented in disk in the following format:
 * <pre>{@code
 * root
 * ├── <locale>
 * │   ├── <filename>
 * ├── <locale>
 * │   ├── <filename>
 * │   │
 * }</pre>
 *
 * <p>
 * Each locale is represented by a directory named after the locale and contains a translation file
 * using a configurable filename. Each locale translation file is encoded in JSON format using the
 * {@link LocaleData.TxPullResponseData} structure.
 */
public class TranslationMapStorage {

    /**
     * The default name for the translations files. It is used by the command line tool's pull
     * command and by the cache providers that read translations from disk or write translations
     * on disk.
     */
    public static final String DEFAULT_TRANSLATION_FILENAME = "txstrings.json";
    /**
     * The default directory that contains the translation files. It is used by the command line
     * tool's pull command and by the standard cache implementation.
     */
    public static final String DEFAULT_TRANSLATIONS_DIR_NAME = "txnative";

    public static final String TAG = TranslationMapStorage.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(TAG);

    private final Gson mGson = new Gson();
    private final IOFileProvider mFileProvider = new IOFileProvider();

    private final String mFilename;

    /**
     * A file interface that abstracts the underlying implementation.
     * <p>
     * The methods have same semantics to {@link File}.
     */
    public interface AbstractFile {

        @Nullable String[] list();

        @NonNull InputStream open() throws IOException;

        @NonNull String getPath();

        @NonNull String getAbsolutePath();

        boolean isDirectory();
    }

    /**
     * An interface that can return an {@link AbstractFile} given a file path.
     */
    public interface AbstractFileProvider {

        @NonNull AbstractFile getFile(@NonNull String pathname);
    }

    /**
     * Creates a new instance that can be used to write or read a {@link LocaleData.TranslationMap}.
     *
     * @param filename The name of a locale's translation file.
     */
    public TranslationMapStorage(@NonNull String filename) {
        mFilename = filename;
    }

    /**
     * Saves the provided translation map to the provided destination directory.
     * <p>
     * The method is synchronous and returns a map where each locale points to a translation file.
     * <p>
     * The method does not delete the content of the destination directory. When saving a locale's
     * translation file, it will replace the existing translation file, if any. Thus, subsequent
     * calls to this method, given the same destination directory, will result in a mix of all the
     * provided translation maps.
     *
     * @param translationMap The translations to save on disk.
     * @param dstDirectory The directory to save the translations to. Existing translation files
     *                     will be overwritten by the supported locale translations. If the directory
     *                     does not exist, it will be created.
     *
     * @return A map with the saved files. If there was an error saving one or more locales, they
     * won't be included in the returned map. An empty map can be returned if everything failed.
     */
    public @NonNull HashMap<String, File> toDisk(@NonNull LocaleData.TranslationMap translationMap, @NonNull File dstDirectory) {
        HashMap<String, File> filesMap = new HashMap<>(translationMap.getLocales().size());

        // Check that provided file is directory or create it
        if (!dstDirectory.isDirectory()) {
            if (!dstDirectory.mkdirs()) {
                LOGGER.log(Level.SEVERE, "Could not create directory: " + dstDirectory.getAbsolutePath());
                return filesMap;
            }
        }

        for (String locale : translationMap.getLocales()) {
            // Create locale subdirectory
            File localeDir = new File(dstDirectory.getPath() + File.separator + locale);
            if (!localeDir.isDirectory()) {
                boolean dirCreated = localeDir.mkdir();
                if (!dirCreated) {
                    LOGGER.log(Level.SEVERE,
                            "Could not create directory: " + localeDir.getAbsolutePath());
                    continue;
                }
            }

            // Write locale file
            LocaleData.LocaleStrings localeStrings = translationMap.get(locale);
            if (localeStrings == null) {
                continue; // Can't happen. Just to suppress lint
            }
            File localeFile = new File(localeDir.getPath() + File.separator + mFilename);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(localeFile, false);
                Writer writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
                // Create a TxPullResponseData object from LocaleStrings
                LocaleData.TxPullResponseData data = new LocaleData.TxPullResponseData(localeStrings.getMap());
                mGson.toJson(data, writer);
                writer.close();
                filesMap.put(locale, localeFile);
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error creating file " + localeFile.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error writing file " +
                        localeFile.getAbsolutePath() + " : " + e);
            }
        }

        return filesMap;
    }

    /**
     * Loads a {@link LocaleData.TranslationMap} from the provided source directory.
     *
     * @param srcDirectory The directory containing translations in the expected format.
     *
     * @return The translation map or <code>null</code> if the directory isn't found or it's empty.
     * If some locales fail to load, they won't be added in the returned map.
     */
    public @Nullable LocaleData.TranslationMap fromDisk(@NonNull File srcDirectory) {
        return fromDisk(mFileProvider, mFileProvider.getFile(srcDirectory.getPath()));
    }

    /**
     * Loads a {@link LocaleData.TranslationMap} from the provided source directory.
     * <p>
     * This is a more general version of {@link #fromDisk(File)}, which can use different file
     * providers.
     */
    protected @Nullable
    LocaleData.TranslationMap fromDisk(@NonNull AbstractFileProvider fileProvider, @NonNull AbstractFile srcDirectory) {
        String[] localeDirNames =  srcDirectory.list();
        if (localeDirNames == null) {
            LOGGER.log(Level.SEVERE, "The directory does not exist: " + srcDirectory.getAbsolutePath());
            return null;
        }

        LocaleData.TranslationMap translationMap = new LocaleData.TranslationMap(localeDirNames.length);

        for (String locale : localeDirNames) {
            // Get locale directory
            AbstractFile localeDir = fileProvider.getFile(srcDirectory.getPath() + File.separator + locale);
            if(!localeDir.isDirectory()) {
                continue;
            }

            // Read locale file
            AbstractFile localeFile = fileProvider.getFile(localeDir.getPath() + File.separator + mFilename);
            InputStream fileInputStream = null;
            Reader reader = null;
            try {
                fileInputStream = localeFile.open();
                reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
                LocaleData.TxPullResponseData data = mGson.fromJson(reader, LocaleData.TxPullResponseData.class);
                if (data == null || data.data == null) {
                    LOGGER.log(Level.SEVERE, "File has incorrect format: " + localeFile.getAbsolutePath());
                    continue;
                }
                translationMap.put(locale, new LocaleData.LocaleStrings(data.data));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.WARNING, "File for locale \"" + locale + "\" does not exist: " + localeFile.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error reading file " +  localeFile.getAbsolutePath() + " : " + e);
            }
            catch (JsonSyntaxException e){
                LOGGER.log(Level.SEVERE, "Error parsing file " +  localeFile.getAbsolutePath() + " : " + e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    } else if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (IOException ignored) {}
            }

        }

        if (translationMap.isEmpty()) {
            return  null;
        }

        return translationMap;
    }

    //region IOFile

    /**
     * An implementation that uses Java's File representation.
     */
    private static class IOFile implements AbstractFile {

        private final File file;

        public IOFile(@NonNull String pathname) {
            file = new File(pathname);
        }

        @Override
        @Nullable public String[] list() {
            return file.list();
        }

        @Override
        @NonNull public InputStream open() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public @NonNull String getPath() {
            return file.getPath();
        }

        @Override
        public @NonNull String getAbsolutePath() {
            return file.getAbsolutePath();
        }

        @Override
        public boolean isDirectory() {
            return file.isDirectory();
        }
    }

    /**
     * A provider that returns an {@link IOFile}.
     */
    public static class IOFileProvider implements AbstractFileProvider {

        @NonNull
        @Override
        public IOFile getFile(@NonNull String pathname) {
            return new IOFile(pathname);
        }
    }

    //endregion
}

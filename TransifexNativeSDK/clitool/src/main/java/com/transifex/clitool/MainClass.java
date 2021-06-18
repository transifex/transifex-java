package com.transifex.clitool;

import com.transifex.common.BuildProperties;
import com.transifex.common.CDSHandler;
import com.transifex.common.LocaleData;
import com.transifex.common.TranslationMapStorage;
import com.transifex.common.TranslationsDownloader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import picocli.CommandLine;

import static picocli.CommandLine.ArgGroup;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Mixin;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.ParentCommand;

/**
 * The main class of the jar that contains the {@link #main(String[])}.
 * <p>
 * The execution and argument parsing is handled by picocli. Each subcommand is implemented in its
 * own class.
 */
@Command(name = "transifex", versionProvider  = MainClass.VersionProvider.class, mixinStandardHelpOptions = true,
        subcommands = {MainClass.PushCommand.class, MainClass.ClearCommand.class,
                MainClass.PullCommand.class, CommandLine.HelpCommand.class},
        description = "Transifex command-line tool for Android",
        synopsisSubcommandLabel = "(push | pull | clear)", sortOptions = false)
public class MainClass {

    public static class VersionProvider implements CommandLine.IVersionProvider {

        @Override
        public String[] getVersion() {
            return new String[]{BuildProperties.getCLIVersion()};
        }
    }

    private static final String TAG = MainClass.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(TAG);

    static final String OUT_DIR_NAME = TranslationMapStorage.DEFAULT_TRANSLATIONS_DIR_NAME;
    static final String OUT_FILE_NAME = TranslationMapStorage.DEFAULT_TRANSLATION_FILENAME;

    @Mixin
    private ReusableAttributes reusable;

    @Option(names = {"-u", "--url"}, required = false, description = "The CDS URL.")
    String hostURL = CDSHandler.CDS_HOST;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MainClass()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Helper method that runs the program without exiting.
     */
    static int testMain(String argumentsString) {
        String[] args = argumentsString.split(" ");
        return  new CommandLine(new MainClass()).execute(args);
    }

    @Command(descriptionHeading = "%nDescription:%n",
            optionListHeading = "%nOptions:%n")
    public static class ReusableAttributes {}

    @Command
    public static class TokenOption {

        @Mixin
        private ReusableAttributes reusable;

        @Option(names = {"-t", "--token"}, required = true, description = "The Transifex token.")
        String token;
    }

    @Command
    public static class SecretOptions extends TokenOption {

        @Option(names = {"-s", "--secret"}, required = true, description = "The Transifex secret.")
        String secret;
    }

    @Command(name = "push", description = "Pushes source strings to CDS", sortOptions = false)
    public static class PushCommand extends SecretOptions implements Callable<Integer> {

        @ParentCommand
        MainClass mainClass;

        @ArgGroup(exclusive = true, multiplicity = "1")
        StringSources stringSources;

        static class StringSources {
            @Option(names = {"-m", "--module"},
                    description = "The name of the module that contains the source strings.",
                    paramLabel = "<module>")
            String moduleName;

            @Option(names = {"-f", "--file"}, arity = "1..",
                    description = "One or more xml resource files containing source strings.",
                    paramLabel = "<file>")
            File[] files;
        }

        @Option(names = {"-a", "--append-tags"}, arity = "0..",
                description = "Append custom tags to the pushed source strings.",
                paramLabel = "<tag>")
        String[] tags;

        @Option(names = {"-p", "--purge"},
                description = "If set, the entire resource content is replaced by the pushed content " +
                        "of this request. Otherwise, source content of this request is appended to " +
                        "the existing resource content.")
        boolean purge;

        @Option(names = {"--dry-run"},
                description = "Do not push to CDS.")
        boolean dryRun;

        @Option(names = {"-v", "--verbose"},
                description = "Verbose output.")
        boolean verbose;

        @Override
        public Integer call() throws Exception {
            // Get String file(s)
            Path currentPath = Paths.get(System.getProperty("user.dir"));
            ArrayList<File> files = new ArrayList<>();

            if (stringSources.moduleName != null) {
                File stringFile = getStringFileForModule(currentPath.toFile(), stringSources.moduleName);
                if (!stringFile.isFile()) {
                    System.out.println("Could not find \"strings.xml\" at: " + stringFile.getAbsolutePath());
                    return 1;
                }
                files.add(stringFile);
            }
            else {
                for (File file : stringSources.files) {
                    if (file.isFile()) {
                        files.add(file);
                    }
                    else {
                        System.out.println("File does not exist: " + file.getAbsolutePath());
                        return 1;
                    }
                }
            }

            if (files.size() == 0) {
                return 1;
            }

            // Parse String file(s)
            LinkedHashMap<String, LocaleData.StringInfo> sourceStringMap = new LinkedHashMap<>(40);
            StringXMLConverter xmlConverter = new StringXMLConverter();

            for (File file : files) {
                try {
                    xmlConverter.process(file, sourceStringMap);
                } catch (Exception e) {
                    System.out.println("Error parsing string file: " + e.getMessage());
                    return 1;
                }
            }

            // Append custom tags
            if (tags != null && tags.length != 0) {
                for (LocaleData.StringInfo stringInfo : sourceStringMap.values()) {
                    stringInfo.appendTags(tags);
                }
            }

            // Create payload
            LocaleData.TxPostData.Meta meta = new LocaleData.TxPostData.Meta();
            meta.purge = purge;
            LocaleData.TxPostData postData = new LocaleData.TxPostData(sourceStringMap, meta);

            if (verbose) {
                StringBuilder sb = new StringBuilder("The following strings are about to be pushed:")
                        .append(System.lineSeparator());
                for (Map.Entry<String, LocaleData.StringInfo> entry : postData.data.entrySet()) {
                    sb.append(entry.getKey()).append(" -> ").append(entry.getValue().toString()).append(System.lineSeparator());
                }
                sb.append(System.lineSeparator());
                sb.append("The following meta will be pushed along the strings: ").append(postData.meta.toString());
                System.out.println();
                System.out.println(sb.toString());
            }

            if (dryRun) {
                return 0;
            }

            // Push to CDS
            CDSHandler cdsHandler = new CDSHandler(null, token, secret,
                    mainClass.hostURL);
            LocaleData.TxPostResponseData response = cdsHandler.pushSourceStrings(postData);

            if (response != null && response.isSuccessful()) {
                System.out.println("Source strings pushed successfully to CDS");

                System.out.println(getDetailsString(response));

                return 0;
            }
            else {
                System.out.println("Error while pushing source strings to CDS");

                if (response != null) {
                    String errorString = getErrorString(response);
                    if (errorString != null) {
                        System.out.println(errorString);
                    }
                }

                return 1;
            }
        }
    }

    @Command(name = "clear", sortOptions = false,
            description = "Clears all existing resource content from CDS")
    public static class ClearCommand extends SecretOptions implements Callable<Integer> {

        @ParentCommand
        MainClass mainClass;

        @Override
        public Integer call() throws Exception {
            // Create payload with empty data
            LocaleData.TxPostData.Meta meta = new LocaleData.TxPostData.Meta();
            meta.purge = true;
            LinkedHashMap<String, LocaleData.StringInfo> emptyMap = new LinkedHashMap<>(0);
            LocaleData.TxPostData postData = new LocaleData.TxPostData(emptyMap, meta);

            // Push to CDS
            CDSHandler cdsHandler = new CDSHandler(null, token, secret,
                    mainClass.hostURL);
            LocaleData.TxPostResponseData response = cdsHandler.pushSourceStrings(postData);

            if (response != null && response.isSuccessful()) {
                System.out.println("Source strings cleared from CDS");

                String details = String.format(Locale.US, "%d strings deleted", response.deleted);
                System.out.println(details);

                return 0;
            }
            else {
                System.out.println("Error while contacting CDS");

                if (response != null) {
                    String errorString = getErrorString(response);
                    if (errorString != null) {
                        System.out.println(errorString);
                    }
                }

                return 1;
            }
        }
    }

    @Command(name = "pull", description = "Downloads translations from CDS to the specified location",
            sortOptions = false)
    public static class PullCommand extends TokenOption implements Callable<Integer> {

        @ParentCommand
        MainClass mainClass;

        @ArgGroup(exclusive = true, multiplicity = "1")
        OutputDestination outputDestination;

        static class OutputDestination {
            @Option(names = {"-m", "--module"},
                    description = "The name of the app module where the translations will be saved to.",
                    paramLabel = "<module>")
            String moduleName;

            @Option(names = {"-d", "--dir"},
                    description = "The path to the app's \"assets\" directory where the translations " +
                            "will be saved to.")
            File directory;
        }

        @Option(names = {"-l", "--locales"}, arity = "1..", required = true,
                description = "A list of the target locales to download from CDS. The " +
                        "source locale can also be included.", paramLabel = "<locale>")
        String[] translatedLocales;

        @Override
        public Integer call() throws Exception {
            // Create output directory
            Path currentPath = Paths.get(System.getProperty("user.dir"));
            File parentDir = null;

            if (outputDestination.moduleName != null) {
                parentDir = getAssetsDirectoryForModule(currentPath.toFile(), outputDestination.moduleName);
            }
            else {
                parentDir = outputDestination.directory;
            }

            File outDir = new File(parentDir.getAbsolutePath() + File.separator + OUT_DIR_NAME);
            if (!outDir.exists()) {
                if (!outDir.mkdirs()) {
                    System.out.println("Could not create directory: " + outDir.getAbsolutePath());
                }
            }

            // Pull from CDS
            CDSHandler cdsHandler = new CDSHandler(translatedLocales, token, null,
                    mainClass.hostURL);
            TranslationsDownloader downloader = new TranslationsDownloader(cdsHandler);
            HashMap<String, File> downloadedFiles = downloader.downloadTranslations(null, outDir, OUT_FILE_NAME);

            if (downloadedFiles.keySet().containsAll(Arrays.asList(translatedLocales))) {
                System.out.println("Translations have been pulled successfully from CDS to: " +
                        outDir.getAbsolutePath());

                return 0;
            }
            else {
                System.out.println("Error while pulling translations from CDS");

                HashSet<String> nonDownloadedLocales = new HashSet<>(Arrays.asList(translatedLocales));
                nonDownloadedLocales.removeAll(downloadedFiles.keySet());
                System.out.println(getNonDownloadedLocalesString(nonDownloadedLocales));

                return 1;
            }
        }
    }

    /**
     * Returns the <code>strings.xml</code> file for the provided gradle module under the main source
     * set in the folder <code>res/values</code>.
     *
     * @param projectDirectory The root of the gradle project.
     * @param moduleName       The module that contains the strings.
     * @return The requested strings.xml file. The file is returned even if does not exist.
     */
    public static @NonNull
    File getStringFileForModule(@NonNull File projectDirectory, @NonNull String moduleName) {
        Path filePath = Paths.get(projectDirectory.getAbsolutePath(), moduleName, "src", "main", "res", "values", "strings.xml");
        return filePath.toFile();
    }

    /**
     * Returns the assets directory for the provided gradle module under the main source
     * set.
     *
     * @param projectDirectory The root of the gradle project.
     * @param moduleName       The module that contains the strings.
     *
     * @return The requested assets directory. The directory is returned even if does not exist.
     */
    public static @NonNull
    File getAssetsDirectoryForModule(@NonNull File projectDirectory, @NonNull String moduleName) {
        Path filePath = Paths.get(projectDirectory.getAbsolutePath(), moduleName, "src", "main", "assets");
        return filePath.toFile();
    }

    /**
     * Utility method to get a human-readable string representation of the errors of a TxPostResponseData
     * object.
     *
     * @return A string containing all the errors of the provided response object or
     * <code>null</code> if the provided response object contains no errors.
     */
    private static @Nullable
    String getErrorString(@NonNull LocaleData.TxPostResponseData response) {
        if (response.errors.length == 0) {
            return null;
        }

        return "Errors: " + Arrays.toString(response.errors);
    }

    /**
     * Utility method to get a human-readable string representation of the information of a
     * TxPostResponseData object.
     *
     * @return A string containing details about the provided response object.
     */
    private static @NonNull String getDetailsString(@NonNull LocaleData.TxPostResponseData response) {
        return String.format(Locale.US, "%d strings created, %d strings updated, " +
                        "%d strings skipped, %d strings deleted, %d strings failed",
                response.created, response.updated, response.skipped, response.deleted,
                response.failed);
    }

    /**
     * Utility method to get an error description using the non-downloaded locales.
     */
    private static @NonNull String getNonDownloadedLocalesString(@NonNull HashSet<String> nonDownloadedLocales) {
        return "The translations for the following locales were not downloaded: " + Arrays.toString(nonDownloadedLocales.toArray());
    }
}

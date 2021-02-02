package com.transifex.clitool;

import com.transifex.common.CDSHandler;
import com.transifex.common.LocaleData;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import androidx.annotation.NonNull;
import picocli.CommandLine;

import static picocli.CommandLine.ArgGroup;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.ParentCommand;

/**
 * The main class of the jar that contains the {@link #main(String[])}.
 * <p>
 * The execution and argument parsing is handled by picocli. Each subcommand is implemented in its
 * own class.
 */
@Command(name = "transifex", version = "transifex 0.1", mixinStandardHelpOptions = true,
        subcommands = {MainClass.PushCommand.class, MainClass.ClearCommand.class, CommandLine.HelpCommand.class},
        synopsisSubcommandLabel = "(push | clear)", sortOptions = false)
public class MainClass {

    private static final String TAG = MainClass.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(TAG);

    @Option(names = {"-t", "--token"}, required = true, description = "The Transifex token.")
    String token;

    @Option(names = {"-s", "--secret"}, required = true, description = "The Transifex secret.")
    String secret;

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

    @Command(name = "push", description = "Pushes source strings to CDS", sortOptions = false)
    public static class PushCommand implements Callable<Integer> {

        @ParentCommand
        MainClass mainClass;

        @Option(names = {"-p", "--purge"},
                description = "If set, the entire resource content is replaced by the pushed content " +
                        "of this request. Otherwise, source content of this request is appended to " +
                        "the existing resource content.")
        boolean purge;

        @ArgGroup(exclusive = true, multiplicity = "1")
        StringSources stringSources;

        static class StringSources {
            @Option(names = {"-m", "--module"},
                    description = "The name of the module that contains the source strings")
            String moduleName;

            @Option(names = {"-f", "--file"}, arity = "1..",
                    description = "One or more string.xml files containing source strings")
            File[] files;
        }

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

            // Create payload
            LocaleData.TxPostData.Meta meta = new LocaleData.TxPostData.Meta();
            meta.purge = purge;
            LocaleData.TxPostData postData = new LocaleData.TxPostData(sourceStringMap, meta);

            // Push to CDS
            CDSHandler cdsHandler = new CDSHandler(null, mainClass.token,
                    mainClass.secret, mainClass.hostURL);
            boolean success = cdsHandler.postSourceStrings(postData);

            if (success) {
                System.out.println("Source strings pushed successfully to CDS");
                return 0;
            }
            else {
                System.out.println("Error while pushing source strings to CDS");
                return 1;
            }
        }
    }

    @Command(name = "clear", sortOptions = false,
            description = "Clears all existing resource content from CDS")
    public static class ClearCommand implements Callable<Integer> {

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
            CDSHandler cdsHandler = new CDSHandler(null, mainClass.token,
                    mainClass.secret, mainClass.hostURL);
            boolean success = cdsHandler.postSourceStrings(postData);

            if (success) {
                System.out.println("Source strings cleared from CDS");
                return 0;
            }
            else {
                System.out.println("Error while contacting CDS");
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
}

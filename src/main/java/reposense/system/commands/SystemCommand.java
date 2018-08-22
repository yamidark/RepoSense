package reposense.system.commands;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import reposense.system.StreamGobbler;

public abstract class SystemCommand {
    public static final DateFormat GIT_SINCE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00+08:00");
    public static final DateFormat GIT_UNTIL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'23:59:59+08:00");

    protected static final String OR_OPERATOR_PATTERN = "\\|";
    protected static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$|]");

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final String COMMAND_ERROR_MESSEAGE_FORMAT = "Error returned from command %s on path %s:\n%s";

    protected final Path path;

    protected String command;

    public SystemCommand(String path) {
        this.path = Paths.get(path);
    }

    protected abstract void buildCommand();

    /**
     * Creates a process to execute the command stored and returns the output of the process.
     */
    public String execute() {
        buildCommand();

        ProcessBuilder pb;
        if (IS_WINDOWS) {
            pb = new ProcessBuilder()
                    .command("CMD", "/c", command)
                    .directory(path.toFile());
        } else {
            pb = new ProcessBuilder()
                    .command("bash", "-c", command)
                    .directory(path.toFile());
        }

        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new SystemCommandException("Error Creating Thread:" + e.getMessage());
        }

        StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
        StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
        outputGobbler.start();
        errorGobbler.start();

        int exit;
        try {
            exit = p.waitFor();
            outputGobbler.join();
            errorGobbler.join();
        } catch (InterruptedException e) {
            throw new SystemCommandException("Error reading output from process.");
        }

        if (exit == 0) {
            return outputGobbler.getValue();
        } else {
            throw new SystemCommandException(
                    String.format(COMMAND_ERROR_MESSEAGE_FORMAT, command, path.toString(), errorGobbler.getValue()));
        }
    }

    protected static String addQuote(String original) {
        return "\"" + original + "\"";
    }

    /**
     * Converts the {@code regexString} to a literal {@code String} where all regex meta-characters are escaped
     * and returns it.
     */
    protected static String escapeSpecialRegexChars(String regexString) {
        return SPECIAL_REGEX_CHARS.matcher(regexString.replace("\\", "\\\\\\")).replaceAll("\\\\$0");
    }
}

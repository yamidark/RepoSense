package reposense.system.commands;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import reposense.system.StreamGobbler;

public abstract class SystemCommand {
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final String COMMAND_ERROR_MESSEAGE_FORMAT = "Error returned from command %s on path %s:\n%s";

    protected final Path path;

    protected String command;

    public SystemCommand(String path) {
        this.path = Paths.get(path);
    }

    protected abstract void buildCommand();

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
            throw new RuntimeException("Error Creating Thread:" + e.getMessage());
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
}

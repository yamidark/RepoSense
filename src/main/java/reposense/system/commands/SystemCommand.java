package reposense.system.commands;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import reposense.system.StreamGobbler;

public abstract class SystemCommand {
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    protected final Path path;

    protected String command;

    public SystemCommand(String path) {
        this.path = Paths.get(path);
    }

    protected abstract void buildCommand();

    public String execute() {
        buildCommand();
        ProcessBuilder pb = null;
        if (IS_WINDOWS) {
            pb = new ProcessBuilder()
                    .command(new String[]{"CMD", "/c", command})
                    .directory(path.toFile());
        } else {
            pb = new ProcessBuilder()
                    .command(new String[]{"bash", "-c", command})
                    .directory(path.toFile());
        }
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new RuntimeException("Error Creating Thread:" + e.getMessage());
        }
        StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
        StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
        outputGobbler.start();
        errorGobbler.start();
        int exit = 0;
        try {
            exit = p.waitFor();
            outputGobbler.join();
            errorGobbler.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error Handling Thread.");
        }

        if (exit == 0) {
            return outputGobbler.getValue();
        } else {
            String errorMessage = "Error returned from command ";
            errorMessage += command + "on path ";
            errorMessage += path.toString() + " :\n" + errorGobbler.getValue();
            throw new RuntimeException(errorMessage);
        }
    }
}

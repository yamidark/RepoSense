package reposense.system.commands;

/**
 * Signals that there was an error while executing a system command.
 */
public class SystemCommandException extends RuntimeException {

    public SystemCommandException(String message) {
        super(message);
    }
}
